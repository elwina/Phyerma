# 蓝牙外设

| 项 | 值 |
|---|---|
| 实验类型名 | 各 BLE 实验自带 |
| Android API | `BluetoothInput` / `BluetoothOutput` |
| SI 单位 | 外设协议各自定义 |
| 众包库字段 | 无（那是手机传感器库） |
| 接入状态 | 上游原样 |
| 研究状态 | 待研究 |

## 1. 物理量与期望值

外接 IMU、气压、温湿度、距离、心率等。单位由固件和 `.phyphox` 映射保证，不走手机 HAL。

## 2. 代码路径

`de.rwth_aachen.phyphox.Bluetooth.*`。实验列表按设备名/UUID 过滤。连接失败有重试，不经 Google Play。

## 3. 内置实验

`assets/experiments/bluetooth/`：TI SensorTag、puck.js、PocketLab、phyphox µ 系列、HID 鼠标、心率等。

## 4. 已知坑

- 国产机 BLE 扫描在后台被杀（权限/保活），不是传感器单位问题。
- 不要把外设读数和手机加速度计校正混用。

## 5. 现行做法

上游原样。本机页不列已连接 BLE。

## 6. 候选规则（草案）

课堂若缺气压/温湿度，优先推荐 BLE 探头，而不是伪造手机传感器。保活另开产品条目。

## 7. 待测机型与问题

- [ ] 目标机 BLE 扫描与连接稳定性
- [ ] 是否要做「用外设标定手机加速度计」的工作流（很后）
