<div align="center">

![Social Preview](social-preview.png)
  
![OS](https://img.shields.io/badge/OS-Android%208%2B-lightgrey)
[![Downloads](https://img.shields.io/github/downloads/pintargasper/smokingtracker/total?style=flat-square)](https://github.com/pintargasper/SmokingTracker/releases) 

</div>

---
<div align="center">
  
[![GitHub Releases](https://custom-icon-badges.herokuapp.com/badge/Website-lightgray?style=for-the-badge&logo=website&logoColor=white)](https://smoking.gasperpintar.com)

[![GitHub Releases](https://custom-icon-badges.herokuapp.com/badge/Download-lightgray?style=for-the-badge&logo=download&logoColor=white)](https://github.com/pintargasper/SmokingTracker/releases/latest)
[![GitHub Releases](https://custom-icon-badges.herokuapp.com/badge/Google%20play-lightgray?style=for-the-badge&logo=download&logoColor=white)](https://play.google.com/store/apps/details?id=com.gasperpintar.smokingtracker)

</div>

## Table of Contents
- [About](#-about)
- [Supported Languages](#-supported-languages)
- [Dependencies & Versions](#-dependencies--versions)
- [How to Build](#-how-to-build)

## 🚀 About

**Smoking Tracker** is an Android application written in **Kotlin**, designed to help users easily track the number of cigarettes smoked. The app allows displaying data on **weekly, monthly, and yearly graphs**, helping users monitor their progress in quitting smoking.  

The app also supports **automatic backups** if enabled on the device, and features a **clean, intuitive interface** for ease of use.  

**Key Features**  
- **Local data storage** for privacy and security  
- **Daily and historical statistics** with graphs  
- **Automatic backups** (device-dependent)  
- **Support for multiple languages** (English and Slovenian)  
- **Simple and intuitive user interface**
  
## 🌐 Supported Languages
- English  
- Slovenian  

> Additional languages will be added in future releases

## 📝 Dependencies & Versions

**Gradle Plugin & Kotlin**  
- Android Gradle Plugin: 8.13.0  
- Kotlin: 2.2.10  

**Libraries**  
> All libraries are configured in `libs.versions.toml`.

## 📝 How to Build

### Steps

```shell
# 1️⃣ Clone repository
git clone [https://github.com/YourUsername/SmokingTracker.git](https://github.com/pintargasper/SmokingTracker.git)
cd SmokingTracker

# 2️⃣ Open project in Android Studio
# Import Gradle project and sync

# 3️⃣ Build APK or run on emulator/device
./gradlew assembleDebug   # for debug build
./gradlew assembleRelease # for release build