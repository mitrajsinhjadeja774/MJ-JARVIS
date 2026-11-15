package com.mj.jarvis
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.*
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
 private lateinit var prefs: SharedPreferences
 private lateinit var status: TextView
 private lateinit var greeting: EditText
 private var tts: TextToSpeech? = null
 private var recognizer: SpeechRecognizer? = null
 private val defaultGreeting = "Yes Mitraj Sir, how can I help you?"
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  prefs = getSharedPreferences("jarvis", MODE_PRIVATE)
  tts = TextToSpeech(this, this)
  val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER_HORIZONTAL; setPadding(28,36,28,24) }
  root.addView(TextView(this).apply { text="M.J. JARVIS"; textSize=30f; gravity=Gravity.CENTER })
  status = TextView(this).apply { text="Your personal voice assistant"; textSize=16f; gravity=Gravity.CENTER; setPadding(0,18,0,18) }
  root.addView(status)
  root.addView(Button(this).apply { text="TALK TO JARVIS"; setOnClickListener { listen() } })
  root.addView(TextView(this).apply { text="Custom greeting"; textSize=18f; setPadding(0,26,0,8) })
  greeting = EditText(this).apply { setText(prefs.getString("greeting",defaultGreeting)); hint="Enter greeting" }
  root.addView(greeting)
  root.addView(Button(this).apply { text="Save greeting"; setOnClickListener { val v=greeting.text.toString().trim(); if(v.isNotEmpty()){prefs.edit().putString("greeting",v).apply(); speak("Greeting saved")} } })
  root.addView(Button(this).apply { text="Reset greeting"; setOnClickListener { prefs.edit().remove("greeting").apply(); greeting.setText(defaultGreeting); speak("Greeting reset") } })
  root.addView(TextView(this).apply { text="Commands: WhatsApp, YouTube, call [number], alarm, Wi-Fi, Bluetooth"; setPadding(0,24,0,0) })
  setContentView(root)
  if(checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO),20)
  speak(prefs.getString("greeting",defaultGreeting) ?: defaultGreeting)
 }
 private fun listen() {
  if(!SpeechRecognizer.isRecognitionAvailable(this)){status.text="Speech recognition unavailable";return}
  if(checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO),20);return}
  recognizer?.destroy()
  recognizer=SpeechRecognizer.createSpeechRecognizer(this)
  recognizer?.setRecognitionListener(object: android.speech.RecognitionListener {
   override fun onReadyForSpeech(p:Bundle?){status.text="Listening…"}
   override fun onBeginningOfSpeech(){}
   override fun onRmsChanged(v:Float){}
   override fun onBufferReceived(b:ByteArray?){}
   override fun onEndOfSpeech(){status.text="Processing…"}
   override fun onError(e:Int){status.text="Didn't catch that. Tap Talk and retry."}
   override fun onResults(b:Bundle?){val s=b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty();status.text="You said: $s";command(s)}
   override fun onPartialResults(b:Bundle?){}
   override fun onEvent(t:Int,b:Bundle?){}
  })
  recognizer?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault().toLanguageTag());putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to JARVIS") })
 }
 private fun command(raw:String) {
  val c=raw.lowercase(Locale.ROOT)
  when {
   c.contains("whatsapp")->launch("com.whatsapp","WhatsApp")
   c.contains("youtube")->launch("com.google.android.youtube","YouTube")
   c.contains("wifi")||c.contains("wi-fi")-> {startActivity(Intent(Settings.ACTION_WIFI_SETTINGS));speak("Opening Wi-Fi settings")}
   c.contains("bluetooth")-> {startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS));speak("Opening Bluetooth settings")}
   c.contains("alarm")-> {startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS));speak("Opening alarms")}
   c.startsWith("call ")-> {val n=raw.substringAfter("call ").filter{it.isDigit()||it=='+'};if(n.isNotBlank()){startActivity(Intent(Intent.ACTION_DIAL,Uri.parse("tel:$n")));speak("Opening dialer")}else speak("Say call followed by a phone number")}
   else->speak("I can open WhatsApp, YouTube, Wi-Fi, Bluetooth, alarms, or the dialer.")
  }
 }
 private fun launch(pkg:String,label:String){val i=packageManager.getLaunchIntentForPackage(pkg);if(i!=null){startActivity(i);speak("Opening $label")}else{speak("$label is not installed");Toast.makeText(this,"$label is not installed",Toast.LENGTH_SHORT).show()}}
 private fun speak(s:String){status.text=s;tts?.speak(s,TextToSpeech.QUEUE_FLUSH,null,"jarvis_reply")}
 override fun onInit(code:Int){if(code==TextToSpeech.SUCCESS){tts?.language=Locale("en","IN");tts?.setSpeechRate(.95f)}}
 override fun onDestroy(){recognizer?.destroy();tts?.stop();tts?.shutdown();super.onDestroy()}
}
