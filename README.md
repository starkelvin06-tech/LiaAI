# Lia AI (com.Lia.assistant)

> **Voice-First Personal AI Companion for Android**  
> Built with Kotlin, Jetpack Compose, Material3, and the Gemini Live API.

---

## 🌟 Flavors

Lia AI builds two distinct flavor variants from the single codebase:

| Variant | Distribution | Permissions & Capabilities |
| :--- | :--- | :--- |
| **`direct`** | Website / Sideload | Full Accessibility screen control (`NovaAccessibilityService`), auto-send messages via WhatsApp/SMS, UI agents, Website Forge. |
| **`play`** | Google Play Store | All Accessibility permissions stripped at compile time. Opens standard SMS / WhatsApp composer drafts for user review. |

---

## 📱 How to Build and Install on Your Phone

### Option A: Automatic Build via GitHub Actions (Recommended for Termux)

1. **Create a new repository** on [github.com/new](https://github.com/new) (e.g. `LiaAI`).
2. **Push this project** from Termux:
   ```bash
   cd ~/projects/LiaAI
   git remote add origin https://github.com/<YOUR_USERNAME>/LiaAI.git
   git branch -M main
   git push -u origin main
   ```
3. Go to the **Actions** tab on GitHub:
   - The **Build Lia AI APKs** workflow runs automatically on Ubuntu CI.
   - It compiles `./gradlew assembleDirectDebug assemblePlayDebug` and runs all unit tests.
4. When finished (typically 2-3 minutes), download the artifacts:
   - `LiaAI-direct-debug` (contains `app-direct-debug.apk`)
   - `LiaAI-play-debug` (contains `app-play-debug.apk`)
5. Open the downloaded `.apk` on your Android device to install!

---

### Option B: Build with Android Studio on PC/Mac/Linux

1. Clone or copy `~/projects/LiaAI` to your computer.
2. Open the project in **Android Studio Ladybug** (or newer).
3. Select Build Variant in the lower-left toolbar:
   - `directDebug` or `playDebug`
4. Run or select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

---

## ⚡ First Time App Setup

1. Open **Lia AI** on your phone.
2. Tap the **Settings** icon (top right).
3. Paste your Gemini API key from [Google AI Studio](https://aistudio.google.com).
4. Tap **Save Key**.
5. Choose from **10 Personalities** (Normal, Friendly, Professional, Funny, Companion, Companion+, Teacher, Developer, Foodie, Nautanki).
6. Return to Home and tap **Start Voice Mode**!
