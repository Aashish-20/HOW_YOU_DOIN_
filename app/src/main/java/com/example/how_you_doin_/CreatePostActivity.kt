package com.example.how_you_doin_

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import com.example.how_you_doin_.daos.PostDao
import com.example.how_you_doin_.notifications.NotificationData
import com.example.how_you_doin_.notifications.PushNotification
import com.example.how_you_doin_.notifications.RetrofitInstance
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.lang.Exception

const val TOPIC = "/topics/myTopic"

class CreatePostActivity : AppCompatActivity() {

    val TAG = "CreatePostActivity"

    private lateinit var postButton: Button
    private lateinit var postInput:EditText
    private lateinit var postDao:PostDao
    private lateinit var txtAddImage:TextView
    private lateinit var imgPreview:ImageView
    private lateinit var progressBar: ProgressBar

    private  var filePath: Uri? = null
    private var imgUrl: String= ""

     val pd : ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        progressBar = findViewById(R.id.progressbar)
        progressBar.visibility = View.GONE

        postButton = findViewById(R.id.postButton)
        postInput = findViewById(R.id.postInput)
        txtAddImage = findViewById(R.id.txtAddImage)
        imgPreview = findViewById(R.id.imgPreview)

        postDao = PostDao()

        txtAddImage.setOnClickListener{
            startFileChooser()
        }

        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

        postButton.setOnClickListener {
            val input = postInput.text.toString().trim()
            if (input.isNotEmpty() || imgPreview.drawable!= null){

                PushNotification(
                    NotificationData(input),
                    TOPIC
                ).also {
                    sendNotification(it)
                }

                uploadFile()

                finish()
            }
          else{
                Toast.makeText(
                    this@CreatePostActivity,
                    "cannot create empty post!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun sendNotification(notification: PushNotification)= CoroutineScope(Dispatchers.IO).launch{
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful){
             //   Log.d(TAG,"Response: ${Gson().toJson(response)}")
//                val intent = Intent(this@CreatePostActivity,MainActivity::class.java)
//                startActivity(intent)

            }else{
                Log.d(TAG, response.errorBody().toString())
            }
        }catch (e:Exception){
            Log.e(TAG,e.toString())
        }
    }

    private fun startFileChooser() {
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(i,"choose Picture"),1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null){

            filePath = data.data!!
            val bitmap:Bitmap = MediaStore.Images.Media.getBitmap(contentResolver,filePath)
            imgPreview.setImageBitmap(bitmap)
        }
    }

    private fun uploadFile() {

        progressBar.visibility = View.VISIBLE

        if (filePath != null){

            pd?.setTitle("uploading")
            pd?.show()

         GlobalScope.launch {

             val imageRef:StorageReference = FirebaseStorage.getInstance().reference.child("images/${filePath!!.lastPathSegment}")
             imageRef.putFile(filePath!!)
                 .addOnSuccessListener {
                     pd?.dismiss()
                     // Toast.makeText(this, filePath.lastPathSegment, Toast.LENGTH_SHORT).show()
                 }
                 .addOnFailureListener{

                     pd?.dismiss()
                     GlobalScope.launch {
                         withContext(Dispatchers.Main){

                             Toast.makeText(this@CreatePostActivity, it.message, Toast.LENGTH_SHORT).show()

                         }
                     }

                 }.addOnProgressListener {p0 ->
                     var progress:Double = (100.0 * p0.bytesTransferred )/p0.totalByteCount
                     pd?.setMessage("Uploaded ${progress.toInt()}%")
                 }

             //downloading the url after uploading

             val uploadTask = imageRef.putFile(filePath!!)
             uploadTask.continueWithTask { task->
                 if (!task.isSuccessful ){
                     task.exception?.let {
                         pd?.show()
                         throw it

                     }
                 }

                 imageRef.downloadUrl

             }.addOnCompleteListener{task->
                 if (task.isSuccessful){

                     val downloadUri = task.result
                     imgUrl = downloadUri.toString()
                     val input = postInput.text.toString().trim()

                     postDao.addPost(imgUrl,input)
                     pd?.dismiss()
                     progressBar.visibility = View.GONE

                     Log.d("downloadPicture", imgUrl)

                 }else{

                     Toast.makeText(this@CreatePostActivity, "not downloaded", Toast.LENGTH_SHORT).show()
                 }
             }
         }

        }
        else{
            val input = postInput.text.toString().trim()
            postDao.addPost("",input)
            progressBar.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (pd != null && pd.isShowing){
            pd.cancel()
        }
    }
}