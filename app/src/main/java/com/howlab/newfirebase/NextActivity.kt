package com.howlab.newfirebase

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_next.*

class NextActivity : AppCompatActivity() {
    var googleSignInClient : GoogleSignInClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_next)
        logout_button.setOnClickListener {
            logout()
        }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)
        set_button.setOnClickListener {
            saveData()
        }
        update_button.setOnClickListener {
            updateData()
        }
        delete_button.setOnClickListener {
            deleteData()
        }
        read_single_button.setOnClickListener {
            readSingleData()
        }
        read_observe_button.setOnClickListener {
            readObserveData()
        }
        query_single_button.setOnClickListener {
            querySingleData()
        }
        query_observe_button.setOnClickListener {
            queryObserveData()
        }
        runTransaction_imageview.setOnClickListener {
            runTransaction()
        }
        firestore_button.setOnClickListener {
            startActivity(Intent(this,FirestoreActivity::class.java))
        }
        storage_button.setOnClickListener {
            startActivity(Intent(this,StorageActivity::class.java))
        }
        crush_button.setOnClickListener {
            var a : String? = null
            a!!.length
        }
    }
    fun logout(){
        FirebaseAuth.getInstance().signOut()

        //Google Session out
        googleSignInClient?.signOut()

        //Facebook Session out
        LoginManager.getInstance().logOut()

        finish()
    }
    fun saveData(){
        var setEditTextString = set_edittext.text.toString()

        var map = mutableMapOf<String,Any>()
        map["name"] = "howl"
        map["age"] = setEditTextString

        FirebaseDatabase.getInstance().reference
            .child("users")
            .child("1")
            .setValue(map)
    }
    fun updateData(){
        var updateEditTextString = update_edittext.text.toString()

        var map = mutableMapOf<String,Any>()
        map["gender"] = updateEditTextString

        FirebaseDatabase.getInstance().reference
            .child("users")
            .child("1")
            .updateChildren(map)

    }

    fun deleteData(){
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child("1")
            .removeValue()
    }
    fun readSingleData(){
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child("1")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    var map = p0.value as Map<String,Any>
                    read_single_textview.text = map["age"].toString()
                }

            })
    }
    fun readObserveData(){
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child("1")
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    var map = p0.value as Map<String,Any>
                    read_observe_textview.text = map["age"].toString()
                }

            })
    }
    fun querySingleData(){
        FirebaseDatabase.getInstance().reference
            .child("users")
            .orderByChild("age").equalTo(query_single_edittext.text.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    var map = p0.children.first().value as Map<String,Any>
                    query_single_textview.text = map["name"].toString()
                }

            })
    }
    fun queryObserveData(){
        FirebaseDatabase.getInstance().reference
            .child("users")
            .orderByChild("age").equalTo(query_observe_edittext.text.toString())
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    var map = p0.children.first().value as Map<String,Any>
                    query_observe_textview.text = map["name"].toString()
                }

            })
    }
    fun runTransaction(){
        var uid = FirebaseAuth.getInstance().uid

        FirebaseDatabase.getInstance().reference
            .child("users")
            .child("1")
            .runTransaction(object : Transaction.Handler{
                override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
                    var p = p2?.getValue(UserModel::class.java)
                    runTransaction_textview.text = p?.likeCount.toString()
                    if(p!!.likes.containsKey(uid)){
                        runTransaction_imageview.setImageResource(android.R.drawable.star_on)
                    }else{
                        runTransaction_imageview.setImageResource(android.R.drawable.star_off)
                    }
                }

                override fun doTransaction(p0: MutableData): Transaction.Result {
                    var p = p0.getValue(UserModel::class.java)
                    if(p == null){
                        p = UserModel()
                        p.likeCount = 1
                        p.likes[uid!!] = true
                        p0.value = p
                        return Transaction.success(p0)
                    }
                    if(p.likes.containsKey(uid)){
                        p.likeCount = p.likeCount!! - 1
                        p.likes.remove(uid)
                    }else{
                        p.likeCount = p.likeCount!! + 1
                        p.likes[uid!!] = true
                    }
                    p0.value = p

                    return Transaction.success(p0)
                }

            })
    }
}
