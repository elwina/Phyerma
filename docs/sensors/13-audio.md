# 麦克风 / 音频

| 项 | 值 |
|---|---|
| 实验类型名 | `<audio>` |
| Android API | AudioRecord |
| SI 单位 | 采样点无量纲；派生 Hz、相对振幅 |
| 众包库字段 | 无 |
| 接入状态 | 独立实现 |
| 研究状态 | 待研究 |

## 1. 物理量与期望值

采样率常见 48 kHz 或 44.1 kHz。声学实验要的是相对波形/频谱，不是已校准的 Pa 声压。

## 2. 代码路径

实验 XML 的 `<audio>`，不经 `SensorInput`。绝对声压需要灵敏度标定，HAL 不提供。

## 3. 内置实验

`audio_spectrum`、`audio_scope`、`audio_amplitude`、`audio_autocorrelation`、`frequency_history`、`doppler`、`sonar`、`acoustic_stopwatch`、`applause_multi`、`inelastic_collision`、`tone_generator`（扬声器输出）。

## 4. 已知坑

- OEM 录音增强、AEC、AGC 会破坏示波器和多普勒。
- 采样率名不副实或被重采样。
- 权限与通话中占用麦克风。

## 5. 现行做法

上游原样。传感器页不列麦克风。

## 6. 候选规则（草案）

需要时在音频实验里检测实际采样率，并提示「系统音效可能已处理」。不做 Pa 校准，除非以后单独做声级计。

## 7. 待测机型与问题

- [ ] 目标机能否关录音增强
- [ ] 声纳 / 多普勒在国产机上是否明显劣于 Pixel 类
