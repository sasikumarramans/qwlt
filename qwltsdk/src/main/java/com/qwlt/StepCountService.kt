package com.qwlt

import android.Manifest
import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.*
import kotlin.math.roundToInt

const val FOOT_TO_METER = 0.7867
const val FOOT_TO_CALORIE = 0.0667

class StepCountService : Service(), SensorEventListener {
    val TAG = "StepCountService"
    var dbManager: DBManager? = null
    private val SERVICE_RESTART_INTERVAL = 15 * 60 * 1000 // 15 minutes


    private var totalStep: Float = 0f
    private var stepCount: StepCount? = null

    /*  private lateinit var dayDao: StepCounterDao
      private lateinit var stepCounterDB: StepCounterDB*/
    /* private var viewModelJob = Job()
     private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)*/
    private var sensorManager: SensorManager? = null

    //private var messenger:Messenger?=null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        //messenger = intent!!.extras!!.get("MESSENGER") as Messenger
        if (dbManager == null) {
            dbManager = DBManager(this)
            dbManager?.open()
        }
        getCurrentLocation();
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if (p0!!.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            totalStep += p0.values[0]
            Toast.makeText(StepCountService@this,totalStep.toString(),Toast.LENGTH_SHORT).show()
            /*  val msg: Message = Message.obtain()
              msg.obj = totalStep*/
            //messenger?.send(msg)

            createMissingHistoryEntries(stepCount!!)
            if(StepsCountHelper.stepCountInterface!=null){
                StepsCountHelper.stepCountInterface.getStepLive(totalStep.roundToInt())
            }
            dbManager?.updateWhere(totalStep.roundToInt().toString(), stepCount?.date)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    fun createDayRecord() {
        stepCount = dbManager?.latestRecord()
        if (stepCount != null && stepCount?.stepCount != null) {
            totalStep = stepCount?.stepCount!!.toFloat()
        }
        createMissingHistoryEntries(stepCount)
    }

    private fun createMissingHistoryEntries(day: StepCount?) {
        val currentDate = Calendar.getInstance()
        currentDate.set(Calendar.HOUR_OF_DAY, 0)
        currentDate.set(Calendar.MINUTE, 0)
        currentDate.set(Calendar.SECOND, 0)
        currentDate.set(Calendar.MILLISECOND, 0)
        val lastDate = Calendar.getInstance()
        val newDay = StepCount(
        )
        if (day == null) {
            newDay.date = DateFormat.standardFormat(lastDate.time)
            newDay.stepCount = "0"
            stepCount = newDay
            dbManager?.insert(newDay.stepCount, newDay.date)
        } else {
            Log.d(TAG, "createMissingHistoryEntries: " + day.date)
            stepCount = day
            lastDate.time = DateFormat.standardParse(day.date)

            if (lastDate.time < currentDate.time) {
                Log.d(TAG, "createMissingHistoryEntries: " + day.date)
                lastDate.add(Calendar.DATE, 1)

                newDay.date = DateFormat.standardFormat(lastDate.time)
                newDay.stepCount = "0"
                stepCount = newDay
                totalStep=0f
                dbManager?.insert(newDay.stepCount, newDay.date)
            }
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

            val notification: Notification = Notification.Builder(this, channelId)
                .setContentTitle(getString(R.string.AutomaticStepCounting))
                .setContentText(getString(R.string.AutomaticStepCountingText))
                .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
                .build()
            startForeground(R.integer.automaticStepCounting_notification_id, notification)
        }
        if (dbManager == null) {
            dbManager = DBManager(this)
            dbManager?.open()
        }
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        createDayRecord()
        startSensor()
        /*  dayDao = MainDatabase.getInstance(this).stepCounterDao*/

    }

    private fun startSensor() {
        val stepSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        if (stepSensor == null) {
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(
                this,
                stepSensor,
                SensorManager.SENSOR_DELAY_FASTEST
            )
            //Toast.makeText(this, "Set up monitor!!!", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        scheduleServiceRestart()
        super.onTaskRemoved(rootIntent)

    }
    private fun scheduleServiceRestart() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, ServiceRestartReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent,  FLAG_IMMUTABLE)

        // Schedule the alarm to trigger periodically
        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + SERVICE_RESTART_INTERVAL,
            SERVICE_RESTART_INTERVAL.toLong(),
            pendingIntent
        )
    }
    fun getCurrentLocation() {
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(StepsCountHelper.context)
        val cancellationTokenSource = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                StepsCountHelper.context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                StepsCountHelper.context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) StepsCountHelper.strLocation =
                location.latitude.toString() + "," + location.longitude
        }
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 300000
        Looper.myLooper()?.let {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.lastLocation
                        StepsCountHelper.strLocation =
                            locationResult.lastLocation.latitude.toString() + "," + locationResult.lastLocation.longitude
                        if(StepsCountHelper.locationUpdateInterface!=null){
                            StepsCountHelper.locationUpdateInterface.getLocationLive(StepsCountHelper.strLocation)
                        }
                        super.onLocationResult(locationResult)
                    }
                },
                it
            )
        }
    }
}