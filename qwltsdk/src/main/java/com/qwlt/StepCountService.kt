package com.qwlt

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Transformations
import com.qwlt.database.MainDatabase
import com.qwlt.database.StepCounterDB
import com.qwlt.database.StepCounterDao
import com.qwlt.utils.DateFormat
import com.qwlt.utils.launchIO
import com.qwlt.utils.observeOnce
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.*

const val FOOT_TO_METER = 0.7867
const val FOOT_TO_CALORIE = 0.0667
class StepCountService : Service(), SensorEventListener {
    val TAG="StepCountService"
    private var totalStep: Float = 0f
    private lateinit var dayDao: StepCounterDao
    private lateinit var stepCounterDB: StepCounterDB
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var sensorManager: SensorManager? = null
    private var messenger:Messenger?=null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
         messenger = intent!!.extras!!.get("MESSENGER") as Messenger
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if (p0!!.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            totalStep += p0.values[0]
            val msg: Message = Message.obtain()
            msg.obj = totalStep
            messenger?.send(msg)
            createMissingHistoryEntries(stepCounterDB)
            launchIO(uiScope) {
                dayDao.addLatestSteps(p0.values[0])
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    fun createDayRecord(){

        val days = dayDao.getAllObservable()
        val today = Transformations.map(days) {
            it.first()
        }
        today.observeOnce({ createMissingHistoryEntries(it) })
    }
    private fun createMissingHistoryEntries(day: StepCounterDB) {
        Log.d(TAG, "createMissingHistoryEntries: "+day.date)
        stepCounterDB=day
        val currentDate = Calendar.getInstance()
        currentDate.set(Calendar.HOUR_OF_DAY, 0)
        currentDate.set(Calendar.MINUTE, 0)
        currentDate.set(Calendar.SECOND, 0)
        currentDate.set(Calendar.MILLISECOND, 0)

        val lastDate = Calendar.getInstance()
        lastDate.time = DateFormat.standardParse(day.date)

        if (lastDate.time < currentDate.time) {
            Log.d(TAG, "createMissingHistoryEntries: "+day.date)
            lastDate.add(Calendar.DATE, 1)
            val newDay = StepCounterDB(
                0,
                DateFormat.standardFormat(lastDate.time),
                0f,
            )
            stepCounterDB=newDay
            launchIO(uiScope) { dayDao.insert(newDay) }
        }
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel("qwlt_service", getString(R.string.channel_id))
                } else {
                    // If earlier version channel ID is not used
                    // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                    getString(R.string.channel_id)
                }
            val pendingIntent: PendingIntent =
                Intent(this, MainActivity::class.java).let { notificationIntent ->
                    PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
                }
            val notification: Notification = Notification.Builder(this,channelId )
                .setContentTitle(getString(R.string.AutomaticStepCounting))
                .setContentText(getString(R.string.AutomaticStepCountingText))
                .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
                .setContentIntent(pendingIntent)
                .build()
            startForeground(R.integer.automaticStepCounting_notification_id, notification)
        }
        dayDao = MainDatabase.getInstance(this).stepCounterDao
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        createDayRecord()
        startSensor()
    }
    private fun startSensor(){
        val stepSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        if (stepSensor == null) {
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(
                this,
                stepSensor,
                SensorManager.SENSOR_DELAY_FASTEST
            )
            Toast.makeText(this, "Set up monitor!!!", Toast.LENGTH_SHORT).show()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }
}