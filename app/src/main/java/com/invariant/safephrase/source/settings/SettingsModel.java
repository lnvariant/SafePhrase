package com.invariant.safephrase.source.settings;


import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.invariant.safephrase.source.phrase.PhraseService;

/**
 * Controls the various settings of the app.
 */
public class SettingsModel {


    /* ************************************************************************* *
     *                                                                           *
     * Instance Variables                                                        *
     *                                                                           *
     * ************************************************************************  */

    private Context context;
    private LocalBroadcastManager local_broadcast_manager;


    /* ************************************************************************* *
     *                                                                           *
     * Static Variables                                                          *
     *                                                                           *
     * ************************************************************************  */

    /** Whether to vibrate the phone when a phrase is triggered */
    public static boolean vibrate_on_phrase = true;

    /** Whether to ring the phone when a phrase is triggered */
    public static boolean ring_on_phrase;

    /** Whether to display a timer when recording video/audio */
    public static boolean display_timer;


    /* ************************************************************************* *
     *                                                                           *
     * Constructors                                                              *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Initialize the settings.
     *
     * @param context the context of the app
     */
    public SettingsModel(Context context) {
        this.context = context;
        local_broadcast_manager = LocalBroadcastManager.getInstance(context);
    }


    /* ************************************************************************* *
     *                                                                           *
     * Instance Methods                                                          *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Handle the triggered settings switch by performing the necessery actions pertaining to it.
     *
     * @param settings_switch the switch that was triggered
     * @param status true if the switch was switched on
     */
    public void handleSetting(Setting settings_switch, boolean status){
        switch (settings_switch){
            /* Whether to enable or disable the PhraseService */
            case POWER:
                handlePhraseService(status);
                break;
            /* Whether to vibrate phone when a phrase action is triggered */
            case VIBRATE:
                vibrate_on_phrase = status;
                break;
            /* Whether to ring the phone when a phrase action is triggered */
            case RING:
                ring_on_phrase = status;
                break;
            /* Whether to display a video/audio timer */
            case TIMER:
                display_timer = status;
                break;
        }
    }

    /**
     * Turns the PhraseService on or off based on status.
     *
     * @param status whether the service should be on (true) or off (false)
     */
    private void handlePhraseService(boolean status){
        if(status) {

            /* Don't start the service if it is already running */
            if(PhraseService.isRunning) return;

            Intent i = new Intent(context, PhraseService.class);
            context.startService(i);
        }
        else{

            /* Don't stop the service if it is already stopped */
            if(!PhraseService.isRunning) return;

            Intent i = new Intent(PhraseService.PHRASE_SERVICE_BROADCAST_ID);
            i.putExtra("command", "STOP");
            local_broadcast_manager.sendBroadcast(i);
        }
    }


    /* ************************************************************************* *
     *                                                                           *
     * Enums                                                                     *
     *                                                                           *
     * ************************************************************************  */

    /**
     * The switches available in the settings activity
     */
    public enum Setting{
        POWER, VIBRATE, RING, TIMER
    }
}
