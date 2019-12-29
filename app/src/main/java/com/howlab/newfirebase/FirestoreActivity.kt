package com.howlab.newfirebase

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_firestore.*
import kotlinx.android.synthetic.main.activity_firestore.query_observe_textview
import kotlinx.android.synthetic.main.activity_next.*
import kotlinx.android.synthetic.main.activity_next.delete_button
import kotlinx.android.synthetic.main.activity_next.query_observe_button
import kotlinx.android.synthetic.main.activity_next.query_observe_edittext
import kotlinx.android.synthetic.main.activity_next.read_observe_button
import kotlinx.android.synthetic.main.activity_next.read_observe_textview
import kotlinx.android.synthetic.main.activity_next.read_single_button
import kotlinx.android.synthetic.main.activity_next.read_single_textview
import kotlinx.android.synthetic.main.activity_next.runTransaction_imageview
import kotlinx.android.synthetic.main.activity_next.runTransaction_textview
import kotlinx.android.synthetic.main.activity_next.set_button
import kotlinx.android.synthetic.main.activity_next.set_edittext
import kotlinx.android.synthetic.main.activity_next.update_button
import kotlinx.android.synthetic.main.activity_next.update_edittext

class FirestoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firestore)
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
        simple_query_single_button.setOnClickListener {
            simpleQuerySingleData()
        }
        compound_query_1_button.setOnClickListener {
            compoundQuery_1()
        }
        compound_query_2_button.setOnClickListener {
            compoundQuery_2()
        }
        compound_query_3_button.setOnClickListener {
            compoundQuery_3()
        }
        query_observe_button.setOnClickListener {
            queryObserveData()
        }
        runTransaction_imageview.setOnClickListener {
            runTransaction()
        }
    }
    fun saveData(){
        var setEditTextString = set_edittext.text.toString()

        var map = mutableMapOf<String,Any>()
        map["name"] = "howl"
        map["gender"] = "Male"
        map["age"] = setEditTextString

        FirebaseFirestore.getInstance()
            .collection("users")
            .document()
            .set(map)
    }
    fun updateData(){
        var updateEditTextString = update_edittext.text.toString()

        var map = mutableMapOf<String,Any>()
        map["gender"] = updateEditTextString

        FirebaseFirestore.getInstance()
            .collection("users")
            .document("1")
            .update(map)

    }

    fun deleteData(){
        FirebaseFirestore.getInstance()
            .collection("users")
            .document("1")
            .delete()
    }
    fun readSingleData(){
        FirebaseFirestore.getInstance()
            .collection("users")
            .document("1")
            .get().addOnSuccessListener { documentSnapshot ->
                var map = documentSnapshot.data as Map<String, Any>
                read_single_textview.text = map["age"].toString()
            }
    }
    fun readObserveData(){
        FirebaseFirestore.getInstance()
            .collection("users")
            .document("1")
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                var map = documentSnapshot?.data as Map<String,Any>
                read_observe_textview.text = map["age"].toString()
            }

    }
    fun simpleQuerySingleData(){
        FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("age",simple_query_single_edittext.text.toString())
            .get().addOnSuccessListener { querySnapshot ->
                println(querySnapshot.documents)
            }

    }
    fun compoundQuery_1(){
        FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("age","16")
            .whereEqualTo("gender","Male")
            .get().addOnSuccessListener { querySnapshot ->
                println(querySnapshot.documents)
            }
    }
    fun compoundQuery_2(){
        FirebaseFirestore.getInstance()
            .collection("users")
            .whereLessThan("age","16")
            .whereEqualTo("gender","Male")
            .get().addOnSuccessListener { querySnapshot ->
                println(querySnapshot.documents)
            }
    }
    fun compoundQuery_3(){
        FirebaseFirestore.getInstance()
            .collection("users")
            .whereGreaterThan("age","15")
            .whereLessThan("age","17")
            .get().addOnSuccessListener { querySnapshot ->
                println(querySnapshot.documents)
            }
    }
    fun queryObserveData(){
        FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("age",query_observe_edittext.text.toString())
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                var map = querySnapshot?.documents?.first()?.data as Map<String,Any>
                query_observe_textview.text = map["name"].toString()
            }

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
