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

**`<video>` 视频输入（已接入）**：实验可声明 `<video resolution="480|720|1080" fps="30" audio="true|false">`，测量期间把相机流录成 MP4 段（暂停切段，`video_<i>.mp4`）。实现复用相机 GL 管线：`AnalyzingOpenGLRenderer.draw()` 每帧多渲染一路到 `MediaCodec` 输入面（H.264 + `MediaMuxer`），`eglPresentationTimeANDROID` 用相机帧时间戳写 MP4 播放时间——视频文件自带逐帧时间。`t` 输出把每帧实验时间写进 buffer（与 MP4 帧数严格 1:1），`video.json` 记分辨率/实测帧数/段起止实验时间/事件表/机型。可选 AAC 音轨（`audio="true"`）内嵌同一 MP4——声画双同步载体。`<camera>` 与 `<video>` 共存时共享同一相机会话，分析路径配置优先。预览复用 `<camera-gui>` 视图元素（放则持续取景，不放则纯后台录）。导出 ZIP 含 `video/` 目录。仅限 video-only 实验时预览走连续 AE + 作者设定分辨率；与 `<camera>` 共存时沿用分析配置。

## 6. 候选规则（草案）

先记录各机 Camera2 能力级，再决定哪些光度实验要降级提示。不把预览亮度换算成 lux。

## 7. 待测机型与问题

- [ ] 曝光锁定是否可用
- [x] vivo V2452A（Android 16）`<video>` 实测：相机 SurfaceTexture 时间戳时基正常（无 `timeAdjustment` 警告），但时钟域比 `elapsedRealtime` 慢 ~0.1s——首帧映射落在 START 之前，`experimentTimeAt` 用首映射向后外推处理。编码器输入侧丢帧（surface 模式下喂快即丢，`framesSubmitted` > `frames` 可见）
- [x] vivo V2452A MediaMuxer 怪癖：`writeSampleData` 成功返回但样本不进容器（stsz 验证）——原因是 muxer 等 AAC 音轨 announce 才能 start，期间到达的视频包若被丢弃，开头 ~1s 全丢。修法：两条轨共用一个按 PTS 排序的包队列，muxer start 前不丢包，`frames`/`framesInContainer` 双计数核对
- [ ] 编码器吞吐：720p30 下 `framesSubmitted` vs MP4 实际帧数（丢帧）
- [ ] 长时间录制的发热/存储与 ZIP 导出体积
- [ ] 光谱实验在主摄上的可用性
