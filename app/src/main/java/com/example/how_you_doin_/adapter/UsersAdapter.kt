package com.example.how_you_doin_.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.how_you_doin_.ChatActivity
import com.example.how_you_doin_.R
import com.example.how_you_doin_.model.Users
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class UsersAdapter(options: FirestoreRecyclerOptions<Users>, val context: Context):
    FirestoreRecyclerAdapter<Users, UsersAdapter.UsersViewHolder>(options) {


    class UsersViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val usersImage: ImageView = itemView.findViewById(R.id.usersImage)
        val usersName: TextView = itemView.findViewById(R.id.usersName)
        val clChatRows: ConstraintLayout = itemView.findViewById(R.id.clChatRows)

    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): UsersViewHolder {
        val viewhHolder = UsersViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.users_list_rows, p0 , false))

        return viewhHolder
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int, model: Users) {
        holder.usersName.text = model.displayName
        Glide.with(holder.usersImage.context).load(model.imageUrl).circleCrop().into(holder.usersImage)

        holder.clChatRows.setOnClickListener {
            val intent = Intent(context,ChatActivity::class.java)
            intent.putExtra("name",model.displayName)
            intent.putExtra("uid",model.uid)
            intent.putExtra("imageUrl",model.imageUrl)
            context.startActivity(intent)
        }
    }
}