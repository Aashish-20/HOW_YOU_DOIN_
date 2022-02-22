package com.example.how_you_doin_

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.how_you_doin_.adapter.CommentAdapter
import com.example.how_you_doin_.adapter.onDeleteAdapter
import com.example.how_you_doin_.adapter.onDislikeAdapter
import com.example.how_you_doin_.adapter.onLikeAdapter
import com.example.how_you_doin_.daos.CommentDao
import com.example.how_you_doin_.model.Comments
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommentsActivity : AppCompatActivity(), onDeleteAdapter, onDislikeAdapter, onLikeAdapter {

    private lateinit var postId:String

    private lateinit var commentDao: CommentDao

    private lateinit var toolbar: Toolbar

    private lateinit var edtCommentText:EditText
    private lateinit var btnPostComment:Button
    private lateinit var recyclerComment:RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var adapter:CommentAdapter

    private val auth = Firebase.auth
    private lateinit var db: FirebaseFirestore
    private var str:String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        db = FirebaseFirestore.getInstance()


        commentDao = CommentDao()

        toolbar = findViewById(R.id.toolbar)
        recyclerComment = findViewById(R.id.recyclerComment)
        edtCommentText = findViewById(R.id.edtCommentText)
        btnPostComment = findViewById(R.id.btnPostComment)

        //naming the toolbar
        setupToolbar()


        if (intent!=null){
            postId = intent.getStringExtra("postId").toString()
        }

        btnPostComment.setOnClickListener {
            val comment = edtCommentText.text.toString().trim()
            if (comment.isNotEmpty()){
                commentDao.addComment(comment,postId)
                edtCommentText.text.clear()
            }else{
                Toast.makeText(this, "cannot add empty comments!!", Toast.LENGTH_SHORT).show()
            }
        }


        setupRecyclerView()

    }

    private fun setupRecyclerView() {

        val commentCollection = commentDao.postCollection.document(postId).collection("comments")
        val query = commentCollection.orderBy("createdAt", Query.Direction.DESCENDING)

        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Comments>().setQuery(query,Comments::class.java).build()
        adapter = CommentAdapter(recyclerViewOptions,this,this,this)
        layoutManager = LinearLayoutManager(this@CommentsActivity)

        recyclerComment.adapter = adapter
        recyclerComment.layoutManager = layoutManager
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    private fun setupToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Comments"
    }

    override fun onDelete(commentId: String) {
        deleteComment(commentId)
    }

    private fun deleteComment(commentId: String){

        //this gives the currentUserId
        val currentUser =auth.currentUser!!.uid

        var id:Map<String,Any>

        //creating the dialog box for user confirmation
        val dialog = AlertDialog.Builder(this@CommentsActivity)
        dialog.setTitle("Delete")
        dialog.setMessage("Are you sure you want to delete the Comment?")
        dialog.setPositiveButton("Yes"){ _, _ ->
            //opening settings to make the connection available

            GlobalScope.launch {

                //for extracting the field value from the database
                db.collection("posts").document(postId).collection("comments").document(commentId).get()
                    .addOnSuccessListener {
                        //if the document exists
                        if (it.exists()){
                            id = it.get("createdBy") as Map<String, Any>
                            str = id.getValue("uid").toString()
                            Log.w("str",str.toString())

                            if (currentUser == str){

                                GlobalScope.launch {
                                    db.collection("posts").document(postId).collection("comments").document(commentId).delete()
                                    withContext(Dispatchers.Main){
                                        Toast.makeText(this@CommentsActivity, "comment deleted", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }else{

                                Toast.makeText(this@CommentsActivity, "Can't delete other persons comment!!!", Toast.LENGTH_SHORT).show()

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

    override fun onDislike(commentId: String) {
        commentDao.updateDisLikes(postId,commentId)
    }

    override fun onLike(commentId: String) {
        commentDao.updateLikes(postId,commentId)
    }

}