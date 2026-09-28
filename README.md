# Phyerma

**The physics lab that knows which phone it is running on.**

Phyerma is an open-source Android app for real physics experiments. It keeps the [phyphox](https://phyphox.org) experiment engine and adds a device-honest layer for the phones people actually carry: Xiaomi, OPPO, vivo, Honor, Huawei, and a separate track for HarmonyOS.

A generic “Android sensor” reading is not the same number on HyperOS, ColorOS, OriginOS, and HarmonyOS. Phyerma shows the phone, the sensor, and the crowd baseline beside the experiment — and the corrections we add later will be switches you can see.

Source: [github.com/elwina/Phyerma](https://github.com/elwina/Phyerma) · Application id: `com.phyerma.app` · License: [GPL-3.0](LICENSE)

中文说明在 [下方](#中文)。路线图（含尚未进安装包的功能）在 [ROADMAP.md](ROADMAP.md)。

## Today

This build already does the following.

- **Experiments.** Ready-made physics experiments from the phyphox library: acceleration, rotation, magnetic field, light, pressure, proximity, sound, GPS, camera, depth, and Bluetooth instruments. Import a `.phyphox` file or a QR code. Export CSV, TSV, or Excel.
- **This phone.** A device page with brand, model, and the system skin (HyperOS, MIUI, ColorOS, OriginOS, Funtouch, MagicOS, HarmonyOS / EMUI, Flyme, One UI, and others).
- **A baseline, not a hidden scale factor.** The page matches this model against a local snapshot of the crowd-sourced [phyphox sensor database](https://phyphox.org/sensordb/). Rest-g, noise, and rate are shown as a reference. They are not applied to experiment readings.
- **Sensor lab.** Every sensor Android exposes, in SI units, with a two-second window, measured noise, measured rate, and the range Android claims — labeled as a claim.
- **Remote screen.** Opt-in. A browser on the same network can view and control the experiment. Nothing to install on the computer.
- **Chinese and English.**
- **Quiet by default.** No Google Play Services, Firebase, Crashlytics, or advertising SDK. Sensor readings stay on the phone unless you export them, turn on remote access, or start an experiment that itself opens a network connection.

Refresh the bundled database snapshot with `python tools/import_phyphox_sensordb.py`. Per-sensor research notes, including the rule that a correction may not silently change a reading, live in [docs/sensors](docs/sensors/README.md).

## Where it is going

The next three surfaces are the product. They are written down so the direction is public. They are **not in the current build**.

1. **Calibration you can read.** Per-brand packs for Xiaomi (including Redmi and POCO), OPPO (including OnePlus and realme), and vivo (including iQOO), plus Honor and Huawei. HarmonyOS and HarmonyOS NEXT are their own track — their own sensor APIs and their own test phones — not a label on an Android skin. Every coefficient is a switch: raw value, corrected value, the number used, and where it came from.
2. **An experiment studio on the phone.** Describe the measurement in ordinary language. Phyerma drafts the sensors, the analysis, and the view. You preview it and save it on the device. A desktop editor is no longer required to add an experiment in class or in the field.
3. **An explanation on every control.** Press a trace, a formula, a sensor channel, or a button. A short note says what the number is, what a sane result looks like, and what this phone model tends to do. Built-in experiments ship with written notes. An optional model can answer a follow-up. The experiment still runs with that model off.

Also on the roadmap: the noise and the crowd baseline on the experiment screen itself, a QR that hands your new experiment to the next phone with no account, and school packs (pendulum, free fall, speed of sound, coil, spring) with the brand note attached. Detail and status: [ROADMAP.md](ROADMAP.md).

## Build

```text
gradlew.bat :app:assembleDebug
```

The debug application id is `com.phyerma.app`.

## Credits and marks

Phyerma is a fork of [phyphox-android](https://github.com/phyphox/phyphox-android), copyright 2016 Dr. Sebastian Staacks, 2nd Institute of Physics, RWTH Aachen University, released under the GNU GPL since phyphox 1.1.0.

**The names “phyphox” and “RWTH Aachen University”, and the RWTH Aachen logo, are trademarks of the upstream project.** Phyerma is not published by RWTH Aachen, and this repository is not the phyphox app-store build.

Sensor-database snapshots are crowd-sourced by phyphox users. FFTW, jlhttp, ZXing, Eclipse Paho, and Chart.js keep the licenses recorded by upstream.

---

## 中文

**Phyerma 是认得自己跑在哪台手机上的物理实验室。**

它是开源的 Android 物理实验应用，保留 [phyphox](https://phyphox.org) 的实验引擎，并为实际在用的手机加一层诚实的设备信息：小米、OPPO、vivo、荣耀、华为，以及单独的鸿蒙线。

HyperOS、ColorOS、OriginOS、鸿蒙上，同一个“Android 传感器”并不是同一个数。Phyerma 把机型、传感器和众包基线摆在实验旁边。以后加上的校正，都会是你能看见的开关。

### 现在就能用

- **实验。** phyphox 实验库：加速度、转动、磁场、光、气压、距离、声音、GPS、相机、深度、蓝牙仪器。可导入 `.phyphox` 或扫二维码。可导出 CSV、TSV、Excel。
- **这台手机。** 设备页写出品牌、型号和系统皮肤（HyperOS、MIUI、ColorOS、OriginOS、Funtouch、MagicOS、鸿蒙 / EMUI、Flyme、One UI 等）。
- **基线只作对照。** 用本地的 [phyphox 传感器库](https://phyphox.org/sensordb/) 快照匹配型号，显示静止重力、噪声和采样率。这些数**不会**被悄悄乘进实验读数。
- **传感器实验室。** Android 暴露出的每一项传感器，SI 单位，两秒窗口，实测噪声，实测速率，以及 Android 自称的量程——标明这是自称。
- **远程画面。** 需要你自己打开。同一网络里的浏览器可以看、可以控，电脑上不用安装软件。
- **中文和英文。**
- **默认安静。** 不用 Google Play 服务、Firebase、Crashlytics，也没有广告 SDK。除非你导出、打开远程访问，或运行一个自己会联网的实验，传感器读数留在手机上。

众包库快照用 `python tools/import_phyphox_sensordb.py` 刷新。每个传感器的研究笔记，以及“没接入之前不得静默改读数”的约定，在 [docs/sensors](docs/sensors/README.md)。

### 正在做（当前安装包里还没有）

1. **看得懂的校准。** 小米（含 Redmi、POCO）、OPPO（含一加、真我）、vivo（含 iQOO）分品牌做，荣耀和华为同样单独成包。鸿蒙和鸿蒙 NEXT 是另一条线：另一套传感器接口、另一组测试机，不是 Android 皮肤上的一个标签。每个系数都是开关：原始值、校正值、用了哪个数、数从哪来。
2. **手机上的实验工作室。** 用白话描述要测什么。Phyerma 起草传感器、分析和界面，你在手机上预览并保存。课堂和野外加实验，不再依赖电脑上的网页编辑器。
3. **每个控件都能解释。** 按住一条曲线、一个公式、一个传感器通道或一个按钮，弹出短说明：这个数是什么、怎样算正常、这台手机通常会怎样。内置实验自带写好的说明。可选模型回答追问。模型关掉，实验照样跑。

同一条路线上还有：实验画面上直接看到噪声和众包基线；一个二维码把刚做好的实验交给下一台手机，不需要账号；单摆、自由落体、声速、线圈、弹簧等课程包，并附上品牌备注。状态表见 [ROADMAP.md](ROADMAP.md)。

### 名称

Phyerma 派生自 [phyphox-android](https://github.com/phyphox/phyphox-android)（Sebastian Staacks 博士，亚琛工业大学第二物理研究所，自 phyphox 1.1.0 起以 GNU GPL 发布）。

**“phyphox”和“RWTH Aachen University”以及亚琛工大标识仍是上游项目的商标。** Phyerma 不是亚琛工大发布的，这个仓库也不是应用商店里的 phyphox。
