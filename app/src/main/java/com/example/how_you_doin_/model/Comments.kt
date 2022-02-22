package com.example.how_you_doin_.model


data class Comments(
    val text:String="",
    val createdBy: Users = Users(),
    val createdAt: Long = 0L,
    val likedBy:ArrayList<String> = ArrayList(),
    val dislikedBy:ArrayList<String> = ArrayList()
)