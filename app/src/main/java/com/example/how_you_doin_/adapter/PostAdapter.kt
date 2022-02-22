package com.example.how_you_doin_.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.how_you_doin_.R
import com.example.how_you_doin_.model.Post
import com.example.how_you_doin_.model.Utils
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class PostAdapter(options: FirestoreRecyclerOptions<Post>, private val listener: IPostAdapter,
                  private val listener2: IDeleteAdapter,private val listener3: ICommentAdapter,private val listener4 : IImageAdapter):
    FirestoreRecyclerAdapter<Post, PostAdapter.PostViewHolder>(options) {

    class PostViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val postText: TextView = itemView.findViewById(R.id.postTitle)
        val userText: TextView = itemView.findViewById(R.id.userName)
        val createdAt: TextView = itemView.findViewById(R.id.createdAt)
        val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val likeButton: ImageView = itemView.findViewById(R.id.likeButton)
        val deleteButton:ImageView = itemView.findViewById(R.id.deleteButton)
        val commentButton:ImageView = itemView.findViewById(R.id.commentButton)
        val imgPost:ImageView = itemView.findViewById(R.id.imgPost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val viewHolder = PostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_post,parent,false))

        viewHolder.likeButton.setOnClickListener{
            //this gives the documents id
            listener.onLikeClicked(snapshots.getSnapshot(viewHolder.adapterPosition).id)
        }

        viewHolder.deleteButton.setOnClickListener {
            //this gives the documents id
            listener2.onDelete(snapshots.getSnapshot(viewHolder.adapterPosition).id)
        }

        viewHolder.commentButton.setOnClickListener {
            //this gives the document id
            listener3.onComment(snapshots.getSnapshot(viewHolder.adapterPosition).id)
        }

        viewHolder.userImage.setOnClickListener {
            //this gives the document id
            listener4.imageClick(snapshots.getSnapshot(viewHolder.adapterPosition).id)
        }


        return viewHolder
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int, model: Post) {
        holder.postText.text = model.text
        holder.userText.text = model.createdBy.displayName
        holder.likeCount.text = model.likedBy.size.toString()
        holder.createdAt.text = Utils.getTimeAgo(model.createdAt)
        Glide.with(holder.userImage.context).load(model.createdBy.imageUrl).circleCrop().into(holder.userImage)

        if (model.imgUrl.isNotEmpty()){
            holder.imgPost.visibility = View.VISIBLE
            Glide.with(holder.imgPost.context).load(model.imgUrl).into(holder.imgPost)
        }else{
            holder.imgPost.visibility = View.GONE
        }

        val auth = Firebase.auth
        val currentUserId = auth.currentUser!!.uid
        val isLiked = model.likedBy.contains(currentUserId)

        if (isLiked){
            holder.likeButton.setImageDrawable(ContextCompat.getDrawable(holder.likeButton.context,
                R.drawable.ic_liked
            ))
        }else{
            holder.likeButton.setImageDrawable(ContextCompat.getDrawable(holder.likeButton.context,
                R.drawable.ic_unliked
            ))
        }
    }

}

interface IPostAdapter{
    fun onLikeClicked(postId:String)
}

interface IDeleteAdapter{
    fun onDelete(postId:String)
}

interface ICommentAdapter{
    fun onComment(postId:String)
}

interface IImageAdapter{
    fun imageClick(postId:String)
}

