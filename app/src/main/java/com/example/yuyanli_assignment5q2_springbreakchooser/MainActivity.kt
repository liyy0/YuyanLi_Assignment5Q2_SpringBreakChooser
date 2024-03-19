package com.example.yuyanli_assignment5q2_springbreakchooser

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.yuyanli_assignment5q2_springbreakchooser.databinding.ActivityMainBinding
import java.util.Locale
import kotlin.math.log
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() , SensorEventListener , TextToSpeech.OnInitListener{
    companion object {
        private const val SPEECH_REQUEST_CODE = 100
    }
    val shanghai = floatArrayOf(31.175679814772067F, 121.50123321872005F)
    val taiwan = floatArrayOf(25.102371F, 121.54854F)
    val guangzhou = floatArrayOf(23.12908F, 113.26436F)
    val china = arrayOf(shanghai, taiwan, guangzhou)

    val BU = floatArrayOf(42.35062653527951F, (-71.10543128982667).toFloat())
    val SF = floatArrayOf(37.7749295F, (-122.4194155).toFloat())
    val NY = floatArrayOf(40.712776F, (-74.005974).toFloat())
    val usa = arrayOf(BU, SF, NY)

    val Madrid = floatArrayOf(40.416775F, (-3.7037902).toFloat())
    val Barcelona = floatArrayOf(41.3850639F, (2.1734035).toFloat())
    val Valencia = floatArrayOf(39.46975F, (-0.37739).toFloat())
    val spanish = arrayOf(Madrid, Barcelona, Valencia)



    lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chineseButton.setOnClickListener{
            setLocale("zh")
            refreshUI()
            showSpeechInput()
        }

        binding.englishButton.setOnClickListener{
            setLocale("en")
            refreshUI()
            showSpeechInput()
        }

        binding.spanishButton.setOnClickListener{
            setLocale("es")
            refreshUI()
            showSpeechInput()
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        textToSpeech = TextToSpeech(this, this)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { accel ->
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Can be left empty
    }


    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val accelerationX = event.values[0]
            val accelerationY = event.values[1]
            val accelerationZ = event.values[2]

            if (sqrt((accelerationX * accelerationX + accelerationY * accelerationY + accelerationZ * accelerationZ).toDouble()) > 12)
            {

                //get current locale
                val config = resources.configuration
                val curlocale = config.locale.language
                Log.d("locale", curlocale)
                //jump to map activity
                selectlocation(curlocale)


            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("onActivityResult", "onActivityResult")
        when (requestCode) {
            SPEECH_REQUEST_CODE -> {
                Log.d("onActivityResult", SPEECH_REQUEST_CODE.toString())
                if (resultCode == Activity.RESULT_OK && null != data) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                    val recognizedText = result?.get(0) // Get the first match

                    // Set the recognized text to your EditText
                    Log.d("recognizedText", recognizedText.toString())
                    binding.editText.setText(recognizedText)

                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Check if the selected language is supported
            Log.d("TTS", "TextToSpeech initialized successfully")
        } else {
            Log.e("TTS", "Initialization failed")
        }
    }


    fun selectlocation(languageCode: String) {
        val greeting = when (languageCode) {
            "zh" -> "你好"
            "en" -> "Hello"
            "es" -> "Hola"
            else -> "Hello" // Default to English if unknown
        }

        speakGreeting(greeting, Locale.forLanguageTag(languageCode))

        when (languageCode) {
            "zh" -> {
                val city = china.random()
                openGoogleMaps(city[0].toDouble(), city[1].toDouble())
            }
            "en" -> {
                val city = usa.random()
                openGoogleMaps(city[0].toDouble(), city[1].toDouble())
            }
            "es" -> {
                val city = spanish.random()
                openGoogleMaps(city[0].toDouble(), city[1].toDouble())
            }
            // Add other languages and locations as needed
        }
    }

    private fun speakGreeting(greeting: String, locale: Locale) {
        textToSpeech.language = locale
        textToSpeech.speak(greeting, TextToSpeech.QUEUE_FLUSH, null, null)
    }


    private fun openGoogleMaps(latitude: Double, longitude: Double) {
        val gmmIntentUri = Uri.parse("geo:$latitude,$longitude")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        try {
            this@MainActivity.startActivity(mapIntent)
        }
        catch (a: Exception) {
            Log.d("locale", "Google Maps is not installed.")
            Toast.makeText(applicationContext, getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show()
        }
    }



    fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun refreshUI() {
        val intent = intent
        finish()
        startActivity(intent)
    }
    private fun showSpeechInput() {
        Log.d("showSpeechInput", "showSpeechInput")
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            // Use the current locale as default
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt))
        }
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
            Log.d("showSpeechInput", "startActivityForResult")
        } catch (a: Exception) {
            Toast.makeText(applicationContext, getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show()
        }
    }


}