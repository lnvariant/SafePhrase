package com.invariant.safephrase.source.other;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

public class InternetCheck {

    /**
     * Checks to see if the internet is available on the user's device. If internet is not available,
     * then it shows an alert directing the user to connect to WIFI if the user has specified to
     * show an alert.
     *
     * @param context activity calling this method
     * @param show_alert true if an alert should be shown directing to the user to connect to WIFI
     * @return true if internet is available
     */
    public static Boolean isInternetAvailable(final Context context, boolean show_alert) {
        try {
            Process p1 = Runtime.getRuntime().exec("ping -c 1    www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal==0);
            if(reachable){
                System.out.println("Internet access");
                return true;
            }
            else{
                System.out.println("No Internet access");
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        /*
        Display alert telling user to connect to internet and then redirect them to WIFI settings
        upon clicking the OK button.
         */
        if(show_alert) {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle("Not connected to the internet");
            alert.setMessage("This action requires your device to be connected to the internet.");
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    context.startActivity(i);
                }
            });
            alert.show();
        }

        return false;
    }
}
