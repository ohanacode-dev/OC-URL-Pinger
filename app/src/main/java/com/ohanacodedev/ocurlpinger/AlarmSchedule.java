package com.ohanacodedev.ocurlpinger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class AlarmSchedule extends BroadcastReceiver
{
    private static final String TAG = "ALARM_SCHEDULE";
    private boolean keepRunningFlag = false;    /* Whether to stop recurrence of the alarm */
    private static final int alarmInterval = 30000;
    private List<String> urlStringList;
    private int executedUrlIndex = 0;

    private class GetUrlContentTask extends AsyncTask<String, Integer, String> {
        protected static final String TAG = "GetUrl";

        protected String doInBackground(String... urls) {
            try{
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(false);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();

                return "OK";
            }catch(Exception ex){
                return ("ERROR: " + ex);
            }
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            // this is executed on the main thread after the process is over
            // update your UI here
            if(result.startsWith("ERROR")) {
                Log.d(TAG, "onPostExecute: " + result);

            }else {
                Log.d(TAG, "Success: " + result);
            }

            executedUrlIndex++;
            if(urlStringList.size() > executedUrlIndex){
                new GetUrlContentTask().execute(urlStringList.get(executedUrlIndex));
            }
        }
    }


    @Override
    public void onReceive(Context context, Intent intent)
    {/* The alarm service has been called by the system */

        /* Read saved preferences */
        getPreferences(context);

        if(keepRunningFlag){    /* The service is active */

            Log.d(TAG, "Activated!!!");
            if(urlStringList.size() > 0){
                new GetUrlContentTask().execute(urlStringList.get(0));
            }
        }else {
            /* Request was made to stop the alarm service */
            cancelAlarm(context);
        }
    }

    public void setAlarm(Context context)
    {
        Log.d("AlarmSchedule", "Starting recurring alarm service");

        AlarmManager alarmManager =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmSchedule.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), alarmInterval, pi);

    }

    public void cancelAlarm(Context context)
    {
        Log.d(TAG, "Stopping recurring alarm service");

        Intent alarmIntent = new Intent(context, AlarmSchedule.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);

    }

    private void getPreferences(Context context){
        try {
            SharedPreferences prefs= context.getSharedPreferences("com.ohanacodedev.ocurlpinger", Context.MODE_PRIVATE);

            keepRunningFlag = prefs.getBoolean("persistent", false);

            urlStringList.clear();
            for (String item: prefs.getString("urllist", "").split("~|~")) {
                urlStringList.add(item);
            }

        } catch (NullPointerException e) {
            Log.e(TAG, "error reading preferences: " + e.getMessage());
        }
    }

}
