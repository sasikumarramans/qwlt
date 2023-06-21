package com.qwlt;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.qwlt.database.MainDatabase;
import com.qwlt.database.StepCounterDB;
import com.qwlt.database.StepCounterDao;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StepsCountHelper implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int MY_PERMISSIONS_REQUEST_ACTIVITY_RECON = 1;

    public StepCounterDao dayDao;
    public static StepsCountHelper stepCountHelper;
    public Activity context;

    public static StepsCountHelper getInstance(Activity context) {
        if (stepCountHelper == null) {
            stepCountHelper = new StepsCountHelper(context);
        }
        return stepCountHelper;

    }

    public StepsCountHelper(Activity context) {
        startStepCountService();
        this.context = context;
    }

    public void startStepCountService() {
        reqActivityReconPermission(context);
    }
    public void startService(){
        //val messenger=Messenger(handler)
        Intent stepCountService = new Intent(context, StepCountService.class);
        //stepCountService.putExtra("MESSENGER",messenger)
        context.startService(stepCountService);
        dayDao = MainDatabase.getInstance(context).getStepCounterDao();
    }

    public int getStepLive() {
        return Math.round(dayDao.getLatestObservable().getSteps());
    }

    public String getSkippedSteps() {//dateformat-2023-03-14
        String startDate = getBeforeDate("YYYY-MM-DD");
        String endDate = getCurrentDate("YYYY-MM-DD");
        List<StepCounterDB> list = dayDao.getStepCountByDate(startDate, endDate);
        //var  stepCountList= ArrayList<StepCount>()
        JSONArray returnObj = new JSONArray();
        try {
            for (StepCounterDB obj : list) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("date", obj.getDate());
                jsonObject.put("steps", obj.getSteps());
                returnObj.put(jsonObject);
            }
        } catch (Exception e) {
            e.getMessage();
        }
        return returnObj.toString();
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
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(context,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        MY_PERMISSIONS_REQUEST_ACTIVITY_RECON);
            } else {

                if (ContextCompat.checkSelfPermission(context,
                        "com.google.android.gms.permission.ACTIVITY_RECOGNITION") == PackageManager.PERMISSION_DENIED) {

                    ActivityCompat.requestPermissions(context,
                            new String[]{("com.google.android.gms.permission.ACTIVITY_RECOGNITION")},
                            MY_PERMISSIONS_REQUEST_ACTIVITY_RECON);
                }
            }

        }else {
            startService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_ACTIVITY_RECON) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService();
            }
        }
    }
}
