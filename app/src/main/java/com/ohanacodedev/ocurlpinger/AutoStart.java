package com.ohanacodedev.ocurlpinger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoStart extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            AlarmSchedule backgroundService = new AlarmSchedule();
            backgroundService.setAlarm(context);
        }
    }
}
