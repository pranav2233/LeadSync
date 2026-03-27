# LeadSync

LeadSync is an Android app for managers to track 1:1s with reportees and recurring conversations with stakeholders.

Repository: `https://github.com/pranav2233/LeadSync`

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
- `backend`: separate Kotlin + Spring Boot backend repo for auth + snapshot sync

## Open locally

1. Open the project in Android Studio.
2. Let Gradle sync and download dependencies.
3. Start the backend from `backend/README.md`.
4. Run the `app` configuration on an emulator or device with API 26+.

## Notes

- If you want, the next step can be adding editing/deleting flows, search/filtering, or calendar/reminder integrations.
