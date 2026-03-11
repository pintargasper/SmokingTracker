<div align="center">

<img src="fastlane/metadata/android/sl/images/featureGraphic.png" width="600px" alt="Social Preview">
<br>
<h1>Sledilnik Kajenja</h1>

<p align="center">
  <a href="README.md">English</a> | <strong>Slovenščina</strong> 
</p>

</div>

<div align="center">

Ustvaril [Gašper Pintar](https://gasperpintar.com)

[![GitHub Releases](https://custom-icon-badges.herokuapp.com/badge/Spletna%20stran-lightgray?style=for-the-badge&logo=website&logoColor=white)](https://gasperpintar.com/smoking-tracker)

<div style="display:flex; justify-content:center; align-items:center; gap:2px;">
  <a href="https://github.com/pintargasper/SmokingTracker/releases/latest" target="_blank">
    <img
      src="assets/badges/en-US/github.png"
      height="40px"
      alt="Download APK from GitHub"
    />
  </a>

  <a href="https://apt.izzysoft.de/fdroid/index/apk/com.gasperpintar.smokingtracker" target="_blank">
    <img
      src="assets/badges/en-US/izzyondroid.png"
      height="40px"
      alt="Download APK from IzzyOnDroid"
    />
  </a>

  <a href="https://www.openapk.net/smoking-tracker/com.gasperpintar.smokingtracker/" target="_blank">
    <img
      src="assets/badges/en-US/open_apk.png"
      height="40px"
      alt="Get it on OpenAPK"
    />
  </a>
  <a href="https://play.google.com/store/apps/details?id=com.gasperpintar.smokingtracker" target="_blank">
    <img
      src="assets/badges/en-US/google_play.png"
      height="40px"
      alt="Get it on Google Play"
    />
  </a>
</div>

[![OS](https://img.shields.io/badge/OS-Android%208%2B-lightgrey)](https://apilevels.com)
[![Preview release](https://img.shields.io/github/release/pintargasper/SmokingTracker.svg?maxAge=3600&include_prereleases&label=preview)](https://github.com/pintargasper/SmokingTracker/releases) 
[![Downloads](https://img.shields.io/github/downloads/pintargasper/smokingtracker/total?style=flat-square)](https://github.com/pintargasper/SmokingTracker/releases)
[![Translation status](https://translate.gasperpintar.com/widget/smokingtracker/svg-badge.svg)](https://translate.gasperpintar.com/engage/smokingtracker/?utm_source=widget)

</div>

## Kazalo vsebine
- [O aplikaciji](#-o-aplikaciji)
- [Podprti jeziki](#-podprti-jeziki)
- [Pomoč pri prevajanju](#-pomoč-pri-prevajanju)
- [Odvisnosti in različice](#-odvisnosti-in-različice)
- [Navodila za gradnjo](#-navodila-za-gradnjo)

## 🚀 O aplikaciji
**Sledilnik Kajenja** je enostavna aplikacija za sledenje kajenju, ki vam pomaga razumeti vaše navade in napredek pri opuščanju kajenja. Vsaka cigareta, ki jo pokadite, je jasno zabeležena, kar vam daje podroben vpogled v vaše dnevne, tedenske in mesečne vzorce

**Ključne lastnosti**
- **Lokalno shranjevanje podatkov** za večjo zasebnost
- **Dnevna, mesečna in letna** statistika z grafi
- **Preprosta analitika**, ki vam bo pomagala razumeti vaše navade
- **Samodejne varnostne kopije** (odvisno od naprave)
- **Večjezična podpora**: angleščina in slovenščina
- **Preprost in intuitiven** uporabniški vmesnik

## 🌐 Podprti jeziki

| Jezik            | Prevedeno |
|:-----------------|:----------|
| 🇺🇸 Angleščina    | [![Translation progress](https://translate.gasperpintar.com/widgets/smokingtracker/en/svg-badge.svg)](https://translate.gasperpintar.com/projects/smokingtracker/app/en) |
| 🇸🇮 Slovenščina   | [![Translation progress](https://translate.gasperpintar.com/widgets/smokingtracker/sl/svg-badge.svg)](https://translate.gasperpintar.com/projects/smokingtracker/app/sl) |
| 🇺🇦 Ukrajinščina  | [![Translation progress](https://translate.gasperpintar.com/widgets/smokingtracker/uk/svg-badge.svg)](https://translate.gasperpintar.com/projects/smokingtracker/app/uk) |

> Dodatni jeziki bodo dodani v prihodnjih izdajah

## 🌐 Pomoč pri prevajanju

<div align="center">
  <a href="https://translate.gasperpintar.com/engage/smokingtracker/?utm_source=widget" target="_blank">
    <img
      src="http://translate.gasperpintar.com/widget/smokingtracker/multi-auto.svg"
      width="500px"
      alt="Help translate"
    >
  </a>
</div>

## 📝 Odvisnosti in različice

**Vtičnik za Gradle**
- Vtičnik za Android Gradle: 9.1.0

**Knjižnice**
> Vse knjižnice so konfigurirane v [`libs.versions.toml`](gradle/libs.versions.toml)

## 📝 Navodila za gradnjo

### Koraki

1. **Kloniraj repozitorij**
```shell
git clone https://github.com/pintargasper/SmokingTracker.git
cd SmokingTracker
```

2. **Odprite projekt v Android Studiu**
- Izberite **Uvozi projekt (Gradle)** in počakajte, da se projekt sinhronizira
- Prepričajte se, da imate nastavljeno pravilno različico **JDK** in **Android SDK**

3. **Zgradite APK ali zaženite aplikacijo**
- Za gradnjo z odpravljanjem napak
```shell
./gradlew assembleDebug
```
- Za izdajo
```shell
./gradlew assembleRelease
```

4. **Zaženi na emulatorju ali napravi**
- V programu Android Studio izberite emulator ali priključite fizično napravo in kliknite **Zaženi**
