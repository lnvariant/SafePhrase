package com.invariant.safephrase.GUI.home;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.invariant.safephrase.GUI.dialog.AddPhraseDialog;
import com.invariant.safephrase.source.phrase.PhraseService;
import com.invariant.safephrase.GUI.settings.Settings;
import com.invariant.safephrase.R;
import com.invariant.safephrase.source.home.HomeModel;
import com.invariant.safephrase.source.permissions.Permissions;
import com.invariant.safephrase.source.phrase.Phrase;
import com.invariant.safephrase.source.phrase.Phrase.PhraseAction;
import com.invariant.safephrase.source.phrase.PhraseAdapter;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {


    /* ************************************************************************* *
     *                                                                           *
     * Instance Variables                                                        *
     *                                                                           *
     * ************************************************************************  */

    /** The model (backend) for this activity (frontend) */
    private HomeModel home_model;

    /** The orange slider (initial position is under the camera button) */
    private TextView selection_slider;

    /** The currently selected button */
    private ImageView selected_btn;

    /** The list view component displaying the currently selected list */
    private RecyclerView phrase_list_view;

    /** English dicitionary */
    public static ArrayList<String> english_dict;


    /* ************************************************************************* *
     *                                                                           *
     * Constructors                                                              *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Initializes the Home Activity.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        home_model = new HomeModel(this);

        /* Initialize the views and load the camera phrase list */
        initializeViews();
        loadPhraseList(home_model.phrases_map.get(PhraseAction.CAMERA));

        /* Get any intents that were sent when starting this activity */
        Intent i = getIntent();

        /* If the intent requires a permission to be requested, then request it */
        if(i.hasExtra("PERMISSION")){
            home_model.requestionPermission(this, i.getStringExtra("PERMISSION"));
        }
        /* Otherwise, request all the required permissions for the app */
        else{
            Permissions.requestAllPermissions(this);
        }
    }


    /* ************************************************************************* *
     *                                                                           *
     * Instance Methods                                                          *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Initializes all the views in the activity which need to be referenced.
     */
    private void initializeViews(){
        selection_slider = (TextView) findViewById(R.id.selection_slider);
        selected_btn = (ImageView) findViewById(R.id.camera_btn);
        selected_btn.setEnabled(false);

        /* Set up Recycler List View */
        phrase_list_view = (RecyclerView) findViewById(R.id.phrase_list_view);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        phrase_list_view.setLayoutManager(llm);
    }

    /**
     * Load the list of phrases for the currently selected phrase action.
     *
     * @param phrase_list list of phrases to load
     */
    public void loadPhraseList(List<Phrase> phrase_list){
        if(phrase_list_view.getAdapter() == null) {
            PhraseAdapter phrase_adapter = new PhraseAdapter(getApplicationContext(), phrase_list);
            phrase_list_view.setAdapter(phrase_adapter);
            Log.i("COS","NEW ADAPTER");
        }
        else{
            PhraseAdapter phrase_adapter = (PhraseAdapter) phrase_list_view.getAdapter();
            phrase_adapter.setPhraseList(phrase_list);
            phrase_adapter.notifyDataSetChanged();
            Log.i("COS","REFRESH ADAPTER");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        home_model.savePhraseLists();

    }


    /**
     * Called when user has finished granting or denying permissions.
     *
     * @param requestCode  the associated code to the permission that was just granted/denied
     * @param permissions the permissions that were granted/denied
     * @param grantResults the results of the permissions request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        /* If only the camera permission was granted, then make sure we also have permission for
         * for the system alert window */
        if(requestCode == 1701 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Permissions.requestDrawOverlayPermission(this);
        }

        /* Find the RECORD_AUDIO permission and make sure it was granted */
        for(int p = 0; p < permissions.length; p++){
            if(permissions[p].equals(android.Manifest.permission.RECORD_AUDIO)){

                if (grantResults.length > 0 && grantResults[p] == PackageManager.PERMISSION_GRANTED) {

                    /* Start the recognizer service if isn't already running */
                    if(!PhraseService.isRunning) {
                        Intent i = new Intent(getApplicationContext(), PhraseService.class);
                        getApplicationContext().startService(i);
                    }

                } else{

                    /* If permission wasn't granted, then just go to the settings activity */
                    Intent i = new Intent(this, Settings.class);
                    startActivity(i);
                }

                Permissions.requestDrawOverlayPermission(this);
                return;
            }
        }
    }


    /* ************************************************************************* *
     *                                                                           *
     * Button Instance Methods                                                   *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Display the correct phrase list upon the user choosing the action's phrases they want to view.
     *
     * @param view view that was clicked
     */
    public void onActionBtnClick(View view){

        /* Animate and perform UI changes */
        ImageView pressed_btn = (ImageView) view;
        selection_slider.animate().x(pressed_btn.getX()).start();
        pressed_btn.setEnabled(false);
        selected_btn.setEnabled(true);
        selected_btn = pressed_btn;

        /* Load the phrase list corresponding to the button clicked */
        switch (view.getId()){
            case R.id.camera_btn:
                home_model.setCurrentPhraseAction(PhraseAction.CAMERA);
                loadPhraseList(home_model.phrases_map.get(PhraseAction.CAMERA));
                break;
            case R.id.video_btn:
                home_model.setCurrentPhraseAction(PhraseAction.VIDEO);
                loadPhraseList(home_model.phrases_map.get(PhraseAction.VIDEO));
                break;
            case R.id.record_btn:
                home_model.setCurrentPhraseAction(PhraseAction.AUDIO);
                loadPhraseList(home_model.phrases_map.get(PhraseAction.AUDIO));
                break;
            case R.id.phone_btn:
                home_model.setCurrentPhraseAction(PhraseAction.PHONE);
                loadPhraseList(home_model.phrases_map.get(PhraseAction.PHONE));
                break;
        }
    }

    /**
     * Show a dialog allowing the user to add the phrase. Add the phrase sent by the dialog.
     *
     * @param view view that was clicked
     */
    public void onAddBtnClick(View view){
        AddPhraseDialog add_phrase_dialog = new AddPhraseDialog();
        AddPhraseDialog.current_phrase_type = home_model.getCurrentPhraseAction();
        add_phrase_dialog.phrase_dialog_listener = home_model;
        add_phrase_dialog.english_dict = english_dict;
        add_phrase_dialog.show(getFragmentManager().beginTransaction(), "add_phrase_dialog");
    }

    /**
     * Go to the settings activity when the user clicks on the settings button.
     *
     * @param view the view that was clicked
     */
    public void onSettingsBtnClick(View view){
        startActivity(new Intent (this, Settings.class));
    }
}

