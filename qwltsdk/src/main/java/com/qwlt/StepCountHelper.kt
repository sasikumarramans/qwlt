package com.qwlt

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import com.qwlt.database.MainDatabase
import com.qwlt.database.StepCounterDao
import com.qwlt.model.StepCount

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
    fun startStepCount(stepCountListener: StepCountListener){
        mStepCountListener=stepCountListener
        val handler: Handler = object : Handler(Looper.myLooper()!!) {
            override fun handleMessage(message: Message) {
                val count: Float = message.obj as Float
                count.let {
                    stepCountListener.currentStepCount(count)
                }
            }
        }
        val messenger=Messenger(handler)
        val stepCountService=Intent(mContext,StepCountService::class.java)
        stepCountService.putExtra("MESSENGER",messenger)
        mContext.startService(stepCountService)
        dayDao = MainDatabase.getInstance(mContext).stepCounterDao
    }

    fun getTodayTotalStepCount():Float {return dayDao.getLatestObservable().steps}
    /*fun getStepsCountByDate(startDate:String,endDate:String):List<StepCount> {
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
    fun getStepsCountByDate(startDate:String):List<StepCount> {
        val list=dayDao.getStepCountByDate(startDate)
        var  stepCountList= ArrayList<StepCount>()
        for (obj in list){
            var stepCount=StepCount()
            stepCount.date=obj.date
            stepCount.stepCount=obj.steps
            stepCountList.add(stepCount)
        }
        return stepCountList
    }*/
}
