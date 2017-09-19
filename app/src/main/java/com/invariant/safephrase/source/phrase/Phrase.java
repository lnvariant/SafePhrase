package com.invariant.safephrase.source.phrase;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;

import com.invariant.safephrase.source.settings.SettingsModel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import edu.cmu.pocketsphinx.Assets;

/**
 * A Phrase is a collection of words a user will say to trigger an action.
 */
public class Phrase {


    /* ************************************************************************* *
     *                                                                           *
     * Instance Variables                                                        *
     *                                                                           *
     * ************************************************************************  */

    /** The string of text the phrase represents */
    protected String phrase_txt;

    /** Information about the phrase's extras options/operations */
    protected String optional_txt;

    /** The type action this phrase represents */
    protected PhraseAction type;


    /* ************************************************************************* *
     *                                                                           *
     * Constructors                                                              *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Intializes a phrase with the given phrase_txt.
     *
     * @param phrase_txt the text this phrase represents
     */
    public Phrase(String phrase_txt) {
        this.phrase_txt = phrase_txt.toUpperCase().trim();
        this.optional_txt = "";
    }


    /* ************************************************************************* *
     *                                                                           *
     * Instance Methods                                                          *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Set up the phrase inside the key_phrases.gram file so it can be picked up by a speech
     * recognizer.
     *
     * @param assets the assets folder that defines where the key_phrases file is located
     */
    public void setupPhrase(Assets assets){
        try {
            File assetsDir = assets.syncAssets();
            File key_phrases = new File(assetsDir, "key_phrases.gram");
            FileWriter file_writer = new FileWriter(key_phrases, true);
            PrintWriter print_writer = new PrintWriter(file_writer);
            print_writer.println(phrase_txt.trim() + " /1e-40/");
            print_writer.close();
            file_writer.close();
        }catch (Exception e){
            Log.i("COS", "Error: Failed to write phrase to file.");
        }
    }

    /**
     * Delete the phrase inside the key_phrases.gram file so it can not be picked up by a speech
     * recognizer.
     *
     * @param assets the assets folder that defines where the key_phrases file is located
     */
    public void deletePhrase(Assets assets){
        try {
            File assetsDir = assets.syncAssets();
            File key_phrases = new File(assetsDir, "key_phrases.gram");

            File tempFile = new File(assetsDir, "myTempFile.gram");

            BufferedReader reader = new BufferedReader(new FileReader(key_phrases));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String currentLine;

            while((currentLine = reader.readLine()) != null) {
                /* Write the line into the file as long as it isn't the one we're trying to delete */
                if(!currentLine.equals(phrase_txt + " /1e-40/")){
                    writer.write(currentLine + System.getProperty("line.separator"));
                }
            }
            writer.close();
            reader.close();
            boolean result = key_phrases.delete();

            if(result) tempFile.renameTo(key_phrases);

        }catch (Exception e){
            Log.i("COS", "Error: Failed to delete phrase from file.");
        }
    }

    /**
     * Executes the action this phrase is tied to. Make a call to super's triggerAction in order to
     * get vibrate and ring functionality.
     *
     * @param context the context of which this phrase is available in
     */
    public void triggerAction(Context context, PhraseService home_model){

        /* Vibrate the phone if setting to vibrate is it is enabled */
        if(SettingsModel.vibrate_on_phrase){
            ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(300);
        }

        /* Ring the phone if setting to ring is it is enabled */
        if(SettingsModel.ring_on_phrase){
            Uri n = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, n);
            r.play();
        }
    }


    /* ************************************************************************* *
     *                                                                           *
     * Getters & Setters                                                         *
     *                                                                           *
     * ************************************************************************  */

    public PhraseAction getType() {
        return type;
    }

    public String getPhraseText(){
        return phrase_txt;
    }

    public String getOptionalText() {
        return optional_txt;
    }

    public void setOptionalText(String optional_txt) {
        this.optional_txt = optional_txt;
    }

    /* ************************************************************************* *
     *                                                                           *
     * Enums                                                                     *
     *                                                                           *
     * ************************************************************************  */

    /**
     * The type of action that will be triggered when this Phrase is said.
     */
    public enum PhraseAction{
        CAMERA, VIDEO, AUDIO, PHONE
    }
}
