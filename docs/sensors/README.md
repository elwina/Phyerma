# Sensor notes / 传感器研究记录

Public product text is in [README](../../README.md). Calibration, on-phone experiment drafts, and explanations that are not in the app yet are in [ROADMAP](../../ROADMAP.md).

This directory is a per-sensor research log, not a feature list. The notes below are kept in Simplified Chinese. A correction must not change an experiment reading until that sensor's file says it is connected. Brand calibration on the roadmap follows the same rule: the coefficient is visible, off by default, and crowd-sourced rest-g / σ / Hz stay a reference.

公开产品说明在 [README](../../README.md)，尚未进安装包的校准、手机端实验生成和解释在 [ROADMAP](../../ROADMAP.md)。

本目录是 Phyerma 逐个传感器的研究笔记，不是功能清单。各传感器笔记为简体中文。实验读数在对应条目改成「已接入」之前，**不得静默改 HAL 数值**。路线图里的品牌校正也遵守这一条：系数必须看得见，默认关闭，众包的 rest-g / σ / Hz 只当对照。

刷新众包库：

```text
python tools/import_phyphox_sensordb.py
```

快照位置：`app/src/main/assets/device-db/phyphox-sensordb.json`  
导入脚本：`tools/import_phyphox_sensordb.py`  
对照页：本机 `DevicePageActivity`，传感器 `SensorLabActivity` / `SensorDetailActivity`  
实验入口：`SensorInput`（以及 GPS / 音频 / 相机等独立类）

## 研究约定

1. 每个传感器一份文件，按 [`_template.md`](_template.md) 写。
2. 先记录「现在代码做什么」，再写「中国机常见坑」，最后才写「要不要校正、怎么校正」。
3. 校正必须可开关、可看见。库里的 rest-g / σ / Hz 默认只当对照，不当隐式系数。
4. 改实验路径时回这里把「接入状态」改掉，并补实测机型。

## 目录与状态

| 文件 | 传感器 | 实验类型名 | 接入状态 | 研究状态 |
|---|---|---|---|---|
| [01-accelerometer.md](01-accelerometer.md) | 加速度计 | `accelerometer` | 仅对照，未改实验 | **当前** |
| [02-linear-acceleration.md](02-linear-acceleration.md) | 线性加速度 | `linear_acceleration` | 仅对照，未改实验 | 待研究 |
| [03-gyroscope.md](03-gyroscope.md) | 陀螺仪 | `gyroscope` | 仅对照，未改实验 | 待研究 |
| [04-magnetic-field.md](04-magnetic-field.md) | 磁力计 | `magnetic_field` | 仅对照，未改实验 | 待研究 |
| [05-pressure.md](05-pressure.md) | 气压 | `pressure` | 仅对照，未改实验 | 待研究 |
| [06-light.md](06-light.md) | 光照 | `light` | 仅对照，未改实验 | 待研究 |
| [07-proximity.md](07-proximity.md) | 距离 | `proximity` | 仅对照，未改实验 | 待研究 |
| [08-temperature.md](08-temperature.md) | 温度 | `temperature` | 仅对照，未改实验 | 待研究 |
| [09-humidity.md](09-humidity.md) | 湿度 | `humidity` | 仅对照，未改实验 | 待研究 |
| [10-gravity.md](10-gravity.md) | 重力 | `gravity` | 代码认，内置实验几乎不用 | 待研究 |
| [11-attitude.md](11-attitude.md) | 姿态 | `attitude` | 代码认，内置实验几乎不用 | 待研究 |
| [12-gps.md](12-gps.md) | GPS / 定位 | `<location>` | 不经 SensorManager | 待研究 |
| [13-audio.md](13-audio.md) | 麦克风 | `<audio>` | 不经 SensorManager | 待研究 |
| [14-camera.md](14-camera.md) | 相机 | `<camera>` | 不经 SensorManager | 待研究 |
| [15-depth.md](15-depth.md) | 深度 / ToF | depth | 少数机 | 待研究 |
| [16-bluetooth.md](16-bluetooth.md) | 蓝牙外设 | BLE 实验 | 外接单位自负 | 待研究 |

传感器页还会列出未校准变体、游戏旋转矢量、计步、厂商私有 type。没有对应实验文件的，先记在「传感器页可见、实验不吃」里，不单独开篇。

## 横切问题（所有 SensorInput 共用）

- **时间戳**：部分机 `event.timestamp` 相对开机时间为负或超前。`SensorInput.fixDeviceTimeOffset` 会在 \(t < -300\,\mathrm{s}\) 或明显超前时估一个固定偏移；\(-300\,\mathrm{s} < t < 0\) 则夹到 0。
- **采样策略**：`auto` / `request` / `generate` / `limit`。`generate` 会在 on-change 传感器长时间无事件时补点。
- **厂商私有传感器**：温度、湿度、气压在标准 type 缺失时，会按名字在 `TYPE_DEVICE_PRIVATE_BASE` 以上的 HAL 项里找，并弹出 vendor 警告。
- **自称量程 / 分辨率**：`Sensor.getMaximumRange()` / `getResolution()` 不采信，传感器详情页只展示，文案写明「Android 自称」。
- **和加速度同一处境**（APK 只经 `SensorManager`，进不了 HAL，三家没有更准的专用 SDK）：陀螺、磁力、气压、光照、距离、温度、湿度，以及软件合成的线性加速度 / 重力 / 姿态。快应用另有 JS 壳（加速度、罗盘、陀螺、光、距离、计步），不是更准的数。相机、GPS、麦克风、健康/计步特权、蓝牙外设**不是**这条。派生量（倾角、气压高度、向心加速度）跟着源传感器走。
