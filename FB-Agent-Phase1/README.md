# FB-Agent Phase 1 — v1.1.0

Facebook-only Android Accessibility prototype with safer node handling and threading.

## Fixed in v1.1.0
- No hardcoded Facebook view IDs/classes.
- Recursive accessibility-tree parsing with null/error guards.
- Child nodes are recycled after use; rootInActiveWindow is not recycled by the app.
- Decision processing runs off the main thread.
- Room DAO uses suspend APIs and Room compiler is configured with kapt.
- Overlay permission is checked before starting the floating service, with runtime exception guards.
- Accessibility service is explicitly limited to Facebook and Facebook Lite via packageNames.
- Duplicate-content fingerprint + cooldown prevents repeated swipe loops.
- Accessibility interruptions cancel pending gestures.
- Floating overlay handles missing permission/window errors without crashing.
- Dry Run remains the default safety mode.

## Build
Open the `FB-Agent-Phase1` folder in Android Studio, use JDK 17, sync Gradle, then Build > Build APK(s).

## Important limitation
The project does not claim to bypass Facebook anti-bot detection or guarantee zero account flags. Gesture variation is for interaction smoothness only.
