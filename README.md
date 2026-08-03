# SubSync - Smart Subscription Manager for Android 📱✨

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-blue.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-purple.svg)](https://developer.android.com/jetpack/compose)
[![Material You](https://img.shields.io/badge/Design-Material%203-pink.svg)](https://m3.material.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**SubSync** is a modern, privacy-first Android application designed to track, manage, and optimize all your recurring subscriptions automatically through mobile bank SMS alerts, digital email receipts, and manual entries.

Built with **Jetpack Compose**, **Material You (Material 3) Dynamic Design**, and an **offline-first Room architecture**.

---

## 🌟 Key Features

### 1. 🤖 Smart SMS & Email Auto-Detection
- **Automated Bank SMS Parser**: Detects recurring payment debits for popular services (**Netflix**, **Spotify**, **Amazon Prime**, **YouTube Premium**, **Disney+ Hotstar**, **Apple**, **ChatGPT Plus**, **Airtel/Jio Broadband**, and more).
- **Email Receipt Parser**: Extracts digital invoice details from receipts (Google Play, Apple In-App Purchases, AWS, Zoom, Adobe, etc.).
- **Privacy First**: All SMS and email receipt parsing happens 100% locally on your device — no financial data is ever uploaded to remote servers.

### 2. 🔄 Smart Deduplication & Payment History
- **No Duplicate Subscriptions**: Automatically groups multiple payment alerts under a single subscription entry (e.g., *Netflix*).
- **Monthly Charge Log**: View historical payment logs through the months with exact dates, amounts, and bank SMS snippets.
- **Future Renewal Timeline**: Visual 4-cycle upcoming billing timeline for every subscription.

### 3. 🎨 Material You (Material 3) UI Interface
- **Dynamic Color Schemes**: Automatically adapts to device wallpaper colors on Android 12+ (`dynamicLightColorScheme` / `dynamicDarkColorScheme`).
- **Interactive Dashboard**: Hero gradient spending cards, active plan counts, category chips, and due-soon renewal alerts.
- **Spending Analytics**: Breakdown of monthly commitment totals, projected annual cost, and category spend distribution progress indicators.
- **Modal Bottom Sheets**: Smooth slide-up sheets for adding/editing subscriptions and inspecting payment histories.

### 4. 🔔 Local Renewal Reminders
- Scheduled local push notifications powered by Android `AlarmManager` and `NotificationManager` to alert you before subscriptions auto-renew.

---

## 🛠️ Architecture & Tech Stack

- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose + Material 3
- **Architecture**: Clean Architecture / MVVM (Model-View-ViewModel) + Repository Pattern
- **Local Database**: Room 2.6 (`kapt` annotation processor)
- **Asynchronous / Reactive**: Kotlin Coroutines + `StateFlow` / `Flow`
- **Navigation**: Jetpack Navigation Compose
- **Target SDK**: Android 14 (API 34) | **Min SDK**: Android 8.0 (API 26)

---

## 📂 Project Structure

```
SubManager/
├── app/
│   ├── src/main/
│   │   ├── java/com/submanager/app/
│   │   │   ├── MainActivity.kt
│   │   │   ├── data/
│   │   │   │   ├── local/          # Room Entities (Subscription & PaymentHistory), DAOs, Database
│   │   │   │   ├── model/          # Subscription Enums & Data Models
│   │   │   │   └── repository/     # SubscriptionRepository
│   │   │   ├── engine/             # SmsParserEngine, EmailReceiptParserEngine, SmsReceiver
│   │   │   ├── notification/       # NotificationHelper & RenewalReminderReceiver
│   │   │   ├── ui/
│   │   │   │   ├── theme/          # Material 3 Color, Type, and Dynamic Theme
│   │   │   │   ├── viewmodel/      # SubscriptionViewModel
│   │   │   │   └── screens/        # Compose UI Screens (Dashboard, Subscriptions, Detector, Analytics, Settings, Sheets)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
    └── libs.versions.toml
```

---

## 🚀 Building & Running

### Prerequisites
- **Android Studio** (Koala / Ladybug or newer recommended)
- **JDK 17** or **JDK 21** (Set Gradle JDK to *Embedded JDK* in Android Studio Settings)

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/cyberlog69/sub-manager.git
   cd sub-manager
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle files (**File ➔ Sync Project with Gradle Files**).
4. Select target device / emulator and click **Run ▶** (`Shift + F10`).

### Running Unit Tests
To verify the SMS parsing and merchant detection logic:
```bash
./gradlew test
```

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
