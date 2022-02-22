package com.example.how_you_doin_

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.how_you_doin_.adapter.ChatFromAdapter
import com.example.how_you_doin_.adapter.ChatToAdapter
import com.example.how_you_doin_.model.Messages
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity() {
    
    private val auth = Firebase.auth

    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        messagesRecyclerView.adapter = adapter
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        setupRecyclerView()

        if (intent!=null){
            setSupportActionBar(chatToolbar)
            supportActionBar?.title = intent.getStringExtra("name")
        }

        btnSendMessage.setOnClickListener {
            if (edtMessage.text.isNotEmpty()){
                savingMessagesInDb()
                messagesRecyclerView.smoothScrollToPosition(adapter.itemCount)
            }
        }
    }

    private fun setupRecyclerView() {

        val fromId = auth.currentUser!!.uid
        val toId = intent.getStringExtra("uid").toString()

        val data = FirebaseDatabase.getInstance().getReference("/messages/$fromId/$toId")
        val photo = intent.getStringExtra("imageUrl")

        data.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val currentUserMsg = snapshot.getValue(Messages::class.java)

                if ( currentUserMsg != null ){
                    if (currentUserMsg.fromId == FirebaseAuth.getInstance().uid){

                        adapter.add(ChatFromAdapter(currentUserMsg.text,
                            auth.currentUser!!.photoUrl.toString(),
                            currentUserMsg.timestamp
                        ))

                    }else{

                        adapter.add(ChatToAdapter(currentUserMsg.text,
                            photo.toString(),
                            currentUserMsg.timestamp))

                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun savingMessagesInDb() {

        val messageTxt = edtMessage.text.toString()
        val fromId = auth.currentUser!!.uid
        val toId = intent.getStringExtra("uid").toString()
        val timestamp = System.currentTimeMillis()

        val dbFromTO = FirebaseDatabase.getInstance().getReference("/messages/$fromId/$toId").push()
        val dbToFrom = FirebaseDatabase.getInstance().getReference("/messages/$toId/$fromId").push()
        
        val messageDetails = Messages(messageTxt,fromId,toId,timestamp, dbFromTO.key!!)

        //saving the chat for retrieving in both the ends
        dbFromTO.setValue(messageDetails).addOnSuccessListener {
            edtMessage.text.clear()

            messagesRecyclerView.scrollToPosition(adapter.itemCount-1)
        }
        dbToFrom.setValue(messageDetails)
        
    }

}