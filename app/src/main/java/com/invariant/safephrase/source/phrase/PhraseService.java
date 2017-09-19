package com.invariant.safephrase.source.phrase;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.invariant.safephrase.GUI.home.Home;
import com.invariant.safephrase.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

/**
 * Contains the speech recognizer that listens for any phrases said by the user.
 */
public class PhraseService extends Service implements RecognitionListener {


    /* ************************************************************************* *
     *                                                                           *
     * Instance Variables                                                        *
     *                                                                           *
     * ************************************************************************  */

    /**
     * The list of phrases that are currently being listened to
     */
    public static List<Phrase> phrases;

    /**
     * The current context of this service
     */
    private Context context;

    /**
     * Recognizes speech
     */
    public SpeechRecognizer recognizer;

    /**
     * Whether audio is being recorded
     */
    public boolean isRecording = false;

    /**
     * Whether this service is running
     */
    public static boolean isRunning = false;

    /**
     * The broadcast ID required to communicate to and from this service
     */
    public static final String PHRASE_SERVICE_BROADCAST_ID = "com.invariant.safephrase.PHRASE_SERVICE_BROADCAST_ID";

    /**
     * The broadcast receiver
     */
    private BroadcastReceiver command_broadcast_receiver;


    /* ************************************************************************* *
     *                                                                           *
     * Constructors                                                              *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Initializes the service to prepare for listening to command broadcasts, and to display a
     * notification on the user's notif bar to let them know this service is running.
     *
     * @param intent the intent this service was run with
     * @param flags flags this service requires
     * @param startId the id of this service
     * @return whatever the onStartComamnd of a Service is suppose to return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.context = getApplicationContext();
        PhraseService.isRunning = true;

        Log.i("COS", "STARTED SELF");

        checkPermissions();
        phrases = new ArrayList<>();

        /* Handle any command sent by the app here */
        command_broadcast_receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {handleCommandBroadcastReceive(context, intent);}};

        LocalBroadcastManager.getInstance(this).registerReceiver(command_broadcast_receiver, new IntentFilter(PHRASE_SERVICE_BROADCAST_ID));

        /* Display a notification that lets the user know that the service is lisening for phrases */
        Intent notif_intent = new Intent(this, Home.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, notif_intent, 0);
        Notification notif = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Phrase Listener")
                .setContentText("Listening for Phrases")
                .setContentIntent(pi)
                .build();

        startForeground(34994, notif);
        setupRecognizer();
        return super.onStartCommand(intent, flags, startId);
    }


    /* ************************************************************************* *
     *                                                                           *
     * Instance Methods                                                          *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Handles the sent broadcast by performing the required command.
     *
     * @param context the context of the broadcast
     * @param intent  the intent that came with the broadcast
     */
    private void handleCommandBroadcastReceive(Context context, Intent intent) {
        try {
            String command = intent.getStringExtra("command");

            /* Based on the command given, perform an action */
            switch (command) {
                case "RESTART RECOGNIZER":
                    recognizer.shutdown();
                    setupRecognizer();
                    break;
                case "STOP":
                    Toast.makeText(context, "Phrase Listener Stopped", Toast.LENGTH_SHORT).show();
                    cleanUp(true);
                    break;
            }
        } catch (Exception ignored) {}
    }

    /**
     * Called when this service is destroyed. Unregisters the this service from listening to
     * broadcasts.
     */
    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(command_broadcast_receiver);
        cleanUp(false);
        super.onDestroy();
    }

    /**
     * Request audio permissions if there aren't any.
     */
    private void checkPermissions(){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            Intent i = new Intent(this, Home.class);
            i.putExtra("PERMISSION", "RECORD_AUDIO");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            cleanUp(true);
        }
    }

    /**
     * Cleans the service to be destroyed. Stops the service if required.
     *
     * @param stopService whether to stop the service while cleaning up <b>(MUST BE FALSE when calling from onDestroy())</b>
     */
    private void cleanUp(boolean stopService){
        if(recognizer != null) {
            recognizer.shutdown();
        }
        PhraseService.isRunning = false;

        if(stopService) {
            stopForeground(true);
            stopSelf();
        }
    }


    /* ************************************************************************* *
     *                                                                           *
     * Speech Recogonition Instance Methods                                      *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Sets up the speech recognizer.
     */
    public void setupRecognizer() {
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(context);
                    File assetDir = assets.syncAssets();
                    Log.i("COS", "Setting up recognizer");
                    setupRecordRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result == null) {
                    switchSearch("phrases");
                }
            }
        }.execute();
    }


    /**
     * Set up the speech recognizer to record with various settings. Start listening for key phrases.
     *
     * @param assetsDir the directory containing all the important speech assets
     * @throws IOException
     */
    private void setupRecordRecognizer(File assetsDir) throws IOException {
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                //.setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .setKeywordThreshold(1e-45f) // Threshold to tune for keyphrase to balance between false alarms and misses
                .setBoolean("-allphone_ci", true)  // Use context-independent phonetic search, context-dependent is too slow for mobile
                .getRecognizer(isRecording);
        recognizer.addListener(this);
        File key_phrases = new File(assetsDir, "key_phrases.gram");
        recognizer.addKeywordSearch("phrases", key_phrases);
    }


    /**
     * Stop the recognizer and begin listening to the provided searchName
     *
     * @param searchName the name associated with the key phrase to listen for
     */
    private void switchSearch(String searchName) {
        recognizer.stop();
        recognizer.startListening(searchName);
    }

    /**
     * Called when the speech recognizer reeceives a partial result of what was said.
     *
     * @param hypothesis the possible peice of speech said
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            Log.i("COS", "PARTIAL RESULT IS: " + hypothesis.getHypstr());
            switchSearch("phrases");
        }
    }

    /**
     * When a phrase is confirmed to have been said, then trigger the phrase's action.
     *
     * @param hypothesis the possible peice of speech said
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            Log.i("COS", "FINAL RESULT IS: " + hypothesis.getHypstr());

            /* Find the phrase object that maches the phrase said */
            Phrase phrase = checkPhraseIn(hypothesis.getHypstr());

            /* If we find the phrase, then trigger its action */
            if (phrase != null) {
                phrase.triggerAction(context, this);
            }
        }
    }

    /**
     * Return the Phrase instancee associated with the given string if it is available in the list
     * of phrases.
     *
     * @param phrase_str the phrase to look for
     * @return a phrase object pertaining to the phrase
     */
    public Phrase checkPhraseIn(String phrase_str) {

        /* For each phrase in phrases, check to see if it is the phrase we're looking for */
        for (Phrase phrase : PhraseService.phrases) {
            if (phrase.getPhraseText().equals(phrase_str.trim())) {
                return phrase;
            }
        }

        return null;
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i("COS", "BEGIN OF SPEECH");
    }

    @Override
    public void onEndOfSpeech() {
        Log.i("COS", "END OF SPEECH: " + recognizer.getSearchName());
    }

    @Override
    public void onError(Exception e) {}

    @Override
    public void onTimeout() {}

    @Override
    public IBinder onBind(Intent intent) {return null;}

}
