# LeadSync

LeadSync is an Android app for managers to track 1:1s with reportees and recurring conversations with stakeholders.

Repository: `https://github.com/pranav2233/LeadSync`
Backend: `https://github.com/pranav2233/LeadSync-backend`

## What it includes

- MVVM architecture with Jetpack Compose UI
- Material 3 components for dashboard, people, detail, and form screens
- Room persistence for:
  - people
  - dated interaction occurrences
  - action items tied to each interaction
- Separate capture areas for:
  - agenda/context
  - progress updates
  - feedback delivered
  - follow-up action items

## Core screens

- Dashboard: people count, open actions, due-this-week actions, recent interactions, feedback highlights
- People: add and browse reportees or stakeholders
- Person detail: occurrence-wise interaction timeline for each individual
- Generic interaction log: one reusable form to save dated 1:1 or stakeholder conversations

## Project structure

- `app/src/main/java/com/example/leadsync/data`: Room entities, DAOs, repository
- `app/src/main/java/com/example/leadsync/sync`: cloud auth/session/snapshot sync client
- `app/src/main/java/com/example/leadsync/ui`: navigation and ViewModels
- `app/src/main/java/com/example/leadsync/ui/screens`: Compose screens and reusable form components
- `app/src/main/java/com/example/leadsync/ui/theme`: colors and typography

## Run locally

1. Clone and run the backend repo:

```bash
git clone https://github.com/pranav2233/LeadSync-backend.git
cd LeadSync-backend
./gradlew bootRun
```

2. Open this Android app project in Android Studio.
3. Let Gradle sync and download dependencies.
4. Run the `app` configuration on an emulator or device with API 26+.
5. In the login screen, use:

```text
http://10.0.2.2:8000
```

For a physical device, replace `10.0.2.2` with your computer's LAN IP.

## Cloud Sync

- The app supports email/password login against the backend.
- Sync is currently manual and snapshot-based:
  - `Pull cloud` restores the backend snapshot to the device
  - `Push cloud` uploads the current device data to the backend

## Notes

- If you want, the next step can be adding editing/deleting flows, search/filtering, or calendar/reminder integrations.
