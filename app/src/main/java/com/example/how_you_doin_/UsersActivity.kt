package com.example.how_you_doin_

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.how_you_doin_.adapter.UsersAdapter
import com.example.how_you_doin_.model.Post
import com.example.how_you_doin_.model.Users
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_users.*

class UsersActivity : AppCompatActivity() {

    private lateinit var adapter: UsersAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var db:FirebaseFirestore

    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        setSupportActionBar(usersToolbar)
        supportActionBar?.title = "Users"

        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {

        val currentUser = auth.currentUser!!.uid

        layoutManager = LinearLayoutManager(this)
        db = FirebaseFirestore.getInstance()
        val users = db.collection("users")
       // val query = users.orderBy("displayName", Query.Direction.DESCENDING)
        val query = users.whereNotEqualTo("uid",currentUser)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Users>().setQuery(query, Users::class.java).build()

        adapter = UsersAdapter(recyclerViewOptions,this)
        usersRecyclerView.adapter = adapter
        usersRecyclerView.layoutManager = layoutManager
        usersRecyclerView.addItemDecoration(DividerItemDecoration(
                this@UsersActivity,LinearLayoutManager.VERTICAL
        ))

    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}