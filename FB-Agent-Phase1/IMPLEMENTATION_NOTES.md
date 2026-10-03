# Implementation review — v1.1.0

Addressed the reported structural issues:

1. Dynamic Facebook UI: no static Facebook IDs/classes; recursive text/content-description extraction.
2. Null safety: root/event/package guards and child-node try/finally recycling.
3. Threading: decision work uses coroutines off the main thread; Room inserts are suspend and executed from the service coroutine.
4. Overlay permission: checked before service start and again inside the service; BadToken/SecurityException are caught.
5. Manifest: INTERNET, SYSTEM_ALERT_WINDOW, FOREGROUND_SERVICE, FOREGROUND_SERVICE_SPECIAL_USE and accessibility binding are declared.
6. Infinite repeat protection: normalized SHA-256 content fingerprint, decision cooldown, and last-action fingerprint.
7. Popup/refresh resilience: null roots and RuntimeExceptions during transient accessibility-tree changes are ignored rather than crashing the service.
8. Room buildability: room-compiler + kapt configured.
9. Facebook scope: accessibility service declares com.facebook.katana and com.facebook.lite only.
10. Gesture variation is retained only for smooth interaction; it is not presented as anti-bot bypass.

Gemini networking is intentionally kept behind the DecisionEngine boundary in this build; no API key is embedded in the APK.
