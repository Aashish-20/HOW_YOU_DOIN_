package com.example.how_you_doin_

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.how_you_doin_.adapter.*
import com.example.how_you_doin_.daos.CommentDao
import com.example.how_you_doin_.daos.PostDao
import com.example.how_you_doin_.model.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), IPostAdapter, IDeleteAdapter, ICommentAdapter,
    IImageAdapter {

//    private lateinit var fab:FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var postDao: PostDao
    private lateinit var commentDao: CommentDao
    private lateinit var adapter: PostAdapter
    private lateinit var toolbar: Toolbar
    private lateinit var progressBar: ProgressBar


    private var str:String? = ""

    private val auth = Firebase.auth
    private lateinit var db:FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = FirebaseFirestore.getInstance()

        progressBar = findViewById(R.id.progressBar)
        toolbar = findViewById(R.id.toolbar)
        setUpToolbar()

        postDao = PostDao()
//        fab = findViewById(R.id.fab)
        recyclerView = findViewById(R.id.recyclerView)
        layoutManager = LinearLayoutManager(this)

//        fab.setOnClickListener{
//            val intent = Intent(this,CreatePostActivity::class.java)
//            startActivity(intent)
//
//        }
        btmNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.fabNa -> {
                    val intent = Intent(this,CreatePostActivity::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.btnUsers -> {
                    val intent = Intent(this,UsersActivity::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }

        setUpRecyclerView()

    }

    private fun setUpRecyclerView() {
        val postCollections = postDao.postCollections
        val query = postCollections.orderBy("createdAt", Query.Direction.DESCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query,Post::class.java).build()

        adapter = PostAdapter(recyclerViewOptions,this,this,this,this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager

    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onLikeClicked(postId: String) {
        postDao.updateLikes(postId)
    }

    override fun onDelete(postId: String) {
        getUserId(postId)
    }

    private fun getUserId(postId:String) {


        //this gives the currentUserId
        val currentUser =auth.currentUser!!.uid

        var id:Map<String,Any>

        //creating the dialog box for user confirmation
        val dialog = AlertDialog.Builder(this@MainActivity)
        dialog.setTitle("Delete")
        dialog.setMessage("Are you sure you want to delete the Post?")
        dialog.setPositiveButton("Yes"){ _, _ ->
            //opening settings to make the connection available

            GlobalScope.launch (Dispatchers.IO){

                //for extracting the field value from the database
                db.collection("posts").document(postId).get()
                    .addOnSuccessListener {
                        if (it.exists()){
                            id = it.get("createdBy") as Map<String, Any>
                            str = id.getValue("uid").toString()
                            Log.w("str",str.toString())

                            if (currentUser == str){

                                GlobalScope.launch {
                                    db.collection("posts").document(postId).delete()
                                    withContext(Dispatchers.Main){
                                        Toast.makeText(this@MainActivity, "post deleted", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }else{

                                Toast.makeText(this@MainActivity, "Can't delete other persons Post!!!", Toast.LENGTH_SHORT).show()

                            }

                        }
                    }
            }

        }
        dialog.setNegativeButton("No"){ _, _ ->
            //
        }
        dialog.create()
        dialog.show()
    }

    override fun onComment(postId: String) {
        commentDao = CommentDao()
       // commentDao.getPostByIds(postId)

        //commentDao.addComment(postId)

        val intent =Intent(this@MainActivity,CommentsActivity::class.java)
        intent.putExtra("postId",postId)
        startActivity(intent)
    }

    private fun setUpToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Share Your Mood"
    }

    override fun imageClick(postId: String) {
        val intent = Intent(this@MainActivity,ProfileActivity::class.java)
        intent.putExtra("documentId",postId)
        startActivity(intent)
    }
}
