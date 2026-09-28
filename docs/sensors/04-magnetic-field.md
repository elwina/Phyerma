# 磁力计

| 项 | 值 |
|---|---|
| 实验类型名 | `magnetic_field` |
| Android type | `TYPE_MAGNETIC_FIELD` / `TYPE_MAGNETIC_FIELD_UNCALIBRATED` |
| SI 单位 | µT |
| 分量 | x y z + abs + accuracy |
| 众包库字段 | `m` 有无；`ms` σ；`mrat` Hz；`mn` 芯片名 |
| 接入状态 | 仅对照，未改实验 |
| 研究状态 | 待研究 |

## 1. 物理量与期望值

地磁场量级大约 25–65 µT（中国多在 40–60）。室内铁磁体可把模长拉到几百 µT。  
校准磁力计会被 OEM 硬铁/软铁校正；未校准更接近原始读数，适合磁尺这类「看相对变化」的实验。

## 2. 代码路径

`SensorInput.start()`：API 18+ 按 `calibrated` 在 `TYPE_MAGNETIC_FIELD` 与 `TYPE_MAGNETIC_FIELD_UNCALIBRATED` 之间切换。  
实验菜单「校准磁力计」复选框：`Experiment.java` 的 `action_calibrated_magnetometer`。  
未校准时 accuracy 记 0；校准时把 `SensorEvent.accuracy` 映射到 -1 / 1 / 2 / 3（注意源码 `switch` 没有 `break`，实际会落到 HIGH=3，这是上游就有的行为，研究时不要当新 bug 先改）。

## 3. 内置实验

`magnetometer.phyphox`，`magnetic_ruler.phyphox`（25 Hz 平均），`mag_spectrum.phyphox`，蓝牙 HID 鼠标磁力。

## 4. 已知坑

- 金属中框 / 磁吸充电导致硬铁偏置。
- 校准把有用的局部场「抹平」，磁尺可能更该用未校准。
- 部分机校准质量差，转一圈模长不该变却变。
- 单位偶尔被报成 T 或 mG，数量级会差 10³–10⁴，比加速度更好认。

## 5. 现行做法

实验原样 + 用户可选校准/未校准。传感器页有库 σ/Hz/芯片名。不改单位。

## 6. 候选规则（草案）

第一版只做数量级警告（模长长期 < 1 或 > 1e4 µT）。硬铁偏置校准留给磁尺实验可选，不进全局 `SensorInput`。

## 7. 待测机型与问题

- [ ] 室外空旷：模长是否 25–65 µT
- [ ] 校准开/关对磁尺实验差多少
- [ ] 核对 `accuracy` 的 switch 是否应补 break（单独开缺陷，不和单位校正捆在一起）
