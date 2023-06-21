package com.sample.qwlt

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.qwlt.StepCountHelper
import com.qwlt.StepCountListener
import com.sample.qwlt.databinding.ActivityMainBinding
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(),StepCountListener {
    private  val PERMISSIONS_REQUEST_ACCESS_ACTIVITY_RECOGNITION = 2
    var stepCountHelper:StepCountHelper?=null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        activityRecognitionPermission()

    }
    fun startCountService(){
        stepCountHelper=StepCountHelper.getInstance(this)
        stepCountHelper?.getStepLive()
        binding.totalSteps.setText("Total Steps: "+ stepCountHelper?.getStepLive())
    }

    override fun currentStepCount(count: Float) {
        binding.totalSteps.setText("Current Steps: "+stepCountHelper?.getStepLive())
        binding.currentSteps.setText(count.roundToInt().toString())

    }
    private fun activityRecognitionPermission(){
        Log.v("TAG", "version sdk : ${Build.VERSION.SDK_INT} and version code : ${Build.VERSION_CODES.Q}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {

                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                    PERMISSIONS_REQUEST_ACCESS_ACTIVITY_RECOGNITION
                )
            }
            else{
                startCountService()
            }
        }
        else{
            Log.v("TAG","seftpermis ${
                ContextCompat.checkSelfPermission(this,
                "com.google.android.gms.permission.ACTIVITY_RECOGNITION")}")
            if (ContextCompat.checkSelfPermission(this,
                    "com.google.android.gms.permission.ACTIVITY_RECOGNITION") == PackageManager.PERMISSION_DENIED){

                ActivityCompat.requestPermissions(this,
                    arrayOf("com.google.android.gms.permission.ACTIVITY_RECOGNITION"),
                    PERMISSIONS_REQUEST_ACCESS_ACTIVITY_RECOGNITION)
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            PERMISSIONS_REQUEST_ACCESS_ACTIVITY_RECOGNITION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCountService()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}