package com.example.img2text2

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.Locale

class TranslateActivity : AppCompatActivity() {

    private var languageArrayList: ArrayList<ModelLanguage>? = null

    companion object {
        private const val TAG = "MAIN_TAG"
    }


    private var inputLanguageCode = "en"
    private var inputLanguageTitle = "English"
    private var outputLanguageCode = "vi"
    private var outputLanguageTitle = "Vietnamese"


    private lateinit var inputText: EditText
    private lateinit var useDetectResult: MaterialButton
    private lateinit var outputText: TextView
    private lateinit var inputLangBtn: MaterialButton
    private lateinit var outputLangBtn: MaterialButton
    private lateinit var translateBtn: MaterialButton

    private lateinit var speakBtn: MaterialButton
    private lateinit var stopSpeakBtn: MaterialButton
    private lateinit var clearBtn: MaterialButton


    // translator
    private lateinit var translatorOptions: TranslatorOptions
    private lateinit var translator: Translator
    private lateinit var text2Speech: TextToSpeech

    //dialog
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)

        inputText = findViewById(R.id.input_text)
        useDetectResult = findViewById(R.id.use_detect_result)
        outputText = findViewById(R.id.output_text)
        inputLangBtn = findViewById(R.id.input_lang_btn)
        outputLangBtn = findViewById(R.id.out_lang_btn)
        translateBtn = findViewById(R.id.btn_translate2)

        speakBtn = findViewById(R.id.speaker_btn)
        stopSpeakBtn = findViewById(R.id.stop_speak_btn)
        clearBtn = findViewById(R.id.clear_btn)

        //progress init
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...!")
        progressDialog.setCanceledOnTouchOutside(false)

        loadAvailableLanguage()

        // In-Out click Listener
        inputLangBtn.setOnClickListener {
            inputLanguageChoose()
        }
        outputLangBtn.setOnClickListener {
            outputLanguageChoose()
        }

        translateBtn.setOnClickListener {
            validateData()

        }

        var detectResultInput = intent.getStringExtra("detectResult").toString()
        useDetectResult.setOnClickListener {
            try {
                Log.d(TAG, "detectResult: $detectResultInput")
                if (detectResultInput.isEmpty() || detectResultInput == "null") {
                    val mainIntent = Intent(this, MainActivity::class.java)
                    showToast("Detect Result is Empty. Please detect an Image")
                    startActivity(mainIntent)
                } else {
                    inputText.setText(detectResultInput)
                }
            } catch (e: Exception) {
                showToast("Can not use Detect Result")
            }
        }


        // speaker
        text2Speech = TextToSpeech(applicationContext, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {


                text2Speech.language = Locale.US
                text2Speech.setSpeechRate(0.1f)

            } else {
                showToast("Language not available")
            }
        })
        speakBtn.setOnClickListener {
            // show log
            Log.v("TAG", "speech resultText ${outputText.text.toString()} ")
            if (outputText.text.isNotEmpty()) {
                showToast("Speaker is ready to speak!")
                text2Speech.speak(outputText.text.toString(), TextToSpeech.QUEUE_ADD, null)
            } else {
                showToast("Set the input Image First")
            }
            if (text2Speech.isSpeaking) {
                text2Speech.stop()
                showToast("Speaker ready to speak again")
                text2Speech.speak(outputText.text.toString(), TextToSpeech.QUEUE_ADD, null)
            }
        }
        stopSpeakBtn.setOnClickListener {
            if (text2Speech.isSpeaking) {
                showToast("Speaker is stopped")
                text2Speech.stop()
            } else {
                showToast("Speaker is not speaking")
            }
        }
        clearBtn.setOnClickListener {
            inputText.text = null
            outputText.text = ""
            detectResultInput = ""
        }

    }

    private var inputLanguageText = ""
    private fun validateData() {
        inputLanguageText = inputText.text.toString().trim()

        Log.d(TAG, "validateData: inputText : $inputLanguageText")

        if (inputLanguageText.isEmpty()) {
            showToast("No text input")
        } else {
            startTranslation()
        }
    }

    private fun startTranslation() {
        progressDialog.setMessage("Processing language model...")
        progressDialog.show()
        translatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(inputLanguageCode)
            .setTargetLanguage(outputLanguageCode).build()

        translator = Translation.getClient(translatorOptions)

        val downloadConditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        translator.downloadModelIfNeeded(downloadConditions)
            .addOnSuccessListener {
                Log.d(TAG, "start Translate")
                progressDialog.setMessage("Translating...")
                translator.translate(inputLanguageText)
                    .addOnSuccessListener { translatedText ->
                        Log.d(TAG, "Translated Text: $translatedText")
                        progressDialog.dismiss()
                        outputText.text = translatedText
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Log.e(TAG, "Translated Text:", e)
                        showToast("Failed to translate due to ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Log.d(TAG, "download failed", e)
                showToast("Failed to download due to ${e.message}")
            }
    }

    private fun loadAvailableLanguage() {
        languageArrayList = ArrayList()

        val languageCodeList = TranslateLanguage.getAllLanguages()

        for (languageCode in languageCodeList) {
            val languageTitle = Locale(languageCode).displayLanguage
            Log.d(TAG, "loadAvailableLanguage: langCode:$languageCode")
            Log.d(TAG, "loadAvailableLanguage: langCode:$languageTitle")

            //create lang model

            val modelLanguage = ModelLanguage(languageCode, languageTitle)

            languageArrayList!!.add(modelLanguage)

        }
    }

    //  popup menu

    private fun inputLanguageChoose() {
        val popupMenu = PopupMenu(this, inputLangBtn)
/////////////////////////////////////////////////////////////////
//        for (i in languageArrayList!!.indices) {
//            popupMenu.menu.add(Menu.NONE, i, i, languageArrayList!![i].languageTitle)
//        }
//        popupMenu.show()
//
//        popupMenu.setOnMenuItemClickListener { menuItem ->
//            val position = menuItem.itemId
//            inputLanguageCode = languageArrayList!![position].languageCode
//            inputLanguageTitle = languageArrayList!![position].languageTitle
//
//            inputLangBtn.text = inputLanguageTitle
//            inputText.hint = "Enter $inputLanguageTitle"
//            Log.d(TAG, "inputLangChoose: Code :$inputLanguageCode, Title:$inputLanguageTitle")
//            false
//        }
//        ////////////////////////////////////////////////////////
        popupMenu.menu.add("ENGLISH")
        popupMenu.menu.add("VIETNAMESE")
        popupMenu.menu.add("CHINESE")
        popupMenu.menu.add("JAPANESE")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.title) {
                "ENGLISH" -> {
                    inputLangBtn.text = "ENGLISH"
                    inputLanguageCode = "en"
                    true
                }

                "VIETNAMESE" -> {
                    inputLangBtn.text = "VIETNAMESE"
                    inputLanguageCode = "vi"

                    true
                }

                "CHINESE" -> {
                    inputLangBtn.text = "CHINESE"
                    inputLanguageCode = "zh"
                    true
                }

                "JAPANESE" -> {
                    inputLangBtn.text = "JAPANESE"
                    inputLanguageCode = "ja"
                    true
                }

                else -> false
            }
        }

    }

    private fun outputLanguageChoose() {
        val popupMenu = PopupMenu(this, outputLangBtn)
//


        /////////////////////////////
        popupMenu.menu.add("ENGLISH")
        popupMenu.menu.add("VIETNAMESE")
        popupMenu.menu.add("CHINESE")
        popupMenu.menu.add("JAPANESE")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.title) {
                "ENGLISH" -> {
                    outputLangBtn.text = "ENGLISH"
                    outputLanguageCode = "en"
                    text2Speech.language = Locale.US
                    true
                }

                "VIETNAMESE" -> {
                    outputLangBtn.text = "VIETNAMESE"
                    outputLanguageCode = "vi"
                    text2Speech.language = Locale.US

                    true
                }

                "CHINESE" -> {
                    outputLangBtn.text = "CHINESE"
                    outputLanguageCode = "zh"
                    text2Speech.language = Locale.CHINA
                    true
                }

                "JAPANESE" -> {
                    outputLangBtn.text = "JAPANESE"
                    outputLanguageCode = "ja"
                    text2Speech.language = Locale.JAPANESE
                    true
                }

                else -> false
            }
        }
        ////////////////////////////
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    }
}