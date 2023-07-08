package com.example.bello.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.bello.R
import com.example.bello.firebase.FirestoreClass
import com.example.bello.model.User
import com.example.bello.utils.Constants
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.IOException

class MyProfileActivity : AppCompatActivity() {
    private var profileSelectedImageURI: Uri?=null
    private var profileImageURL:String=""
    private lateinit var userDetail:User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        setUpActionBar()
        FirestoreClass().loadUserData(this)

        val profile_image: ImageView = findViewById(R.id.iv_profile_user_image)
        profile_image.setOnClickListener{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
            == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        val profile_update_btn:Button = findViewById(R.id.update_btn)
        profile_update_btn.setOnClickListener{
            if(profileSelectedImageURI !=null){
                uploadUserImage()
            }else{
                updateUserProfileData()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //To Show a image choose
                Log.d("Permission","Permission allowed.")
                Constants.showImageChooser(this)
            }else{
                Log.d("Permission","Permission denied.")
                Toast.makeText(this,
                    "Oops, You just deny the permission, the app can not access to your storage.",
                Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val profile_image: ImageView = findViewById(R.id.iv_profile_user_image)
        Log.d("MYProfile","Error has occured.")
        if (resultCode == Activity.RESULT_OK &&
            requestCode == Constants.PICK_IMAGE_REQUEST_CODE &&
                data!!.data != null){
                profileSelectedImageURI=data.data
            try {
                Glide
                    .with(this)
                    .load(profileSelectedImageURI)
                    .centerCrop()
                    .placeholder(R.drawable.ic_nav_user)
                    .into(profile_image)
            }catch (e:IOException){
                Log.d("MYProfile","Error has occured.")
                e.printStackTrace()
            }

        }
    }

    private fun updateUserProfileData(){
        val userHashMap = HashMap<String,Any>()


        val profile_name:EditText = findViewById(R.id.profile_username)
        val profile_mobile:EditText = findViewById(R.id.profile_mobile)

        if(profileImageURL.isNotEmpty() && profileImageURL != userDetail.image){
            userHashMap[Constants.IMAGE] = profileImageURL
        }
        if(profile_name.text.toString() != userDetail.name){
            userHashMap[Constants.NAME] = profile_name.text.toString()
        }

        if(profile_mobile.text.toString() != userDetail.mobile.toString()){
            userHashMap[Constants.MOBILE] = profile_mobile.text.toString().toLong()
        }

        FirestoreClass().updateUserProfileData(this,userHashMap)
    }

    private fun setUpActionBar(){
        val toolbar: Toolbar = findViewById(R.id.toolbar_profile)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_new_24)
            actionBar.title=resources.getString(R.string.my_profile_title)
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun setUserDataInProfile(user:User){
        userDetail = user
        val profile_name:EditText = findViewById(R.id.profile_username)
        val profile_mobile:EditText = findViewById(R.id.profile_mobile)
        val profile_email:EditText = findViewById(R.id.profile_email)
        val profile_image: ImageView = findViewById(R.id.iv_profile_user_image)
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_nav_user)
            .into(profile_image)

        profile_name.setText(user.name)
        profile_email.setText(user.email)
        if(user.mobile != null){
            profile_mobile.setText(user.mobile.toString())
        }
    }

    private fun uploadUserImage(){
        if(profileSelectedImageURI !=null) {
            val sRef:StorageReference? = Firebase.storage
                .reference.child("USER_IMAGE"+System.currentTimeMillis()
                        +"."+Constants.getFileExtension(this,profileSelectedImageURI))

            sRef?.putFile(profileSelectedImageURI!!)?.addOnSuccessListener { taskSnapshot ->
                Log.i(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.i("Downloadable Image URL", uri.toString())
                    profileImageURL = uri.toString()

                    updateUserProfileData()
                }
            }?.addOnFailureListener{ exception->
                Toast.makeText(this,
                    exception.message,Toast.LENGTH_LONG).show()
            }
        }
    }

    fun profileUpdateSuccess(){
        setResult(Activity.RESULT_OK)
        finish()
    }
}