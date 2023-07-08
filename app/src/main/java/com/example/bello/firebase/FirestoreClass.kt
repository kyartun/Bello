package com.example.bello.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.bello.activities.CreateBoardActivity
import com.example.bello.activities.MainActivity
import com.example.bello.activities.MyProfileActivity
import com.example.bello.activities.SignInActivity
import com.example.bello.activities.SignUpActivity
import com.example.bello.fragments.CreatorFragment
import com.example.bello.model.Board
import com.example.bello.model.User
import com.example.bello.utils.Constants
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity:SignUpActivity, userInfo: User){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisterSuccess()
            }
            .addOnFailureListener{
               e->
                Log.e(activity.javaClass.simpleName,"Error occuring in insert user document.")
            }
    }

    fun createBoard(activity:CreateBoardActivity, board: Board){
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
//                activity.userRegisterSuccess()
                Log.d(activity.javaClass.simpleName,"board has been successfully created.")
                Toast.makeText(activity,"Board has been created successfully.",Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener{
                    e->
                Log.e(activity.javaClass.simpleName,"Error occuring in creationg board.")
            }
    }

    fun updateUserProfileData(activity:MyProfileActivity,userHashMap:HashMap<String,Any>){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"Update profile successfully.")
                Toast.makeText(activity,"Update profile successfully.",Toast.LENGTH_SHORT).show()
                activity.profileUpdateSuccess()
            }.addOnFailureListener{
                e->
                Log.e(activity.javaClass.simpleName,"Failure on updating profile.")
                Toast.makeText(activity,"Update profile fail.",Toast.LENGTH_SHORT).show()
            }
    }

    fun loadUserData(activity:Activity){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener {document->
                val loggedUser = document.toObject(User::class.java)!!

                when (activity) {
                    is SignInActivity -> {
                        activity.signInSuccess(loggedUser)
                    }
                    is MainActivity->{
                        Log.d("mUsername","Load in LoadUserData")
                        activity.updateNavigationUserDetails(loggedUser)
                    }
                    is MyProfileActivity->{
                        activity.setUserDataInProfile(loggedUser)
                    }
                }

            }
            .addOnFailureListener{
                    e->
                Log.e(activity.javaClass.simpleName,"Error occuring in insert user document.")
            }
    }

    fun getCurrentUserId():String{
        var currentUser = Firebase.auth.currentUser
        var currentUserID=""
        if(currentUser != null){
            currentUserID = currentUser.uid
        }
        return currentUserID
//        return Firebase.auth.currentUser!!.uid
//        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun getBoardList(activity: CreatorFragment){
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains("assignedTo",getCurrentUserId())
            .get()
            .addOnSuccessListener {
                document ->
                Log.i(activity.javaClass.simpleName,document.documents.toString())
                val boardList:ArrayList<Board> = ArrayList()
                for(i in document.documents){
                    val board = i.toObject(Board::class.java)
                    board!!.documentId = i.id
                    boardList.add(board)
                }
                activity.populateBoardListToUI(boardList)
            }
            .addOnFailureListener{
                e->
                Log.e(activity.javaClass.simpleName,"Error with get board lists")
            }
    }

    fun getCurrentUser():String{
        var mUsername:String=""
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val loggedUser = document.toObject(User::class.java)!!
                mUsername= loggedUser.name
                Log.d("mUsername", "$mUsername in the success mode.")
            }.addOnFailureListener{
                Log.e("mUserename","$mUsername in the failure mode.")
            }
        Log.d("mUsername"," is the result.")
        return mUsername
    }
}