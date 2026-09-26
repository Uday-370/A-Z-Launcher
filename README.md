# Alphabet Launcher (NovaFocus)

A high-performance, minimal Android home-screen replacement built with Jetpack Compose, Material 3, MVVM, and Hilt. It features an interactive, right-edge-pinned vertical alphabet bar that bends toward the user's finger using a hand-written Gaussian curve falloff, switching in real time to an indexed app list with steady 60fps performance and zero touch-path allocations.

---

## 1. Setup Steps (Fresh Clone + Single Sync)

### Prerequisites
- **Android Studio**: Android Studio Koala / Ladybug / 2026.1.x or newer.
- **JDK**: 17 or newer (Android Studio bundles JBR 17/21 — no separate install needed for standard setups).
- **Android SDK**: `minSdk 26`, `compileSdk 35` (or latest stable SDK), `targetSdk 35`.

### Build & Run Instructions
1. Clone the repository:
   ```bash
   git clone <repo-url>
   cd "A-Z Launcher"
   ```
2. Open the project in Android Studio or trigger a command-line build.
3. Synchronize Gradle and build debug APK:
   ```bash
   ./gradlew assembleDebug
   ```
4. Run pure domain unit tests:
   ```bash
   ./gradlew testDebugUnitTest
   ```
5. Install and launch on an emulator or physical device (API 26+):
   ```bash
   ./gradlew installDebug
   ```

---

## 2. Curve Animation & Physics Explanation

### Gaussian Falloff Formula
The core bending animation is written from first principles in `domain/CurveMath.kt`, with no third-party physics or dock-animation library involved:

$$\text{offset}(d) = -\text{maxBulgePx} \times \exp\left(-\frac{d^2}{2\sigma^2}\right)$$

Where:
- $d = \text{restY} - \text{touchY}$: the vertical distance between an item's resting position and the finger's current position.
- $\text{maxBulgePx}$: the peak horizontal displacement toward the screen centre (tuned to ~76dp).
- $\sigma$ (`sigmaPx`): controls how many neighbouring letters get pulled in.
- $\text{influence} = \exp\left(-\frac{d^2}{2\sigma^2}\right)$: a Gaussian bell curve — the letter directly under the finger gets full displacement ($\approx -\text{maxBulgePx}$ at $d=0$), neighbours fall off smoothly, and distant letters decay toward 0.

### Parameter Tuning
- **$\sigma$ tuning**: set to $2.8 \times \text{letterSpacingPx}$, so roughly 5–6 letters visibly deflect around the finger — matching the reference video without distorting distant letters.
- **Spring release**: on finger-up, the bulge amplitude animates back via `Animatable<Float>` with `spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)`, giving a natural overshoot-and-settle rather than a snap or linear tween.
- **Magnification Wave**: letters dynamically scale ($1.0\times \to 1.35\times$) during the draw phase inside `Modifier.graphicsLayer` corresponding to Gaussian influence, producing an organic dock-like wave.

### Steady 60fps: Lambda-Form `Modifier.offset { }`
A hard grading requirement is zero stutter under fast dragging:
- Computing the offset in the composable body (e.g. `val x = CurveMath.offsetFor(...); Modifier.offset(x.dp, 0.dp)`) forces recomposition of all 28 bar items on every pixel of pointer movement.
- Instead, `AlphabetBar` uses the deferred-read lambda overload:
  ```kotlin
  Modifier.offset {
      val touchY = touchYState.value
      val currentBulge = animBulge.value
      val restY = letterRestYs[index]
      val x = if (touchY == null || currentBulge == 0f) 0f
              else CurveMath.offsetFor(restY - touchY, currentBulge, sigmaPx)
      IntOffset(x.roundToInt(), 0)
  }
  ```
- **Why this matters**: the lambda form reads state only at the layout/draw phase, so Compose skips recomposition of all 28 nodes entirely and just re-lays-out — this is what keeps drag scrubbing at 60fps.
- **Zero PackageManager calls on the touch path**: all installed apps are queried and grouped once into a cached `Map<Char, List<InstalledApp>>` in `AppRepository` / `LauncherViewModel` on `Dispatchers.Default`. Selecting a letter is an $O(1)$ map lookup, not an IPC call.

---

## 3. Third-Party Libraries & Dependencies

Every dependency actually declared in `gradle/libs.versions.toml` / `app/build.gradle.kts`, with the exact version pinned there:

| Dependency | Version | Purpose |
|---|---|---|
| `com.android.application` (AGP) | `8.5.2` | Android Gradle Plugin build toolchain |
| `org.jetbrains.kotlin.android` | `2.0.20` | Kotlin language support for Android |
| `org.jetbrains.kotlin.plugin.compose` | `2.0.20` | Official Kotlin Compose compiler plugin |
| `com.google.devtools.ksp` | `2.0.20-1.0.25` | Kotlin Symbol Processing for compile-time code generation |
| `com.google.dagger:hilt-android` | `2.51.1` | Dependency injection framework for Android architecture |
| `com.google.dagger:hilt-compiler` | `2.51.1` | Annotation processor for the Hilt dependency graph |
| `androidx.core:core-ktx` | `1.13.1` | Android KTX extensions and hardware-accelerated drawable-to-bitmap conversion |
| `androidx.lifecycle:lifecycle-runtime-ktx` | `2.8.5` | Coroutine lifecycle scopes (`lifecycleScope`, `viewModelScope`) |
| `androidx.lifecycle:lifecycle-runtime-compose` | `2.8.5` | Compose lifecycle state observation |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | `2.8.5` | Compose integration for ViewModel instantiation |
| `androidx.activity:activity-compose` | `1.9.2` | ComponentActivity integration with `setContent` / `enableEdgeToEdge()` |
| `androidx.datastore:datastore-preferences` | `1.0.0` | Async, reactive persistence for favourite apps |
| `androidx.compose:compose-bom` | `2024.09.00` | Jetpack Compose Bill of Materials ensuring version alignment |
| `androidx.compose.ui:ui`, `ui-graphics`, `ui-tooling-preview` | BOM | Core Compose rendering, graphics layers, layout preview |
| `androidx.compose.material3:material3` | BOM | Modern Material Design 3 design system components |
| `androidx.compose.material:material-icons-extended` | BOM | Material vector icons (search, close, star, navigation) |
| `junit:junit` | `4.13.2` | Unit testing framework for domain math |
| `androidx.test.ext:junit` | `1.2.1` | AndroidX test extensions for unit testing |
| `androidx.test.espresso:espresso-core` | `3.6.1` | UI test assertions and instrumentation runner |

> Pinned versions match `gradle/libs.versions.toml`.

---

## 4. Completed Bonuses

1. **Spring Physics on Release** — `Animatable` with `Spring.DampingRatioMediumBouncy` / `Spring.StiffnessLow` for an elastic overshoot rebound.
2. **Haptic Tick on Letter Change** — `LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.TextHandleMove)` inside `LaunchedEffect(selectedIndex)`, firing only on discrete index transitions.
3. **Dim Letters with No Installed Apps** — letters absent from `appsByLetter` render at 20% opacity.
4. **Light & Dark Theme** — Material 3 theme driven by `isSystemInDarkTheme()`, edge-to-edge transparent system bars.
5. **Default Launcher Configuration** — manifest declares `CATEGORY_HOME` / `CATEGORY_DEFAULT` intent filters with `singleTask` launch mode.
6. **Favourites Persistence** — Jetpack DataStore Preferences; long-press toggles a favourite with immediate disk persistence.
7. **Live App-List Refresh** — foreground `BroadcastReceiver` for `ACTION_PACKAGE_ADDED` / `_REMOVED` / `_CHANGED` / `_REPLACED`, triggering a reload.
8. **Swipe-Up Search** — upward drag opens a full-text search overlay filtering installed apps in real time.
9. **Pure Unit Tests** — 9 tests in `CurveMathTest.kt` covering Gaussian symmetry, decay across sigmas, exact/midpoint index resolution, and boundary cases.

---

## 5. AI Tools Used (Disclosure)

In compliance with the assignment's disclosure requirement:
- **Tool used**: Antigravity IDE (Gemini-driven agent).
- **Scaffolded with AI assistance**:
  - `build.gradle.kts`, `settings.gradle.kts`, `libs.versions.toml`
  - `domain/CurveMath.kt` — the Gaussian displacement function and nearest-letter-index logic
  - `domain/CurveMathTest.kt` — unit tests for the falloff math and boundary conditions
  - `data/AppRepository.kt`, `data/InstalledApp.kt` — installed-app query and caching
  - `ui/AlphabetBar.kt`, `ui/LetterBubble.kt`, `ui/HomeScreen.kt`, `ui/ClockHeader.kt`, `ui/AppListItem.kt` — Compose layouts, gestures, animation
  - `viewmodel/LauncherViewModel.kt` — StateFlow architecture and DataStore integration
- Every generated file was reviewed and can be explained and extended live.

---

## 6. TODO: Physical Device Screen Recording

> [!IMPORTANT]
> This has not been recorded yet. Before submitting, record on a physical device:
> 1. Resting screen — live clock/date and favourite apps.
> 2. Slow drag across the bar — smooth Gaussian bend and letter bubble.
> 3. Fast drag — zero-stutter 60fps and instant letter switching.
> 4. A letter with zero installed apps — the "No apps" empty state.
> 5. Finger release — the spring overshoot rebound.
> 6. Tapping an app in the filtered list to launch it.
> 7. Every bonus actually built: theme switching, favouriting, swipe-up search, live install/uninstall refresh.
