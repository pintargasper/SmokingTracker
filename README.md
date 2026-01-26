<div align="center">

<img src="social-preview.png" width="200px" alt="Social Preview">
<br>
<h1>Smoking tracker</h1>

![OS](https://img.shields.io/badge/OS-Android%208%2B-lightgrey)
[![Downloads](https://img.shields.io/github/downloads/pintargasper/smokingtracker/total?style=flat-square)](https://github.com/pintargasper/SmokingTracker/releases)
[![Translation status](https://translate.gasperpintar.com/widget/smokingtracker/svg-badge.svg)](https://translate.gasperpintar.com/engage/smokingtracker/?utm_source=widget) 

</div>

---
<div align="center">

[![GitHub Releases](https://custom-icon-badges.herokuapp.com/badge/Website-lightgray?style=for-the-badge&logo=website&logoColor=white)](https://gasperpintar.com/smoking-tracker)

[![GitHub Releases](https://custom-icon-badges.herokuapp.com/badge/Download-lightgray?style=for-the-badge&logo=download&logoColor=white)](https://github.com/pintargasper/SmokingTracker/releases/latest)
[![GitHub Releases](https://custom-icon-badges.herokuapp.com/badge/Google%20play-lightgray?style=for-the-badge&logo=download&logoColor=white)](https://play.google.com/store/apps/details?id=com.gasperpintar.smokingtracker)

</div>

## Table of Contents
- [About](#-about)
- [Supported Languages](#-supported-languages)
- [Help Translate](#-help-translate)
- [Dependencies & Versions](#-dependencies--versions)
- [How to Build](#-how-to-build)

## 🚀 About
**Smoking Tracker** is an easy to use smoking tracking app that helps you understand your habits and progress towards quitting. Every cigarette you smoke is clearly recorded, giving you detailed insight into your daily, weekly and monthly patterns

**Key Features**
- **Local data storage** for greater privacy
- **Daily, monthly and yearly** statistics with graphs
-  **Simple analytics** to help you understand your habits
- **Automatic backups** (device-dependent)
- **Multi language support**: English and Slovenian
- **Simple and intuitive** user interface

## 🌐 Supported Languages
- English
- Slovenian

> Additional languages will be added in future releases

## 🌐 Help translate

<div align="center">
  <a href="https://translate.gasperpintar.com/engage/smokingtracker/?utm_source=widget" target="_blank">
    <img
      src="http://translate.gasperpintar.com/widget/smokingtracker/multi-auto.svg"
      width="500px"
      alt="Help translate"
    >
  </a>
</div>

## 📝 Dependencies & Versions

**Gradle Plugin**
- Android Gradle Plugin: 9.0.0

**Libraries**
> All libraries are configured in `libs.versions.toml`.

## 📝 How to Build

### Steps

```shell
# 1️⃣ Clone repository
git clone https://github.com/pintargasper/SmokingTracker.git
cd SmokingTracker

# 2️⃣ Open project in Android Studio
# Import Gradle project and sync

# 3️⃣ Build APK or run on emulator/device
./gradlew assembleDebug   # for debug build
./gradlew assembleRelease # for release build
