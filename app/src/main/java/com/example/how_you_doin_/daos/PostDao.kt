package com.example.how_you_doin_.daos

import android.util.Log
import com.example.how_you_doin_.model.Post
import com.example.how_you_doin_.model.Users
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostDao {

    private val db = FirebaseFirestore.getInstance()
    val postCollections = db.collection("posts")
    private val auth = Firebase.auth

    fun addPost(imgUrl:String,text:String){
        val currentUserId = auth.currentUser!!.uid


        GlobalScope.launch{
            val userDao = UserDao()

            /*
            toObject() converts the variable into the instance of that class so we can use the functionality of that class
             ,in which the instance belongs to that class
             */
            val user = userDao.getUserByID(currentUserId).await().toObject(Users::class.java)!!

            Log.d("user", user.toString())

            val currentTime = System.currentTimeMillis()
            val post = Post(currentUserId,imgUrl,text,user,currentTime)
            postCollections.document().set(post)
        }
    }

    private fun getPostById(postId: String): Task<DocumentSnapshot> {
        return postCollections.document(postId).get()
    }

    fun updateLikes(postId: String){
        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val post = getPostById(postId).await().toObject(Post::class.java)!!
            val isLiked = post.likedBy.contains(currentUserId)

            if (isLiked){
                post.likedBy.remove(currentUserId)
            }else{
                post.likedBy.add(currentUserId)
            }
            postCollections.document(postId).set(post)
        }
    }
}