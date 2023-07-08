package com.example.bello.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.example.bello.R
import com.example.bello.databinding.ActivityTaskListBinding

class TaskListActivity : AppCompatActivity() {
    private lateinit var task_binding: ActivityTaskListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        task_binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(task_binding.root)

        setUpActionBar()
    }

    private fun setUpActionBar(){
        setSupportActionBar(task_binding.toolbarTask)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_new_24)
            actionBar.title=resources.getString(R.string.task_title)
        }

        task_binding.toolbarTask.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}