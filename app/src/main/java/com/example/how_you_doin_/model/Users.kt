package com.example.how_you_doin_.model

import java.sql.Timestamp

data class Users(
    val uid:String = "",
    val displayName:String ?= "",
    val imageUrl:String = ""
)