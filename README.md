# M.J. JARVIS V1.3.3

Android voice-assistant starter for Vivo V80 (Android 15). Includes an editable/savable/resettable English greeting, tap-to-talk speech recognition and text-to-speech, installed-app discovery and launch, WhatsApp and YouTube launch, Wi-Fi/Bluetooth/alarm settings, and dialer launch for a spoken number.

## V1.3.3 screen-control foundation
- Adds an optional, user-enabled Android AccessibilityService.
- Supports explicit screen actions: back, home, recents, scroll up/down, and tap a visible text label.
- The user must enable **M.J. JARVIS Screen Control** manually in Android Accessibility settings.
- Accessibility is used only to perform the explicit command received; the service does not independently listen to audio.

APK build: GitHub Actions runs on push to main. Open Actions, choose Android APK Build, and download the MJ-JARVIS-V1.3.3-debug artifact after success.

Not implemented yet: always-on wake word, background listening, floating voice control while another app is foregrounded, scheduled WhatsApp sending, direct calling, and complete Gujarati/Hindi/English command handling. Call opens the dialer only; sensitive actions remain user-controlled.
