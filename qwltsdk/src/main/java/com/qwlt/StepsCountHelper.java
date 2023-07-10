package com.qwlt;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class StepsCountHelper {
    private static final int MY_PERMISSIONS_REQUEST_ACTIVITY_RECON = 1;

    public static StepsCountHelper stepCountHelper;
    public static Activity context;
    public static StepCountInterface stepCountInterface;
    public static LocationUpdateInterface locationUpdateInterface;
    public static DBManager dbManager;
    public static String strLocation;

    public static void getInstance(Activity context) {
        stepCountHelper = new StepsCountHelper(context);
    }

    public StepsCountHelper(Activity context) {
        this.context = context;
        dbManager = new DBManager(context);
        dbManager.open();
        // dayDao = QwltDatabase.getInstance(context).stepCounterDao();
        startStepCountService();
    }

    public void startStepCountService() {
        reqActivityReconPermission(context);
    }

    public void startService() {
        Intent stepCountService = new Intent(context, StepCountService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(stepCountService);
        } else {
            context.startService(stepCountService);
        }
    }
    public void stepLiveDelegate(StepCountInterface stepIf){
        stepCountInterface=stepIf;
    }
    public void locationLiveDelegate(LocationUpdateInterface locationIf){
        locationUpdateInterface=locationIf;
    }

    public int getStepLive() {
        if (dbManager != null)
            return dbManager.first();
        return 0;
    }

    public String getSkippedSteps() {//dateformat-2023-03-14
        if (dbManager == null) return "";
        return dbManager.last5Records();
    }

    private String getBeforeDate(String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_YEAR, -5);
        Date newDate = calendar.getTime();
        String date = simpleDateFormat.format(newDate);
        return date;
    }

    private String getCurrentDate(String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Date newDate = calendar.getTime();
        String date = simpleDateFormat.format(newDate);
        return date;
    }


    public void reqActivityReconPermission(Activity context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            /*String[] permissions = {Manifest.permission.ACTIVITY_RECOGNITION, Manifest.permission.POST_NOTIFICATIONS};
            int requestCode = 1;*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Log.d("TAG", "reqActivityReconPermission: " + "if");
                   /* for (String permission : permissions) {
                        Log.d("TAG", "reqActivityReconPermission: "+permission);
                        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(context, new String[]{permission}, requestCode++);
                        }
                    }*/
                ActivityCompat.requestPermissions(context,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION, Manifest.permission.POST_NOTIFICATIONS,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACTIVITY_RECON);
            } else {
                Log.d("TAG", "reqActivityReconPermission: " + "if else");
                ActivityCompat.requestPermissions(context,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACTIVITY_RECON);
            }
            Handler handler = new Handler(Looper.myLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        Log.d("TAG", "permission granted");
                        startService();
                    } else {
                        handler.postDelayed(this, 2000);
                    }
                }
            }, 2000);
        } else {
            startService();
        }
    }



    public String getLocationLive(){
        return strLocation;
    }


    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("TAG", "onRequestPermissionsResult: ");
    }*/
}
