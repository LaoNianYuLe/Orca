# I Left Sidebar Workspace Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a ChatGPT-mobile-style left drawer to the Android app, with fixed work入口, real recent sessions, swipe/open/close behavior, and existing navigation preserved.

**Architecture:** Keep `AppNavigation` as the single owner of the existing `NavHost`. Add a Compose `AppShell` around it in `MainActivity` so the drawer is UI-only and can call the existing `NavController`. The drawer observes `ChatRepository.observeSessions()` directly, displays a bounded recent list, and never duplicates or mutates session data.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, Kotlin coroutines/Flow, existing Android unit-test setup.

---

### Task 1: Define drawer state and pure transition behavior

**Files:**
- Create: `src/android/app/src/main/java/com/i/app/ui/shell/SidebarState.kt`
- Test: `src/android/app/src/test/java/com/i/app/ui/shell/SidebarStateTest.kt`

- [ ] **Step 1: Write failing tests for open/close snap decisions**

```kotlin
@Test
fun `release past midpoint opens drawer`() {
    assertEquals(
        SidebarTarget.Open,
        SidebarState.targetAfterDrag(offset = 500f, width = 800f, velocity = 0f),
    )
}

@Test
fun `release before midpoint closes drawer`() {
    assertEquals(
        SidebarTarget.Closed,
        SidebarState.targetAfterDrag(offset = 200f, width = 800f, velocity = 0f),
    )
}

@Test
fun `fast right fling opens and fast left fling closes`() {
    assertEquals(SidebarTarget.Open, SidebarState.targetAfterDrag(80f, 800f, 1_200f))
    assertEquals(SidebarTarget.Closed, SidebarState.targetAfterDrag(720f, 800f, -1_200f))
}
```

- [ ] **Step 2: Run the focused test and verify it fails**

Run from `src/android`:

```bash
./gradlew :app:testDebugUnitTest --tests 'com.i.app.ui.shell.SidebarStateTest'
```

Expected: compilation failure because `SidebarState` and `SidebarTarget` do not exist.

- [ ] **Step 3: Implement the minimal pure state API**

Create a `SidebarTarget` enum with `Closed` and `Open`. Create `SidebarState.targetAfterDrag(offset, width, velocity)` using a velocity threshold of `1_000f` and otherwise opening at `offset >= width * 0.5f`. Clamp all caller-provided offsets later in the UI; this function only decides the final target.

- [ ] **Step 4: Run the focused test and verify it passes**

```bash
./gradlew :app:testDebugUnitTest --tests 'com.i.app.ui.shell.SidebarStateTest'
```

Expected: `BUILD SUCCESSFUL` and all three tests pass.

- [ ] **Step 5: Commit the state unit**

```bash
git add src/android/app/src/main/java/com/i/app/ui/shell/SidebarState.kt src/android/app/src/test/java/com/i/app/ui/shell/SidebarStateTest.kt
git commit -m "test: define sidebar snap behavior"
```

### Task 2: Build the drawer UI and recent-session presentation

**Files:**
- Create: `src/android/app/src/main/java/com/i/app/ui/shell/AppShell.kt`
- Create: `src/android/app/src/main/java/com/i/app/ui/shell/Sidebar.kt`
- Create: `src/android/app/src/main/java/com/i/app/ui/shell/SidebarSessionItem.kt`
- Modify: `src/android/app/src/main/res/values/strings.xml`

- [ ] **Step 1: Add localized labels required by the fixed information architecture**

Add string resources for `新会话`, `手机库`, `项目`, `技能`, `文案`, `最近`, and the drawer content description. Keep the resource names stable and use the English fallback for non-Chinese locales; do not replace existing settings/session strings.

- [ ] **Step 2: Implement `SidebarSessionItem` as a stateless row**

The row accepts `ChatSessionEntity` and `onClick: (String) -> Unit`, renders the title with `session.title?.takeIf { it.isNotBlank() } ?: stringResource(R.string.new_chat)`, truncates to one line, exposes a minimum 48dp touch target, and passes only the session id to the callback.

- [ ] **Step 3: Implement `Sidebar` with fixed entries and real recent sessions**

The composable accepts `sessions: List<ChatSessionEntity>`, `onNewChat`, `onStorage`, `onProjects`, `onSkills`, `onCopywriting`, `onSessionClick`, and `onClose`. Render the fixed entries in the exact order from the approved spec, then a divider, the `最近` label, and at most 20 sessions sorted by `updatedAt` descending. Use existing Material icons and `MaterialTheme.colorScheme`; do not add an icon dependency.

- [ ] **Step 4: Implement `AppShell` layout and drawer gestures**

Use a full-screen `Box` with the supplied content as the base layer, a scrim above it only while open/dragging, and the drawer above the scrim. Measure the root width with `onSizeChanged`; set drawer width to `coerceIn(320.dp.toPx(), rootWidth * 0.82f)` while respecting the available width. Track a pixel offset from `-drawerWidth` to `0f`, animate settle with `Animatable` and `tween(250)`, open on menu callback or a horizontal drag starting within 32dp of the left edge, close on a left drag over the drawer, and close on scrim tap. Shift the main content by at most 12dp while the drawer is open; do not scale it.

- [ ] **Step 5: Verify the new UI compiles in isolation**

```bash
./gradlew :app:compileDebugKotlin
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit the drawer UI**

```bash
git add src/android/app/src/main/java/com/i/app/ui/shell src/android/app/src/main/res/values/strings.xml
git commit -m "feat: add mobile left sidebar shell"
```

### Task 3: Integrate the shell with existing navigation

**Files:**
- Modify: `src/android/app/src/main/java/com/i/app/MainActivity.kt`
- Modify: `src/android/app/src/main/java/com/i/app/ui/navigation/AppNavigation.kt`

- [ ] **Step 1: Add shell callbacks at the existing `setContent` boundary**

Keep `rememberNavController()` and the existing `AppNavigation(...)` call in `MainActivity`. Wrap that call in `AppShell`, pass `app.chatRepository.observeSessions()` as the recent-session Flow, and pass navigation lambdas that use the existing `Routes` and `safeNavigate` functions.

- [ ] **Step 2: Map every approved drawer entry to an existing route**

Use `Routes.chat("__new__${UUID.randomUUID()}")` for 新会话 and 文案; 文案 additionally seeds a one-shot copywriting preset through a small UI-only pending action in `DeepLinkCoordinator` or the existing chat action mechanism. Use `Routes.FILE_BROWSER` for 手机库, `Routes.SESSION_LIST` for 项目 in the first version, and `Routes.SKILLS` for 技能. Recent rows navigate with `Routes.chat(sessionId)`. Every callback closes the drawer after navigation.

- [ ] **Step 3: Preserve existing deep-link and lifecycle behavior**

Do not alter `launchDeepLink`, `currentChatSessionId`, `SessionActivityTracker`, repository arguments, or the NavHost route declarations. The shell must wrap the NavHost without changing its start destination or back-stack restoration.

- [ ] **Step 4: Make back handling close the drawer first**

Use Compose `BackHandler(enabled = drawerIsOpen)` inside `AppShell` to settle the drawer closed and consume the event. When closed, do not install a handler so existing `ChatScreen` and activity back behavior remains unchanged.

- [ ] **Step 5: Add the menu entry to screens globally**

Because the shell is above the whole `AppNavigation`, its menu button remains available over chat, terminal, files, settings, and dialogs that do not replace the root activity content. Keep the button hit target at least 48dp and use the app’s `I` branding only where existing theme resources already provide it.

- [ ] **Step 6: Build the integrated debug APK**

```bash
./gradlew :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`; APK at `src/android/app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 7: Commit navigation integration**

```bash
git add src/android/app/src/main/java/com/i/app/MainActivity.kt src/android/app/src/main/java/com/i/app/ui/navigation/AppNavigation.kt
git commit -m "feat: connect sidebar to app navigation"
```

### Task 4: Verify behavior and prevent regressions

**Files:**
- Modify: `src/android/app/src/test/java/com/i/app/ui/shell/SidebarStateTest.kt` if additional pure cases are needed
- Inspect: `src/android/app/src/main/java/com/i/app/ui/shell/AppShell.kt`

- [ ] **Step 1: Run all existing JVM tests**

```bash
./gradlew :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL` with no existing test regressions.

- [ ] **Step 2: Run static checks relevant to changed Kotlin files**

```bash
./gradlew :app:compileDebugKotlin :app:lintDebug
```

Expected: compilation succeeds; any pre-existing lint warnings are recorded without changing unrelated code.

- [ ] **Step 3: Inspect the final diff for scope and whitespace**

```bash
git diff --check
git diff --stat HEAD~3..HEAD
```

Expected: only the sidebar shell, navigation wiring, localized labels, tests, and the plan commits are present; no Room schema, provider, agent, sandbox, or package-name changes are introduced by this feature.

- [ ] **Step 4: Perform a manual acceptance pass on an installed debug APK**

Verify: default closed state; menu opens; left-edge swipe opens; drawer left swipe and scrim tap close; width is approximately 82%; fixed entries are ordered correctly; recent sessions are real and newest-first; tapping a session opens the right chat; New Chat creates a draft route; terminal/files/settings still show the menu; back closes the drawer before navigating back.

- [ ] **Step 5: Report the APK path and verification results**

```text
APK: /Users/mars/Documents/图片/OpenMinis/src/android/app/build/outputs/apk/debug/app-debug.apk
Build: :app:assembleDebug PASS
JVM tests: :app:testDebugUnitTest PASS
```

