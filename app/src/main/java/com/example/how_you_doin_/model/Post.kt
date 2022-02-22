package com.example.how_you_doin_.model

data class Post (
    val userId:String = "",
    val imgUrl:String = "",
    val text:String = "",
    val createdBy:Users = Users(),
    val createdAt:Long = 0L,
    val likedBy:ArrayList<String> = ArrayList()
        )