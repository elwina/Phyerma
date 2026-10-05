# 麦克风 / 音频

| 项 | 值 |
|---|---|
| 实验类型名 | `<audio>` |
| Android API | AudioRecord |
| SI 单位 | 采样点无量纲；派生 Hz、相对振幅 |
| 众包库字段 | 无 |
| 接入状态 | 实验路径原样；另有声纹同步轨 SyncAudioTrack（可选，默认关） |
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

**声纹同步轨（已接入）**：`SyncAudioTrack` 提供一条实验无关的可选录音轨，实验菜单可勾选「记录声纹同步音频」。实验自带 `<audio>` 时共享同一条 `AudioRecord`（在 `processAnalysis` 的 `read()` 处分流，首轮样本也保留），否则自建 `AudioRecord`，优先 `AudioSource.UNPROCESSED` 绕开 OEM 增强，16bit/48k/mono。锚点用 `getTimestamp(TIMEBASE_MONOTONIC)` 在数据流线程内取，帧序号↔`elapsedRealtimeNanos`↔实验时间，与传感器同一时基。暂停切段，每段一 WAV；导出 ZIP 内有 `sync/audio_<i>.wav` + `sync/sync.json`（锚点、分段、事件、机型、丢帧估计）。清数据一并丢弃。对齐计算不在本机做。

## 6. 候选规则（草案）

需要时在音频实验里检测实际采样率，并提示「系统音效可能已处理」。不做 Pa 校准，除非以后单独做声级计。

声纹同步的锚点斜率（Δ帧/Δ纳秒）即实测采样率，可用来发现重采样。

## 7. 待测机型与问题

- [ ] 目标机能否关录音增强
- [ ] 声纳 / 多普勒在国产机上是否明显劣于 Pixel 类
- [ ] UNPROCESSED 在国产机上是否无声或仍被处理
- [ ] 同步轨与实验 `<audio>` 并行时 tap 覆盖率（overrun 检测值）
- 同步精度物理上限：声速 ~343 m/s，间距 1 m ≈ 3 ms 传播延迟；拍手类瞬态检测 ~5-10 ms
