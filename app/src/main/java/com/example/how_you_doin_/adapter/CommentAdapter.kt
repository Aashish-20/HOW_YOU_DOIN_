package com.example.how_you_doin_.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.how_you_doin_.R
import com.example.how_you_doin_.model.Comments
import com.example.how_you_doin_.model.Utils
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class CommentAdapter(options: FirestoreRecyclerOptions<Comments>,
      private val listener1:onLikeAdapter, private val listener2:onDislikeAdapter, private val listener3:onDeleteAdapter)
    : FirestoreRecyclerAdapter<Comments, CommentAdapter.CommentViewHolder>(options){

    class CommentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val postText: TextView = itemView.findViewById(R.id.postTitle)
        val userText: TextView = itemView.findViewById(R.id.userName)
        val createdAt: TextView = itemView.findViewById(R.id.postedAt)
        val userImage: ImageView = itemView.findViewById(R.id.userImage)

        val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        val likeButton: ImageView = itemView.findViewById(R.id.likeButton)
        val likeButtonColored:ImageView = itemView.findViewById(R.id.likeButtonColored)
        val dislikeButton:ImageView = itemView.findViewById(R.id.dislikeButton)
        val disLikeButtonColored:ImageView = itemView.findViewById(R.id.dislikeButtonColored)
        val dislikeCount:TextView = itemView.findViewById(R.id.dislikeCount)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val viewHolder = CommentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.comments_post,parent,false))

        viewHolder.likeButton.setOnClickListener {
            listener1.onLike(snapshots.getSnapshot(viewHolder.adapterPosition).id)
        }

        viewHolder.dislikeButton.setOnClickListener {
            listener2.onDislike(snapshots.getSnapshot(viewHolder.adapterPosition).id)
        }

        viewHolder.deleteButton.setOnClickListener {
            listener3.onDelete(snapshots.getSnapshot(viewHolder.adapterPosition).id)
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int, model: Comments) {
        holder.postText.text = model.text
        holder.userText.text = model.createdBy.displayName
        holder.createdAt.text = Utils.getTimeAgo(model.createdAt)

        holder.likeCount.text = model.likedBy.size.toString()
        holder.dislikeCount.text = model.dislikedBy.size.toString()

        Glide.with(holder.userImage.context).load(model.createdBy.imageUrl).circleCrop().into(holder.userImage)

        val auth = Firebase.auth
        val currentUser = auth.currentUser!!.uid
        val isLiked = model.likedBy.contains(currentUser)
        val disLiked = model.dislikedBy.contains(currentUser)

        if (disLiked){
            holder.disLikeButtonColored.visibility = View.VISIBLE
            holder.likeButtonColored.visibility = View.GONE
        }

        if(isLiked){
            holder.disLikeButtonColored.visibility = View.GONE
            holder.likeButtonColored.visibility = View.VISIBLE
        }

    }
}

interface onLikeAdapter{
    fun onLike(commentId:String)
}

interface onDislikeAdapter{
    fun onDislike(commentId: String)
}

interface onDeleteAdapter{
    fun onDelete(commentId: String)
}