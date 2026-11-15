package com.mj.jarvis

import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.os.*
import android.speech.*
import android.provider.Settings
import java.util.Locale

/** User-started, visible foreground microphone session. No audio is stored. */
class JarvisListeningService : Service() {
 private val handler = Handler(Looper.getMainLooper())
 private var recognizer: SpeechRecognizer? = null
 private var active = false
 private val channelId = "jarvis_listening"
 private val restart = Runnable { if (active) listen() }

 override fun onCreate() {
  super.onCreate()
  (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
   NotificationChannel(channelId, "JARVIS listening", NotificationManager.IMPORTANCE_LOW))
 }
 override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
  if (intent?.action == ACTION_STOP) { stopSession(); return START_NOT_STICKY }
  if (!active) {
   active = true
   val notification = notification()
   if (Build.VERSION.SDK_INT >= 29) startForeground(42, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
   else startForeground(42, notification)
   listen()
  }
  return START_NOT_STICKY
 }
 private fun notification(): Notification {
  val stop = Intent(this, JarvisListeningService::class.java).setAction(ACTION_STOP)
  val stopPi = PendingIntent.getService(this, 1, stop, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  val openPi = PendingIntent.getActivity(this, 2, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  return Notification.Builder(this, channelId)
   .setSmallIcon(android.R.drawable.ic_btn_speak_now)
   .setContentTitle("M.J. JARVIS — Listening ON")
   .setContentText("Microphone active. Tap Privacy ON to stop.")
   .setContentIntent(openPi)
   .addAction(Notification.Action.Builder(null, "Privacy ON · Stop", stopPi).build())
   .setOngoing(true).build()
 }
 private fun listen() {
  if (!active || !SpeechRecognizer.isRecognitionAvailable(this)) { stopSession(); return }
  recognizer?.destroy()
  recognizer = SpeechRecognizer.createSpeechRecognizer(this)
  recognizer?.setRecognitionListener(object : RecognitionListener {
   override fun onReadyForSpeech(params: Bundle?) {}
   override fun onBeginningOfSpeech() {}
   override fun onRmsChanged(rmsdB: Float) {}
   override fun onBufferReceived(buffer: ByteArray?) {}
   override fun onEndOfSpeech() {}
   override fun onError(error: Int) { schedule() }
   override fun onResults(results: Bundle?) {
    val phrase = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
    handle(phrase)
    schedule()
   }
   override fun onPartialResults(partialResults: Bundle?) {}
   override fun onEvent(eventType: Int, params: Bundle?) {}
  })
  try {
   recognizer?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
   })
  } catch (_: Exception) { schedule() }
 }
 private fun schedule() {
  handler.removeCallbacks(restart)
  if (active) handler.postDelayed(restart, 1200)
 }
 private fun handle(raw: String) {
  val c = raw.trim().lowercase(Locale.ROOT)
  if (c.contains("privacy mode on") || c.contains("stop listening") || c.contains("પ્રાઇવસી મોડ ચાલુ")) {
   stopSession()
   return
  }
  if (c == "go home" || c == "home" || c == "go back" || c == "back" ||
      c == "scroll down" || c == "scroll up" || c == "recents" || c == "recent apps" ||
      c.startsWith("tap ")) JarvisAccessibilityService.execute(c)
 }
 private fun stopSession() {
  active = false
  handler.removeCallbacks(restart)
  recognizer?.cancel()
  recognizer?.destroy()
  recognizer = null
  stopForeground(STOP_FOREGROUND_REMOVE)
  stopSelf()
 }
 override fun onBind(intent: Intent?) = null
 override fun onDestroy() { active = false; handler.removeCallbacks(restart); recognizer?.destroy(); super.onDestroy() }
 companion object { const val ACTION_STOP = "com.mj.jarvis.STOP_LISTENING" }
}
