# Project and Copywriting Workspaces Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace the placeholder sidebar destinations for “项目” and “文案” with dedicated, usable workspaces.

**Architecture:** Add two lightweight Compose screens backed by the existing `FolderEntity` and `ChatSessionEntity` data. Projects use existing session folders as project records; copywriting uses a dedicated workspace with template actions and writing-category history, while opening a chat through the existing navigation path.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Navigation Compose, Room repositories, JUnit.

---

### Task 1: Define testable workspace behavior

**Files:**
- Create: `src/android/app/src/test/java/com/i/app/ui/workspace/WorkspaceCatalogTest.kt`
- Create: `src/android/app/src/main/java/com/i/app/ui/workspace/WorkspaceCatalog.kt`

- [x] Add tests for the copywriting catalog: it must contain stable Chinese templates for short-video copy, social post, product description, and article outline; every template must have a nonblank prompt.
- [x] Run the focused test and confirm it fails because the catalog does not exist.
- [x] Implement the immutable catalog and rerun the focused test until it passes.

### Task 2: Build project workspace screen

**Files:**
- Create: `src/android/app/src/main/java/com/i/app/ui/workspace/WorkspaceScreens.kt`
- Modify: `src/android/app/src/main/res/values/strings.xml`

- [x] Render a Material 3 top bar titled “项目” with the global sidebar and an add button.
- [x] Observe folders and sessions from `ChatRepository`, show project cards with name, description, and session count.
- [x] Add a create-project dialog that calls `ChatRepository.createFolder` and updates immediately through the existing Flow.
- [x] Show an empty state explaining that a project can organize related conversations.
- [x] Render the copywriting workspace in the same file with a top bar, template cards, writing-history sessions, and a clear template action.

### Task 3: Wire real navigation and actions

**Files:**
- Modify: `src/android/app/src/main/java/com/i/app/ui/navigation/AppNavigation.kt`
- Modify: `src/android/app/src/main/java/com/i/app/MainActivity.kt`

- [x] Add `PROJECTS` and `COPYWRITING` routes and register both screens in the NavHost.
- [x] Route the sidebar “项目” action to `PROJECTS` instead of `SESSION_LIST`.
- [x] Route the sidebar “文案” action to `COPYWRITING` instead of directly creating an ordinary chat.
- [x] Make template and new-copywriting actions open a new chat with a writing-focused prompt prefilled, without changing normal chat behavior.

### Task 4: Verify

**Files:**
- No additional files.

- [x] Run the focused workspace unit test.
- [x] Run `:app:assembleDebug`.
- [x] Install the APK on the existing emulator and inspect both sidebar destinations.
- [x] Confirm the normal chat route and composer still build and open; model selection remains unchanged.
