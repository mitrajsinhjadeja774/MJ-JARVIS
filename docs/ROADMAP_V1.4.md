# M.J. JARVIS — V1.4+ Product Roadmap

This document records the requested product direction. Features below are planned, not claims about the current APK.

## Product goal
A personal, voice-first assistant for the user's Vivo V80 that can perform the broadest practical set of phone tasks hands-free, while respecting Android permissions, app boundaries, privacy, and user safety.

## V1.4 — Foundation and reliability
- Fix and verify the existing V1.3.3 Android build before adding risky features.
- Preserve the editable English greeting and explicit accessibility actions.
- Improve command parsing and app discovery; add clear spoken feedback when Android blocks an action.
- Add a permission/status screen explaining microphone, accessibility, notifications, camera, and overlay access.

## Personal voice security
- Add an explicit owner voice-enrollment flow with several spoken samples.
- Evaluate speaker verification locally where feasible; reject low-confidence or unknown speakers.
- Treat speech-to-text recognition and speaker identity as separate checks.
- Provide a safe fallback for noisy environments and never claim voice biometrics are perfect authentication.
- Require device authentication or explicit confirmation for sensitive actions (payments, passwords, security settings, destructive actions).

## Hands-free operation
- Investigate an Android-compliant foreground microphone service and wake-word engine.
- Support screen-off listening only while the phone is powered on and Android/OEM background-microphone rules allow it.
- Add a floating assistant only with user-granted overlay permission.
- Clearly expose listening state, mute/stop controls, battery impact, and privacy status.

## Phone and app control
- Expand supported app launching and accessibility actions incrementally.
- Use Android intents and official APIs first; use AccessibilityService only for user-authorized visible UI interactions.
- Do not promise universal control: protected screens, banking apps, permission dialogs, and OS-restricted actions may remain unavailable.

## Camera assistant
- Add a camera preview and photo/video capture flow with runtime permission.
- Add framing, angle, lighting, portrait/landscape, and low-light guidance.
- Explore on-device image analysis; clearly label suggestions as guidance, not guaranteed professional results.

## Calls and notifications
- Explore notification-based missed-call summaries and supported call-screening APIs.
- Do not promise arbitrary auto-answering or injecting JARVIS speech into cellular call audio; Android, carrier, and default-dialer restrictions apply.
- Keep call handling opt-in and transparent.

## AI question answering and internet
- Add an AI question-answering layer with a secure backend or supported provider integration.
- Never embed private API keys in the APK.
- Distinguish offline capabilities from internet-dependent capabilities and report failures clearly.

## Project Mode — Smart Replica Builder
- Accept a project idea and clarify its intended use, skill level, budget, and safety constraints.
- Produce a bill of materials down to small components, dimensions, tools, estimated costs, and sourcing guidance.
- Generate or display interactive 3D models, exploded views, assembly sequences, and step-by-step instructions where supported.
- Provide relevant tutorial videos or generated instructional clips only when the video-generation capability is available.
- Track build progress and update the next steps.
- For fictional designs such as an Iron Man-inspired suit, focus on safe cosplay/prop construction (foam, plastics, LEDs, low-voltage electronics); do not provide dangerous weapon, hazardous propulsion, or unsafe powered-exoskeleton instructions.

## MJ HOLO-DESIGN — interactive 3D/4D experience
- Target a rotatable, zoomable 3D model with exploded-part views and animated assembly steps.
- Explain each component, its purpose, dimensions, material, and connection method.
- Use “4D” to mean time-based animation and user interaction in the app; a true free-floating hologram requires separate hardware and is not implied.
- Prefer a mobile-friendly 3D viewer and staged implementation; do not claim movie-grade holographic hardware effects on a phone screen.

## Delivery and verification
- Every release must have a successful GitHub Actions build before an APK is called ready.
- Test install, permissions, voice commands, and accessibility behavior on the Vivo V80.
- Record known limitations and provide the exact successful APK artifact link only after verifying its availability.
