package com.qwlt

import android.content.Context
import android.content.Intent
import com.qwlt.database.MainDatabase
import com.qwlt.database.StepCounterDao
import com.qwlt.model.StepCount
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.math.roundToInt

class StepCountHelper(context: Context) {
    var mContext: Context = context
    var mStepCountListener:StepCountListener?=null
    private lateinit var dayDao: StepCounterDao
    companion object {
        private var instance: StepCountHelper? = null
        fun getInstance(context: Context): StepCountHelper {
            if (instance == null) {
                instance = StepCountHelper(context)
            }

            return instance!!
        }
    }
    fun startStepCountService(){
       /* mStepCountListener=stepCountListener
        val handler: Handler = object : Handler(Looper.myLooper()!!) {
            override fun handleMessage(message: Message) {
                val count: Float = message.obj as Float
                count.let {
                    stepCountListener.currentStepCount(count)
                }
            }
        }*/
        //val messenger=Messenger(handler)
        val stepCountService=Intent(mContext,StepCountService::class.java)
        //stepCountService.putExtra("MESSENGER",messenger)
        mContext.startService(stepCountService)
        dayDao = MainDatabase.getInstance(mContext).stepCounterDao
    }

    fun getStepLive():Int {return dayDao.getLatestObservable().steps.roundToInt()}

    fun getStepsCountByDate(startDate:String,endDate:String):List<StepCount> {//dateformat-2023-03-14
        val list=dayDao.getStepCountByDate(startDate,endDate)
        var  stepCountList= ArrayList<StepCount>()
        for (obj in list){
            var stepCount=StepCount()
            stepCount.date=obj.date
            stepCount.stepCount=obj.steps
            stepCountList.add(stepCount)
        }
        return stepCountList
    }
    fun getSkippedSteps():String {//dateformat-2023-03-14
        val startDate=getBeforeDate("YYYY-MM-DD")
        val endDate=getCurrentDate("YYYY-MM-DD")
        val list=dayDao.getStepCountByDate(startDate,endDate)
        //var  stepCountList= ArrayList<StepCount>()
        var returnObj=JSONArray()
        for (obj in list){
            val jsonObject=JSONObject()
            jsonObject.put("date",obj.date)
            jsonObject.put("steps",obj.steps)
           /* var stepCount=StepCount()
            stepCount.date=obj.date
            stepCount.stepCount=obj.steps*/
            returnObj.put(jsonObject)
        }
        return returnObj.toString()
    }
    fun getBeforeDate(dateFormat: String): String {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_YEAR, -5)
        val newDate = calendar.time
        val date = dateFormat.format(newDate)
        return date
    }
    fun getCurrentDate(dateFormat: String): String {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        val newDate = calendar.time
        val date = dateFormat.format(newDate)
        return date
    }
    fun getStepsCountByDate(startDate:String):List<StepCount> {//dateformat-2023-03-14
        val list=dayDao.getStepCountByDate(startDate)
        var  stepCountList= ArrayList<StepCount>()
        for (obj in list){
            var stepCount=StepCount()
            stepCount.date=obj.date
            stepCount.stepCount=obj.steps
            stepCountList.add(stepCount)
        }
        return stepCountList
    }
}
