# 相机

| 项 | 值 |
|---|---|
| 实验类型名 | `<camera>` |
| Android API | CameraX / Camera2（`camera/CameraInput.kt`） |
| SI 单位 | 像素、相对亮度、HSV；不是 lux |
| 众包库字段 | 无 |
| 接入状态 | 独立实现 |
| 研究状态 | 待研究 |

## 1. 物理量与期望值

光度/光谱实验分析预览帧，不是 `TYPE_LIGHT`。绝对亮度受曝光、ISO、色调映射影响。

## 2. 代码路径

`de.rwth_aachen.phyphox.camera.CameraInput`。实验可锁曝光、选 AE 策略、框选 ROI。

## 3. 内置实验

`camera-luminance`、`camera-hsv`、`camera_spectrum_luma`、`camera_stopwatch_luma`、`camera_stopwatch_hue`、`spectroscopy`。

## 4. 已知坑

- 国产机 Camera2 能力不齐（锁定曝光/手动 ISO 失败）。
- 多摄切换、美颜/HDR 预览破坏光度。
- 和光照传感器不是同一条精度问题。

## 5. 现行做法

上游原样。传感器页不列相机。

## 6. 候选规则（草案）

先记录各机 Camera2 能力级，再决定哪些光度实验要降级提示。不把预览亮度换算成 lux。

## 7. 待测机型与问题

- [ ] 曝光锁定是否可用
- [ ] 光谱实验在主摄上的可用性
