package com.example.how_you_doin_.adapter


import com.bumptech.glide.Glide
import com.example.how_you_doin_.R
import com.example.how_you_doin_.model.Utils
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_from_rows.view.*

class ChatFromAdapter(val text:String, private val imgUrl:String, private val timeStamp:Long) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.from_message.text = text

        Glide.with(viewHolder.itemView.userProfileImage)
            .load(imgUrl)
            .circleCrop()
            .into(viewHolder.itemView.userProfileImage)

        viewHolder.itemView.fromMsgTimeStamp.text = Utils.getTimeAgo(timeStamp)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_rows
    }
}