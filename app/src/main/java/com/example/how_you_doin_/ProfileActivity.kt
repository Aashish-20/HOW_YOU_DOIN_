package com.example.how_you_doin_

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.how_you_doin_.adapter.*
import com.example.how_you_doin_.daos.CommentDao
import com.example.how_you_doin_.daos.PostDao
import com.example.how_you_doin_.model.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.item_post.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity(), IPostAdapter, IDeleteAdapter, ICommentAdapter,
    IImageAdapter {

    private var documentId:String = ""
    private var str:String? = ""

    private val auth = Firebase.auth
    private lateinit var db: FirebaseFirestore

    private lateinit var info : Map<String,Any>

    private lateinit var layoutManager:RecyclerView.LayoutManager
    private lateinit var postDao: PostDao
    private lateinit var commentDao: CommentDao

    private lateinit var adapter:PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        layoutManager = LinearLayoutManager(this)
        postDao = PostDao()

        if (intent != null){
         documentId = intent.getStringExtra("documentId").toString()

            db = FirebaseFirestore.getInstance()

            getUserInfo(documentId)
            userActivities(documentId)

        }

        btnChat.setOnClickListener {
            getUserInfoForChat(documentId)
        }

    }

    private fun getUserInfo(documentId: String) {
        GlobalScope.launch {

            db.collection("posts").document(documentId).get()
                .addOnSuccessListener {
                    if (it.exists()){
                        info = it.get("createdBy") as Map<String, Any>
                        userProfileName.text = info["displayName"].toString()
                        Glide.with(userProfileImage).load(info["imageUrl"]).circleCrop().into(userProfileImage)

                    }
                }
        }
    }

    private fun getUserInfoForChat(documentId: String) {
        GlobalScope.launch {

            db.collection("posts").document(documentId).get()
                .addOnSuccessListener {
                    if (it.exists()){
                        info = it.get("createdBy") as Map<String, Any>

                        val userName = info["displayName"].toString()
                        val userImage = info["imageUrl"].toString()
                        val userId = info["uid"].toString()

                        val intent = Intent(this@ProfileActivity,ChatActivity::class.java)
                        intent.putExtra("name", userName)
                        intent.putExtra("uid", userId)
                        intent.putExtra("imageUrl", userImage)
                        startActivity(intent)
                    }
                }
        }
    }

    private fun userActivities(documentId: String) {

        var userId:String?=null
        //var map:Map<String,Any>
        GlobalScope.launch (Dispatchers.IO){
            db.collection("posts").document(documentId).get()
                .addOnSuccessListener {
                    if (it.exists()){

                        // map = it.get("createdBy") as Map<String, Any>
                        userId =it.get("userId").toString()

                        val activityCollections = postDao.postCollections
                        val query = activityCollections.whereEqualTo("userId",userId)

                        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()

                        adapter = PostAdapter(recyclerViewOptions,this@ProfileActivity,
                            this@ProfileActivity,this@ProfileActivity,this@ProfileActivity)
                        recyclerProfile.adapter = adapter
                        recyclerProfile.layoutManager = layoutManager

                        adapter.startListening()
                    }
                }
        }
    }

    override fun onLikeClicked(postId: String) {
        postDao.updateLikes(postId)
    }

    override fun onDelete(postId: String) {
        getUserId(postId)
    }

    private fun getUserId(postId:String) {


        //this gives the currentUserId
        val currentUser =auth.currentUser!!.uid

        var id:Map<String,Any>

        //creating the dialog box for user confirmation
        val dialog = AlertDialog.Builder(this@ProfileActivity)
        dialog.setTitle("Delete")
        dialog.setMessage("Are you sure you want to delete the Post?")
        dialog.setPositiveButton("Yes"){ _, _ ->
            //opening settings to make the connection available

            GlobalScope.launch (Dispatchers.IO){

                //for extracting the field value from the database
                db.collection("posts").document(postId).get()
                    .addOnSuccessListener {
                        if (it.exists()){
                            id = it.get("createdBy") as Map<String, Any>
                            str = id.getValue("uid").toString()
                            Log.w("str",str.toString())

                            if (currentUser == str){

                                GlobalScope.launch {
                                    db.collection("posts").document(postId).delete()
                                    withContext(Dispatchers.Main){
                                        Toast.makeText(this@ProfileActivity, "post deleted", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }else{

                                Toast.makeText(this@ProfileActivity, "Can't delete other persons Post!!!", Toast.LENGTH_SHORT).show()

                            }

                        }
                    }
            }

        }
        dialog.setNegativeButton("No"){ _, _ ->
            //
        }
        dialog.create()
        dialog.show()
    }

    override fun onComment(postId: String) {
        commentDao = CommentDao()

        val intent = Intent(this@ProfileActivity,CommentsActivity::class.java)
        intent.putExtra("postId",postId)
        startActivity(intent)
    }

    override fun imageClick(postId: String) {
        recyclerProfile.smoothScrollToPosition(0)
    }
}