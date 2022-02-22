package com.example.how_you_doin_.model

import java.sql.Timestamp

data class Messages(
    val text : String = "",
    val fromId : String = "",
    val toId : String = "",
    val timestamp: Long = 0L,
    val id:String = ""
//    val imgUrl:String = ""
)