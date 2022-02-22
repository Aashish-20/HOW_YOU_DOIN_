package com.example.how_you_doin_.daos

import com.example.how_you_doin_.model.Users
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserDao {

    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("users")

    fun addUser(user: Users?){

        //this below criteria, the user will enter in this scope if the user is not null
        user?.let {
            //using coroutines so that we can create a background thread which would not affect the main thread
            GlobalScope.launch (Dispatchers.IO){
                userCollection.document(user.uid).set(it)
            }
        }

    }

    fun getUserByID(uId:String):Task<DocumentSnapshot>{
        return userCollection.document(uId).get()
    }
}