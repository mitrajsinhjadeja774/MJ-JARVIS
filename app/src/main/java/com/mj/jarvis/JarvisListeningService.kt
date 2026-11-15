package com.mj.jarvis

import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.os.*
import android.speech.*
import android.util.Log
import java.util.Locale

/**
 * Explicit one-shot background commands. Never restarts recognition automatically.
 * The notification stays visible; Privacy ON removes it and stops the service.
 */
class JarvisListeningService : Service() {
 private var recognizer: SpeechRecognizer? = null
 private var active = false
 private var listening = false
 private val channelId = "jarvis_listening"
 private val notificationId = 42
 private val handler = Handler(Looper.getMainLooper())
 private var lastStatus = "Idle. Tap Speak to give a command."
 private val timeout = Runnable {
  if (listening) {
   Log.w("JarvisVoice", "Speech recognition timed out after 10 seconds")
   finishListening("Timed out. Tap Speak to retry.")
  }
 }

 override fun onCreate() {
  super.onCreate()
  (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
   NotificationChannel(channelId, "JARVIS voice control", NotificationManager.IMPORTANCE_LOW))
 }

 override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
  if (intent?.action == ACTION_STOP) {
   stopSession()
   return START_NOT_STICKY
  }
  if (!active) {
   active = true
   if (Build.VERSION.SDK_INT >= 29)
    startForeground(notificationId, notification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
   else startForeground(notificationId, notification())
  }
  if (intent?.action == ACTION_SPEAK) listenOnce()
  return START_NOT_STICKY
 }

 private fun notification(): Notification {
  val stop = Intent(this, JarvisListeningService::class.java).setAction(ACTION_STOP)
  val speak = Intent(this, JarvisListeningService::class.java).setAction(ACTION_SPEAK)
  val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
  val stopPi = PendingIntent.getService(this, 1, stop, flags)
  val speakPi = PendingIntent.getService(this, 2, speak, flags)
  return Notification.Builder(this, channelId)
   .setSmallIcon(android.R.drawable.ic_btn_speak_now)
   .setContentTitle("M.J. JARVIS — Ready")
   .setContentText(if (listening) "Listening to one command…" else lastStatus)
   .addAction(Notification.Action.Builder(null, "Speak", speakPi).build())
   .addAction(Notification.Action.Builder(null, "Privacy ON · Stop", stopPi).build())
   .setOngoing(true).build()
 }

 private fun refreshNotification() {
  (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(notificationId, notification())
 }

 private fun listenOnce() {
  if (!active || listening) return
  if (!SpeechRecognizer.isRecognitionAvailable(this)) return
  listening = true
  lastStatus = "Listening…"
  refreshNotification()
  handler.postDelayed(timeout, 10000)
  recognizer?.destroy()
  recognizer = SpeechRecognizer.createSpeechRecognizer(this)
  recognizer?.setRecognitionListener(object : RecognitionListener {
   override fun onReadyForSpeech(params: Bundle?) {}
   override fun onBeginningOfSpeech() {}
   override fun onRmsChanged(rmsdB: Float) {}
   override fun onBufferReceived(buffer: ByteArray?) {}
   override fun onEndOfSpeech() {}
   override fun onError(error: Int) {
    Log.w("JarvisVoice", "SpeechRecognizer error code: $error")
    finishListening("Recognition error $error. Tap Speak to retry.")
   }
   override fun onResults(results: Bundle?) {
    val phrase = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
    Log.i("JarvisVoice", "Recognition returned; characters: ${phrase.length}")
    finishListening(if (phrase.isBlank()) "No words recognized." else "Heard command. Processing…")
    handle(phrase)
   }
   override fun onPartialResults(partialResults: Bundle?) {}
   override fun onEvent(eventType: Int, params: Bundle?) {}
  })
  try {
   recognizer?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
   })
  } catch (e: Exception) {
   Log.e("JarvisVoice", "Unable to start recognition", e)
   finishListening("Could not start voice recognition.")
  }
 }

 private fun finishListening(message: String) {
  handler.removeCallbacks(timeout)
  lastStatus = message
  listening = false
  recognizer?.destroy()
  recognizer = null
  if (active) refreshNotification()
 }

 private fun handle(raw: String) {
  val c = raw.trim().lowercase(Locale.ROOT).removePrefix("hey jarvis").trim().trimStart(',')
  if (c.contains("privacy mode on") || c.contains("stop listening") || c.contains("પ્રાઇવસી મોડ ચાલુ")) {
   stopSession()
   return
  }
  if (c == "go home" || c == "home" || c == "go back" || c == "back" ||
      c == "scroll down" || c == "scroll up" || c == "recents" || c == "recent apps" ||
      c.startsWith("tap ")) {
   val success = JarvisAccessibilityService.execute(c)
   lastStatus = if (success) "Command executed: $c" else "Command failed. Check Screen Control permission."
   refreshNotification()
  } else {
   lastStatus = "Unsupported command. Try Go Home or Go Back."
   refreshNotification()
  }
 }

 private fun stopSession() {
  active = false
  handler.removeCallbacks(timeout)
  listening = false
  recognizer?.cancel()
  recognizer?.destroy()
  recognizer = null
  stopForeground(STOP_FOREGROUND_REMOVE)
  stopSelf()
 }
 override fun onBind(intent: Intent?) = null
 override fun onDestroy() { active = false; handler.removeCallbacks(timeout); recognizer?.destroy(); super.onDestroy() }
 companion object {
  const val ACTION_STOP = "com.mj.jarvis.STOP_LISTENING"
  const val ACTION_SPEAK = "com.mj.jarvis.SPEAK_ONCE"
 }
}
