package com.invariant.safephrase.source.home;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.invariant.safephrase.source.phrase.PhraseService;
import com.invariant.safephrase.GUI.dialog.AddPhraseDialog;
import com.invariant.safephrase.source.permissions.Permissions;
import com.invariant.safephrase.source.phrase.Phrase;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.invariant.safephrase.source.phrase.Phrase.PhraseAction;

/**
 * Manages administration aspects of phrases such as:
 * <p>
 * <li> Adding phrases </li>
 * <li>  Saving the list of phrases </li>
 * <li>  Loading the list of phrases </li>
 */
public class HomeModel implements AddPhraseDialog.PhraseDialogListener {


    /* ************************************************************************* *
     *                                                                           *
     * Instance Variables                                                        *
     *                                                                           *
     * ************************************************************************  */

    /** Contains a list of phrases for each type of phrase */
    public HashMap<Phrase.PhraseAction, List<Phrase>> phrases_map;

    /** The currently displaying (selected) phrase list */
    private PhraseAction current_phrase_action;

    /** The current context */
    private Context context;

    /** The broadcast link to the PhraseService */
    private LocalBroadcastManager local_broadcast_manager;


    /* ************************************************************************* *
     *                                                                           *
     * Constructors                                                              *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Initializes the home model.
     *
     * @param context the current context
     */
    public HomeModel(Context context) {
        this.context = context;
        local_broadcast_manager = LocalBroadcastManager.getInstance(context);

        /* Init or load the list of phrases */
        loadPhraseLists();

        /* Set the current phrase action the user is looking at on the UI */
        current_phrase_action = Phrase.PhraseAction.CAMERA;

        /* Start the recognizer service if isn't already running, make sure we have recording permissions */
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            if (!PhraseService.isRunning) {
                Intent i = new Intent(context, PhraseService.class);
                context.startService(i);
            }
        }
    }


    /* ************************************************************************* *
     *                                                                           *
     * Instance Methods                                                          *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Add phrase to the associated phrase list and restart the recognizer so it can listen to it.
     *
     * @param phrase
     */
    @Override
    public void onAddPhrase(Phrase phrase) {

        /* Add the phrase to its corresponding list in the phrases_map */
        phrases_map.get(phrase.getType()).add(phrase);

        /* Also add the phrase to the global list of phrases */
        PhraseService.phrases.add(phrase);

        /* Let the PhraseService know to restart the speach recognizer */
        Intent i = new Intent(PhraseService.PHRASE_SERVICE_BROADCAST_ID);
        i.putExtra("command", "RESTART RECOGNIZER");
        local_broadcast_manager.sendBroadcast(i);
    }

    /**
     * Save all the phrases in the list views.
     */
    public void savePhraseLists(){

        SharedPreferences.Editor editor = context.getSharedPreferences("Home", Context.MODE_PRIVATE).edit();
        PhraseService.phrases = new ArrayList<>();

        /* Initialize all the list of phrases inside the phrases map */
        for(PhraseAction key : phrases_map.keySet()){
            PhraseService.phrases.addAll(phrases_map.get(key));
        }

        /* Save the list of phrases as a Json string */
        editor.putString("phrases_list", new Gson().toJson(PhraseService.phrases));
        editor.apply();
    }

    /**
     * Init/Load up all the saved phrases into the list views to display to the user.
     */
    private void loadPhraseLists() {
        SharedPreferences shared_pref = context.getSharedPreferences("Home", Context.MODE_PRIVATE);

        phrases_map = new HashMap<>();

        /* Initialize all the list of phrases inside the phrases map */
        for (PhraseAction key : PhraseAction.values()) {
            phrases_map.put(key, new ArrayList<Phrase>());
        }

        /* If there exists a saved version of the list of phrases, then load it */
        if (shared_pref.contains("phrases_list")) {
            Gson gson = new Gson();
            PhraseService.phrases = new ArrayList<>();

            /* Load up the phrases Json string and parse it as a Json array */
            String phrases_list_str = shared_pref.getString("phrases_list", "");
            JsonArray jsonArray = new JsonParser().parse(phrases_list_str).getAsJsonArray();

            /* Put every element from the Json array into the list of phrases*/
            for (JsonElement e : jsonArray)
                PhraseService.phrases.add(gson.fromJson(e, Phrase.class));

            /* For each phrase in the list of phrases, place it inside it's corresponding list in the phrases map */
            for (Phrase phrase : PhraseService.phrases) {
                PhraseAction type = phrase.getType();
                phrases_map.get(type).add(phrase);
            }
        }
    }

    /**
     * Request the given permission from Permissions.
     *
     * @param permission the permission to request from Permissions
     */
    public void requestionPermission(Activity activity, String permission) {
        switch (permission){
            case "RECORD_AUDIO":
                Permissions.requestRecordAudioPermission(activity);
                break;
            case "CAMERA":
                Permissions.requestCameraPermission(activity);
                break;
            case "CALL_PHONE":
                Permissions.requestPhonePermission(activity);
                break;
        }
    }

    /* ************************************************************************* *
     *                                                                           *
     * Getters & Setter                                                          *
     *                                                                           *
     * ************************************************************************  */

    public PhraseAction getCurrentPhraseAction() {
        return current_phrase_action;
    }

    public void setCurrentPhraseAction(PhraseAction current_phrase_action) {
        this.current_phrase_action = current_phrase_action;
    }
}
