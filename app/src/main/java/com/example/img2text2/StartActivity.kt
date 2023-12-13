package com.example.img2text2

import android.app.ProgressDialog.show
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import java.util.Locale
import java.util.Objects

class StartActivity : AppCompatActivity() {
    private lateinit var translateBtn: Button
    private lateinit var speechButton: ImageButton
    private lateinit var speechRecognizer: SpeechRecognizer
    lateinit var img2TextBtn: Button
    private val REQUEST_CODE_SPEECH_INPUT = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        translateBtn = findViewById(R.id.btn_translate)
        translateBtn.setOnClickListener {
            val intent = Intent(this, TranslateActivity::class.java)
            startActivity(intent)
        }

        img2TextBtn = findViewById(R.id.btn_img2text)
        img2TextBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }


        // speech recognize
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechButton = findViewById(R.id.speech_recog_btn)
        speechButton.setOnClickListener {

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak for command")

            try {
                startActivityForResult(intent,REQUEST_CODE_SPEECH_INPUT)
            }catch (e: Exception) {
                // on below line we are displaying error message in toast
                Toast
                    .makeText(
                        this, " " + e.message,
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }

        }

    }

    // on below line we are calling on activity result method.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // in this method we are checking request
        // code with our result code.
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            // on below line we are checking if result code is ok
            if (resultCode == RESULT_OK && data != null) {

                val res : ArrayList<String> = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>


                val recognizedText = Objects.requireNonNull(res)[0]
                // Set command based on recognized text
                handleCommand(recognizedText.toUpperCase())
            }
        }
    }

    private fun handleCommand(command: String) {
        val outputTextView: TextView = findViewById(R.id.idTVOutput)

        when (command.toUpperCase(Locale.getDefault())) {
            "DỊCH" -> {
                // Replace this with the action you want to perform for the "open translate" command
                outputTextView.text = "DỊCH"
                val intent = Intent(this, TranslateActivity::class.java)
                startActivity(intent)
            }
            "OCR" -> {
                // Replace this with the action you want to perform for the "open image to text" command
                outputTextView.text = "OCR"
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            // Add more cases for other commands if needed
            else -> {
                // Handle unrecognized command
                outputTextView.text = "Unrecognized command: $command"
            }
        }
    }

}

