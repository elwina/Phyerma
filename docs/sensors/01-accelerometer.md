# 加速度计

| 项 | 值 |
|---|---|
| 实验类型名 | `accelerometer` |
| Android type | `Sensor.TYPE_ACCELEROMETER`（值为 1） |
| SI 单位 | m/s² |
| 分量 | x y z，实验可再要 `abs`、`t` |
| 众包库字段 | `a` 有无；`aa` rest-g 均值；`as` σ；`arat` Hz；`an` 芯片名；`av` 厂商 |
| 接入状态 | 仅对照，未改实验 |
| 研究状态 | **当前** |

这是第一优先：单位错会直接毁掉倾斜、频谱和几乎所有力学实验；线性加速度、重力也往往同源。

## 0. 数据从哪来

加速度计读数**不是** Phyerma 算出来的，也**不是**众包库给的。我们只向 Android 要「默认加速度计」，再把系统回调的三个 float 原样收下。

```text
MEMS 芯片（质量块 + 电容）
    → OEM 驱动 / Sensor HAL（把 ADC 换成 float，声称单位是 m/s²）
        → Android SensorManager
            → SensorEvent.values[0,1,2] + timestamp
                ├─ 实验：PhyphoxFile 解析 <sensor type="accelerometer"> → SensorInput
                └─ 传感器页：SensorLab / SensorDetail 直接 registerListener
```

1. **物理**：芯片测的是比力（proper acceleration），重力算进去。静止时模长应是 \(g\)，不是 0。加速度计本身是硬件，**不是合成**。合成指的是系统从它再拆出「重力」和「线性加速度」两个虚拟传感器，见下节。
2. **坐标**：设备坐标系。屏幕朝自己时，x 右、y 上、z 垂直屏幕向外。平放屏幕朝上，常见 \(z \approx +g\)。
3. **我们拿到的就是** `event.values[0..2]`。加速度计**没有** vendor 名字回退，只用 `getDefaultSensor(TYPE_ACCELEROMETER)`。
4. **库里的 `aa`**：别人在 phyphox 传感器库提交的静止模长，启动时按 `Build.MODEL` 对上才显示。不参与实验、不改 `values`。
5. **唯一会动数字的**：采样策略（抽点、平均、补点）和时间戳偏移。分量本身不换算。

### 对 OEM HAL 的访问程度

普通应用（含 Phyerma）**进不了 HAL**。看不到 ADC 原始计数、出厂标定、传感器中枢固件、HAL 里有没有低通。SELinux 也拦着 `/dev`、I2C。

| 层 | 能不能碰 | 我们实际用到 |
|---|---|---|
| MEMS / I2C / 中枢固件 | 否（除非 root + 厂商文档，不能进正式应用） | 不用 |
| OEM Sensor HAL `.so` | 否 | 不用 |
| `Sensor` 元数据 | 是 | 名字、vendor、自称量程/分辨率/`minDelay` |
| `SensorEvent` | 是 | `values[]`、`timestamp`、`accuracy` |
| `TYPE_ALL` 列表 | 是 | 传感器页；加速度计实验只用默认 type 1 |
| 未校准加速度 / Direct Channel | 系统若提供则可以 | **尚未接** |

能**防**的是输出层的系统性错误（单位差 9.8 倍、Hz 虚标），靠静止模长和自己算的时间戳。防不了已经揉进 float 里的滤波、轴偏、非线性。现在连输出层校正都还没接到实验。

### 硬件加速度 vs 合成量

芯片只交出一个含重力的矢量 \(\mathbf{a}\)。Android 再提供两个 type，多数机是在传感器中枢或 HAL 里用滤波拆出来的，不是第二颗芯片：

\[
\mathbf{a} \approx \mathbf{a}_\text{linear} + \mathbf{g}
\]

| type | 谁在测 / 算 | 静止时期望 |
|---|---|---|
| `TYPE_ACCELEROMETER` | MEMS 硬件 | \(\|\mathbf{a}\| \approx g\) |
| `TYPE_GRAVITY` | 从 \(\mathbf{a}\)（常加陀螺）低通 / 融合 | \(\|\mathbf{g}\| \approx g\) |
| `TYPE_LINEAR_ACCELERATION` | \(\mathbf{a} - \mathbf{g}\) | \(\approx \mathbf{0}\) |

所以「合成」= 系统估计重力方向并从比力里减掉，不是 Phyerma 算的，也不是另一条更准的 HAL。单位错了，三路通常一起错。猛转手机时分离会滞后，线性加速度会短暂假跳。拆法与核对判据见 `02` §1.1。

厂商公开能力（2026-09 查开放平台，不是 HAL）：

- 小米 / OPPO / vivo 的手机 APK **没有**「更准的加速度计 SDK」。HyperOS / ColorOS / OriginOS 仍是 Android 应用框架，原生应用继续走 `SensorManager`。
- 三家都有**快应用 / 快游戏**的 `startAccelerometer` / `subscribeAccelerometer`（game≈20 ms、ui≈60 ms、normal≈200 ms）。这是 JS 封装，单位写 m/s²，小米文档加了「设备实现为主」。Phyerma 是 APK，用不上。
- 小米另有跑步机计步私有 type `33171041` + `miui_step_counter_service`，仍经 `SensorManager`，且不是比力、只覆盖极少机型。
- OPPO 对第三方开放的是 Hyper Boost / 相机等，适配文档只强调 Android 12 的 `HIGH_SAMPLING_RATE_SENSORS`（>200 Hz 要权限），没有换单位的 IMU SDK。
- vivo 手表 BlueOS 有 `subscribeAccelerometer`；手机侧同样是快应用那套，不是给 APK 的精密库。

## 1. 物理量与期望值

Android 文档：`TYPE_ACCELEROMETER` 是**含重力**的比力，单位 m/s²。

理想静止（任意姿态）：

\[
|a| = \sqrt{x^2+y^2+z^2} \approx g \approx 9.80665\,\mathrm{m/s^2}
\]

桌面平放、屏幕朝上时，常见约定是 \(z \approx +g\)，\(x \approx y \approx 0\)（部分机坐标轴相反，只改符号不改模长）。

判断单位的经验门槛（静止 2 s 窗口，σ 不能太大）：

| 实测 \|a\| | 含义 |
|---|---|
| 约 9.5–10.2 | HAL 按 SI 报，正常 |
| 约 0.95–1.05 | 很大概率把 g 当成无量纲 1，应按 \(g_0\) 放大 |
| 约 0 | 没在动但读数死了，或权限/传感器没挂上 |
| 约 980 | 极罕见，可能当 cm/s² |

众包库的 `aa` 是用户提交的静止模长均值，**不是**我们测的。有的行已经是 ~9.8，有的是 ~1.0，说明库本身混了两种单位，不能当唯一判据。

标准重力取 \(g_0 = 9.80665\,\mathrm{m/s^2}\)（CGPM）。本地 g 随纬度大约 9.78–9.83，远小于「1 对 9.8」这种数量级错误。

## 2. 代码路径

实验：`SensorInput.resolveSensorName(accelerometer)` → `getDefaultSensor(TYPE_ACCELEROMETER)`。  
没有 vendor 名字回退。`calibrated` 开关**不**作用于加速度计（那是磁力计的）。  
`onSensorChanged` 把 `event.values[0..2]` 原样写入 x/y/z；`abs` 为 \(\sqrt{x^2+y^2+z^2}\)。

UI：

- 本机页：命中库则显示 `rest-g`、σ、Hz、芯片名。
- 传感器页：`SENSOR_DELAY_GAME` 列表 + 详情 `SENSOR_DELAY_FASTEST`。
- `SampleWindow`：2 s 滚窗，模长均值、σ、实测 Hz。
- `SensorCatalog.dbBaseline`：拼 `phyphox 库：芯片名  rest-g=…  σ=…  … Hz`。

库解析：`DeviceProfile` 读 `aa/as/arat/an/av`。匹配键是 `Build.MODEL`，品牌用 `BRAND`/`MANUFACTURER` 兜底。

## 3. 内置实验

| 文件 | 用法 |
|---|---|
| `accelerometer.phyphox` | 原始三轴 + 模长，图轴单位写 m/s² |
| `acc_spectrum.phyphox` | `rate="0"` 尽快，频谱 |
| `inclination.phyphox` | `rate="2" average="true"`，用重力方向估倾角 |
| `sensordb.phyphox` | 提交本机数据到上游库（`ignoreUnavailable`） |

间接依赖（读的是线性加速度或陀螺，但单位错会连锁）：弹簧、电梯、向心、运动秒表、单摆。见 `02` / `03`。

## 4. 已知坑

待实机验证，先当假设：

1. **单位当 g**：部分国产 HAL / 厂商私有加速度计 `values` 在 1 附近，不在 9.8。实验图仍标 m/s²，单摆周期公式、倾角 `atan` 会错。
2. **自称分辨率骗人**：`getResolution()` 常过于乐观。以 2 s σ 为准。
3. **采样率虚标**：`minDelay` 说能 200+ Hz，实测 `event.timestamp` 算出的 Hz 低一截。用 `SampleWindow.formatRate()`。
4. **低通 / 批处理**：省电时 OEM 可能合并事件或降率，力学实验抖动变小、带宽变窄。
5. **坐标轴**：有的平板/折叠屏在某些姿态下轴对调。模长仍可用，分量实验要小心。
6. **库 rest-g 混单位**：`aa≈1` 的行不能直接当「应乘 9.8」的指令，要和本机窗口对读。

上游 `SensorInput` 已处理的：离谱时间戳偏移（见目录 README 横切问题）。**没有**单位换算。

## 5. 现行做法

- 实验：原样 HAL。
- 传感器详情：六位小数、模长、σ、Hz、库基线并列。
- 不根据 `aa` 或本机均值改 `SensorInput`。

这样能看见「这台机是 9.81 还是 1.00」，但倾斜/频谱实验仍吃错单位。

## 6. 候选规则（草案，未实施）

检测（须同时满足，避免把真 1 m/s² 的运动当单位错）：

1. 用户在传感器详情明确处于静止，或连续 2 s 窗口 σ < 0.05 × 均值。
2. 模长均值 \(m\) 落在 0.85–1.15（疑似 g 单位），或 9.4–10.3（疑似 SI）。
3. 若有库命中：把本机 \(m\) 和 `aa` 一起显示；**仅当本机 \(m\) 自己落在 0.85–1.15 时**才建议缩放。

校正（若做）：

- 乘 \(g_0 = 9.80665\)，只作用于 `TYPE_ACCELEROMETER` 的 x/y/z/abs。
- 必须有开关，默认建议：检测为 g 单位时**提示**，用户确认后才写入实验。
- 线性加速度 / 重力是否同乘：见 `02`、`10`。同一颗芯片通常要一致。

误判代价：若静止模长真的接近 1 m/s²（几乎不可能在地球表面），会把数据放大约 10 倍。所以必须人眼确认，不能开机暗改。

## 7. 待测机型与问题

- [ ] 模拟器 Pixel（对照）：静止 \|a\| 是否约 9.8
- [ ] 至少一台小米 / Redmi（HyperOS）
- [ ] 至少一台华为 / 荣耀（Harmony / MagicOS）
- [ ] 至少一台 OPPO / vivo / 一加
- [ ] 对每台记录：芯片名、自称分辨率、2 s σ、实测 Hz、库 `aa`、是否该乘 \(g_0\)
- [ ] 决定：检测提示放在传感器页，还是实验开始时挡一次
