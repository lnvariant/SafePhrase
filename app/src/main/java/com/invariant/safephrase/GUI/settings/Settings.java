package com.invariant.safephrase.GUI.settings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;

import com.invariant.safephrase.source.phrase.PhraseService;
import com.invariant.safephrase.R;
import com.invariant.safephrase.source.settings.SettingsModel;


public class Settings extends AppCompatActivity {


    /* ************************************************************************* *
     *                                                                           *
     * Instance Variables                                                        *
     *                                                                           *
     * ************************************************************************  */

    private SettingsModel settings_model;


    /* ************************************************************************* *
     *                                                                           *
     * Constructors                                                              *
     *                                                                           *
     * ************************************************************************  */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        overridePendingTransition(R.anim.translate_left_in, R.anim.translate_left_out);

        settings_model = new SettingsModel(this);
        initializeSwitches();
    }


    /* ************************************************************************* *
     *                                                                           *
     * Instance Methods                                                          *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Initalize the switches by adding a handler to them where when they are checked, settings_model
     * handles the pertaining actions.
     */
    private void initializeSwitches(){

        Switch power_switch = (Switch) findViewById(R.id.power_switch);
        Switch vibrate_switch = (Switch) findViewById(R.id.vibrate_switch);
        Switch ring_switch = (Switch) findViewById(R.id.ring_switch);
        Switch timer_switch = (Switch) findViewById(R.id.timer_switch);

        /* Assign all the switches' click listeners so they trigger a appropriate setting */
        power_switch.setOnCheckedChangeListener((buttonView, isChecked) -> settings_model.handleSetting(SettingsModel.Setting.POWER, isChecked));
        vibrate_switch.setOnCheckedChangeListener((buttonView, isChecked) -> settings_model.handleSetting(SettingsModel.Setting.VIBRATE, isChecked));
        ring_switch.setOnCheckedChangeListener((buttonView, isChecked) -> settings_model.handleSetting(SettingsModel.Setting.RING, isChecked));
        timer_switch.setOnCheckedChangeListener((buttonView, isChecked) -> settings_model.handleSetting(SettingsModel.Setting.TIMER, isChecked));


        /* Initialize the switches to their corresponding values */
        vibrate_switch.setChecked(SettingsModel.vibrate_on_phrase);
        ring_switch.setChecked(SettingsModel.ring_on_phrase);
        timer_switch.setChecked(SettingsModel.display_timer);

        /* Change the status of the switch based on if PhraseService is runnning */
        if(!PhraseService.isRunning){
            power_switch.setChecked(false);
        }
        else{
            power_switch.setChecked(true);
        }
    }

    /**
     * Go to back to the previous activity upon the user clicking the back button.
     *
     * @param view the view that was clicked
     */
    public void onBackBtnClick(View view){
        finish();
        overridePendingTransition(R.anim.translate_right_in, R.anim.translate_right_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.translate_right_in, R.anim.translate_right_out);
    }
}
