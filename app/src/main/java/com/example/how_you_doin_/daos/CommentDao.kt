package com.example.how_you_doin_.daos

import com.example.how_you_doin_.model.Comments
import com.example.how_you_doin_.model.Users
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CommentDao {

    private val db = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth

    val postCollection = db.collection("posts")


  //  val commentCollection = db.collection("comments")
    fun addComment(text: String, postId: String){
        GlobalScope.launch {
            //val postId = text
            val currentUser = auth.currentUser!!.uid
            val userDao = UserDao()
            val comment = userDao.getUserByID(currentUser).await().toObject(Users::class.java)!!
            val createdAt = System.currentTimeMillis()
            val postComment = Comments(text,comment,createdAt)
            postCollection.document(postId).collection("comments").document().set(postComment)
        }
    }

    private fun getCommentsById(postId: String, commentId:String):Task<DocumentSnapshot>{
        return postCollection.document(postId).collection("comments").document(commentId).get()
    }

    fun updateLikes(postId: String, commentId: String){

        GlobalScope.launch {
            val currentUser = auth.currentUser!!.uid
            val comments = getCommentsById(postId,commentId).await().toObject(Comments::class.java)!!
            val isLiked = comments.likedBy.contains(currentUser)

            if (isLiked){
                comments.likedBy.remove(currentUser)
                comments.dislikedBy.add(currentUser)
            }else{
                comments.likedBy.add(currentUser)
                comments.dislikedBy.remove(currentUser)
            }
            postCollection.document(postId).collection("comments").document(commentId).set(comments)
        }
    }

    fun updateDisLikes(postId: String, commentId: String){

        GlobalScope.launch {
            val currentUser = auth.currentUser!!.uid
            val comments = getCommentsById(postId,commentId).await().toObject(Comments::class.java)!!
            val disLiked = comments.dislikedBy.contains(currentUser)

            if (disLiked){
                comments.dislikedBy.remove(currentUser)
                comments.likedBy.add(currentUser)
            }else{
                comments.dislikedBy.add(currentUser)
                comments.likedBy.remove(currentUser)
            }
            postCollection.document(postId).collection("comments").document(commentId).set(comments)
        }
    }
}