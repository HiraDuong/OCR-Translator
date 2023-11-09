package com.example.img2text2

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Notification.Action
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION_CODES.P
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : AppCompatActivity() {

    private lateinit var takeImgBtn : MaterialButton
    private lateinit var detectImgBtn : MaterialButton
    private lateinit var imgView : ImageView
    private lateinit var resultText : TextView
    private lateinit var recognizer : TextRecognizer

    lateinit var progressDialog: ProgressDialog

    private companion object{
        private const val CAMERA_REQUEST_CODE = 100
        private const val STORAGE_REQUEST_CODE = 101

    }

    private var imgUri : Uri? = null
    private lateinit var cameraPermission: Array<String>
    private lateinit var storagePermission: Array<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        takeImgBtn = findViewById(R.id.take_img)
        detectImgBtn = findViewById(R.id.detect_img)
        imgView = findViewById(R.id.img_view)
        resultText = findViewById(R.id.result)


        //get permisson
        cameraPermission = arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        //progress
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //recognizer
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        //
        takeImgBtn.setOnClickListener{
            showInputImageDialog()
        }

        detectImgBtn.setOnClickListener {
            if(imgUri == null){
                showToast("Take input first")
            }

            else{
                recognitionFromImage()
            }
        }

    }

    private fun recognitionFromImage() {
        progressDialog.setMessage("Prepairing")
        progressDialog.show()
        try {
            val inputImage = InputImage.fromFilePath(this,imgUri!!)
            val taskResult = recognizer.process(inputImage)
                .addOnSuccessListener {text->
                    progressDialog.dismiss()
                    val recognizeText = text.text

                    resultText.setText(recognizeText)
                }

                .addOnFailureListener{e->
                    progressDialog.dismiss()
                    showToast("Recognize Failed ${e.message}")
                }
        }
        catch (e: Exception){
            showToast("Failed ${e.message}")
        }
    }

    private fun showInputImageDialog() {
        val popupMenu= PopupMenu(this,takeImgBtn)
        popupMenu.menu.add(Menu.NONE,1,1,"CAMERA")
        popupMenu.menu.add(Menu.NONE,2,2,"GALLERY")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {items->
            val id= items.itemId
            if (id == 1){
            if (checkCameraPermissions()){
                    pickImageCamera()
                }
               else{
                   requestCameraPermissions()
                showToast("DONE")

                }

                //pickImageCamera()
            }

            if (id == 2){
               if (checkStoragePermissions()){
                    pickImage()
                showToast("DONE")

              }
                else{
                   requestStoragePermissions()
               }
                //pickImage()
            }

            return@setOnMenuItemClickListener true

        }
    }

    private fun pickImage(){
        val intent= Intent(Intent.ACTION_PICK)

        intent.type = "image/*"
        galleryActivityResultLaucher.launch(intent)
    }

    private  val galleryActivityResultLaucher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
        if(result.resultCode == Activity.RESULT_OK){
            val data = result.data
            imgUri = data!!.data
            imgView.setImageURI(imgUri)
        }
        else{
            showToast("CANCEL!")
        }
    }
    private fun pickImageCamera(){
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE,"Sample Title")
        values.put(MediaStore.Images.Media.DESCRIPTION,"Sample Description")

        imgUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imgUri)
        cameraActivityResultLaucher.launch(intent)
    }

    private val cameraActivityResultLaucher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->

        if(result.resultCode == Activity.RESULT_OK){
            imgView.setImageURI(imgUri)
        }
        else{
            showToast("CANCEL!")
        }
    }

    private  fun checkStoragePermissions():Boolean{
        val storageResult= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        Log.v("TAG",(storageResult).toString())
        Log.v("TAG2",(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ).toString())
        //return storageResult
        return true
    }

    private fun checkCameraPermissions():Boolean{
        val cameraResult = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val storageResult = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        Log.v("TAG",(cameraResult && storageResult).toString())
        Log.v("TAG2",(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) ).toString())

        return cameraResult
    }

    private fun requestStoragePermissions(){
        ActivityCompat.requestPermissions(this,storagePermission, STORAGE_REQUEST_CODE)
    }

    private fun requestCameraPermissions(){
        ActivityCompat.requestPermissions(this,cameraPermission, CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            CAMERA_REQUEST_CODE->{
                if(grantResults.isNotEmpty()){
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (cameraAccepted && storageAccepted){
                        pickImageCamera()
                    }
                }
                else{
                    showToast("CAMERA and STORAGE are required ...")
                }
            }
            STORAGE_REQUEST_CODE->{
                if(grantResults.isNotEmpty()){
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (storageAccepted){
                        pickImage()
                    }
                }
                else{
                    showToast("STORAGE are required ...")
                }
            }
        }
    }

    private fun showToast(mess:String){
        Toast.makeText(this,mess,Toast.LENGTH_SHORT).show()
    }
}