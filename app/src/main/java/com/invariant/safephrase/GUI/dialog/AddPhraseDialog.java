package com.invariant.safephrase.GUI.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.invariant.safephrase.R;
import com.invariant.safephrase.source.other.BinarySearch;
import com.invariant.safephrase.source.other.Searcher;
import com.invariant.safephrase.source.phrase.CameraPhrase;
import com.invariant.safephrase.source.phrase.PhonePhrase;
import com.invariant.safephrase.source.phrase.Phrase;
import com.invariant.safephrase.source.phrase.RecordPhrase;

import java.io.IOException;
import java.util.ArrayList;

import edu.cmu.pocketsphinx.Assets;
import fr.tvbarthel.lib.blurdialogfragment.BlurDialogFragment;

public class AddPhraseDialog extends BlurDialogFragment {

    /* ************************************************************************* *
     *                                                                           *
     * Instance Variables                                                        *
     *                                                                           *
     * ************************************************************************  */

    /** The currently selected phrase type */
    public static Phrase.PhraseAction current_phrase_type;

    /** The root view of this dialog */
    private View root_view;

    /** Displays errors */
    private TextView error_lbl;

    /** Name of the phrase given my the input in this field */
    private EditText phrase_field;

    /** Anyone listening to this dialog will add the selected phrase when this dialog is finished. */
    public PhraseDialogListener phrase_dialog_listener;

    /** English Dictionary */
    public ArrayList<String> english_dict;


    /* ************************************************************************* *
     *                                                                           *
     * Constructors                                                              *
     *                                                                           *
     * ************************************************************************  */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root_view = inflater.inflate(R.layout.add_phrase_dialog, container, false);

        /* Initialize the main views */
        ((TextView) root_view.findViewById(R.id.title_lbl)).setText("Add a " + current_phrase_type.toString().toLowerCase() + " phrase");
        error_lbl = (TextView) root_view.findViewById(R.id.error_lbl);
        phrase_field = (EditText) root_view.findViewById(R.id.phrase_field);

        /* Load any extra options views */
        loadExtraOptionsViews();

        /* Set the add_btn's click listener */
        (root_view.findViewById(R.id.add_btn)).setOnClickListener(v -> onAddBtnClick());

        return root_view;
    }


    /* ************************************************************************* *
     *                                                                           *
     * Instance Methods                                                          *
     *                                                                           *
     * ************************************************************************  */


    /**
     * Called when the add button is clicked in the add phrase dialog.
     */
    private void onAddBtnClick(){
        String phrase_txt = phrase_field.getText().toString();

        /* Check to make sure a valid phrase is entered */
        if(!checkPhrase(phrase_txt)){
            return;
        }

        Phrase phrase = null;

        /* Set the extra options for the phrase depending on the selected options in the radio groups */
        switch (current_phrase_type){
            case CAMERA:
                switch (((RadioGroup) root_view.findViewById(R.id.camera_radio_group)).getCheckedRadioButtonId()){
                    case R.id.front_camera_btn:
                        phrase = new CameraPhrase(phrase_txt, CameraPhrase.CameraOption.FRONT_CAMERA);
                        break;
                    case R.id.back_camera_btn:
                        phrase = new CameraPhrase(phrase_txt, CameraPhrase.CameraOption.BACK_CAMERA);
                        break;
                }
                break;
            case VIDEO:
            case AUDIO:
                switch (((RadioGroup)  root_view.findViewById(R.id.recording_radio_group)).getCheckedRadioButtonId()){
                    case R.id.start_recording_btn:
                        phrase = new RecordPhrase(phrase_txt,
                                RecordPhrase.RecordOperation.START_RECORDING, current_phrase_type);
                        break;
                    case R.id.stop_recording_btn:
                        phrase = new RecordPhrase(phrase_txt,
                                RecordPhrase.RecordOperation.STOP_RECORDING, current_phrase_type);
                        break;
                    case R.id.both_recording_btn:
                        phrase = new RecordPhrase(phrase_txt,
                                RecordPhrase.RecordOperation.BOTH_RECORDING, current_phrase_type);
                }
                break;
            case PHONE:
                phrase = new PhonePhrase(phrase_txt,
                        ((EditText)  root_view.findViewById(R.id.phone_field)).getText().toString());
                break;
        }

        try {
            if(phrase != null)
                phrase.setupPhrase(new Assets(getActivity().getApplicationContext()));
        } catch (IOException e) {e.printStackTrace();}

        /* Let everyone, who needs to add the phrase, add it appropriately */
        phrase_dialog_listener.onAddPhrase(phrase);
        dismiss();
    }

    /**
     * Returns true if the given phrase is valid by checking for the following:
     * <br/><br/>
     * - 4 or more words entered <br/>
     * - All words are inside the app's dictionary
     *
     * @param phrase the phrase to check if valid
     * @return true if the given phrase is valid
     */
    private Boolean checkPhrase(String phrase){
        String[] phrase_list = phrase.split("[[ ]*|[,]*|[\\.]*|[:]*|[/]*|[!]*|[?]*|[+]*]+");

        Log.i("COS", "SIZE OF ENGLISH: " + english_dict.size());

        if(phrase_list.length < 4){
            displayError("Less than 4 words entered. Please enter 4 or more words.");
            return false;
        }

        Searcher searcher = new BinarySearch();

        for (String word : phrase_list) {
            if (!searcher.search(english_dict, word)) {
                displayError(word + " is not in the app's dictionary. Please use another word.");
                return false;
            }
        }

        return true;
    }

    /**
     * Make any extra options views visible to the user.
     */
    private void loadExtraOptionsViews(){
        switch (current_phrase_type){
            case CAMERA:
                root_view.findViewById(R.id.camera_radio_group).setVisibility(View.VISIBLE);
                break;
            case VIDEO:
                root_view.findViewById(R.id.recording_radio_group).setVisibility(View.VISIBLE);
                break;
            case AUDIO:
                root_view.findViewById(R.id.recording_radio_group).setVisibility(View.VISIBLE);
                break;
            case PHONE:
                root_view.findViewById(R.id.phone_group).setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * Displays an error message in the error_lbl given the msg* to display.
     * <br/><br/>
     * <i>*Automatically prepends "Error: " to the given message.</i>
     *
     * @param msg the message to display in the error
     */
    private void displayError(String msg){
        error_lbl.setText("Error: " + msg);
    }

    /**
     * Anyone listening to this dialog will add the selected phrase when this dialog is finished.
     */
    public interface PhraseDialogListener{
        void onAddPhrase(Phrase phrase);
    }

    @Override
    protected boolean isRenderScriptEnable() {
        return true;
    }

    @Override
    protected float getDownScaleFactor() {
        return (float) 3.0;
    }
}
