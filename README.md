# Family Medicine

A simple, family-first Android app that makes sure everyone at home takes their medicine on time. It rings a real alarm, takes over the phone screen at medicine time, and lets the caregivers see everyone's progress live.

Built with plain HTML, CSS, and JavaScript, wrapped as an Android app with [Capacitor](https://capacitorjs.com/). Live sync between phones uses Firebase Firestore. The APK is built automatically by GitHub Actions.

---

## Features

- **Full-screen MEDICINE TIME alert** that wakes the phone and shows over the lock screen.
- **Loud alarm** that rings until you tap the screen, set separately for each family member.
- **Three clear answers:** Already taken, Taking, or Forgot (counts as a missed dose).
- **Misclick protection:** every answer asks for confirmation, buttons unlock only 1 hour before a dose, and **Change answer** lets you undo a mistake.
- **Per-phone alert settings:** one phone can turn alerts off for some members and keep them for others.
- **Live family tracking:** every phone sees what each member has taken in real time.
- **Reports:** 14-day accuracy ring, on-time percentage, forgot count, streak badges, and a daily bar chart for each member.
- **Missed-dose alert to another phone:** turn on "Alert me if missed by 45 min" for a member in the Alerts tab. If the dose isn't answered, that phone rings and shows a full-screen alert.
- **Alarms survive restarts** and the Alerts tab shows when the next alarm is due.
- **Overdue banner** for doses unanswered for 45+ minutes, and a **Next dose** countdown.
- **Refill tracker:** tablet counts go down as doses are taken, with a red "refill soon" warning at 5 days left.
- **Flexible schedules:** daily medicines or every N days (for example, every 15 days).
- **Add or dismiss** medicines and family members at any time. History is kept.
- **Daily rollover at 12:00 AM IST** with a sync to the cloud.

---

## Project structure

```
FamilyMedicine/
├── www/
│   ├── index.html            # The whole app: design, logic, medicine list
│   └── firebase-config.js    # Your Firebase settings (the only file with secrets)
├── native/                   # Android alarm code (alarm, full-screen alert, permissions)
│   ├── AlarmPlugin.java
│   ├── AlarmReceiver.java
│   └── MainActivity.java
├── .github/workflows/
│   └── build.yml             # Builds the APK on every push
├── capacitor.config.json
└── package.json
```

---

## Setup

### 1. Firebase (live sync)

1. Create a project at [console.firebase.google.com](https://console.firebase.google.com).
2. Go to **Build → Firestore Database → Create database** and choose a nearby location.
3. In the **Rules** tab, publish:
   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{db}/documents {
       match /family/{code} {
         allow get, create, update: if true;
       }
     }
   }
   ```
4. Open **Project settings**, add a **Web app** (`</>`), and copy the config values.
5. Open `www/firebase-config.js`, replace `null`, and set a long random `window.FAMILY` code (same on every phone):
   ```js
   window.FB={apiKey:"...",authDomain:"...",projectId:"...",storageBucket:"...",messagingSenderId:"...",appId:"..."};
   ```

> Keep this repository **private**. The family code works like a secret address for your data, and the rules block listing other documents. Anyone who has both your config and your family code could still read the data.

### 2. Build the APK

1. Commit and push the project to GitHub.
2. Open the **Actions** tab. The **Build APK** workflow runs automatically (or choose **Run workflow**).
3. When it shows a green tick (about 5 to 8 minutes), open the run and download **FamilyMedicine-APK** from **Artifacts**.
4. Unzip it to get `app-debug.apk`.

### 3. Install on each phone

1. Send `app-debug.apk` to the phone and open it. Allow **Install unknown apps** if asked.
2. Open the app and allow notifications.
3. Go to the **Alerts** tab and tap **Allow** on each permission: exact alarms, full-screen alerts, and battery unrestricted.
4. Tap **Test alarm in 10 seconds**, lock the screen, and check that the phone wakes and rings.
5. Confirm the Alerts tab shows **Live sync: ON**.

---

## Using the app

| Tab | What it does |
| --- | --- |
| **Today** | Everyone's doses for the day, with status, next dose, and overdue warnings |
| **Report** | Accuracy, on-time rate, streaks, and forgotten doses over 14 days |
| **Medicines** | Add or dismiss medicines, add members, and update tablet counts |
| **Alerts** | Permissions, test alarm, and per-member reminder and alarm settings |

---

## Making changes

Edit the file, then commit, push, and download the new APK from the Actions tab. Installing it over the old app keeps all data.

| To change | Edit |
| --- | --- |
| Look, wording, or the default medicine list | `www/index.html` |
| Firebase connection | `www/firebase-config.js` |
| Alarm behaviour or permissions | `native/` and `.github/workflows/build.yml` |

Default medicines are defined in the `seed()` function in `www/index.html`. They are only used the first time the app starts on a phone. After that, manage medicines from the **Medicines** tab.

---

## Known limits

- Alarms re-arm after a restart on their own, but open the app once if the phone was off for days.
- Some brands (Xiaomi, Oppo, Vivo, Realme) have extra autostart or lock-screen permissions that may need turning on.
- Refill counts assume one tablet per dose and only change when a dose is confirmed in the app.
- The APK is a debug build for private family use, not a Play Store release. It is signed with the key in `native/family.keystore`, so updates install over the old app.

---

## Disclaimer

This app is a reminder tool, not medical advice. Always follow the doctor's instructions for medicines and doses.
