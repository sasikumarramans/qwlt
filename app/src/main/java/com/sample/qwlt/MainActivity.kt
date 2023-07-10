package com.sample.qwlt

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.qwlt.StepCountListener
import com.qwlt.StepsCountHelper
import com.sample.qwlt.databinding.ActivityMainBinding
import kotlin.math.log
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(),StepCountListener {
    private  val PERMISSIONS_REQUEST_ACCESS_ACTIVITY_RECOGNITION = 2
    var stepCountHelper:StepsCountHelper?=null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        startCountService()
        //activityRecognitionPermission()

    }
    fun startCountService(){
        StepsCountHelper.getInstance(this);
        stepCountHelper= StepsCountHelper.stepCountHelper

        stepCountHelper?.getStepLive()
        binding.totalSteps.setText("Total Steps: "+ stepCountHelper?.getStepLive())
        binding.currentSteps.setText("Skipped steps: "+ stepCountHelper?.getSkippedSteps())
        binding.currentSteps.setText("Skipped steps: "+ stepCountHelper?.getLocationLive())
    }

    override fun currentStepCount(count: Float) {
        binding.totalSteps.setText("Current Steps: "+stepCountHelper?.getStepLive())
        binding.currentSteps.setText(count.roundToInt().toString())

    }
    private fun activityRecognitionPermission(){
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
   /* override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d("TAG", "onRequestPermissionsResult1: ")
       *//* when(requestCode){
            PERMISSIONS_REQUEST_ACCESS_ACTIVITY_RECOGNITION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCountService()
                }
            }
            else -> *//*super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //}
    }*/
}