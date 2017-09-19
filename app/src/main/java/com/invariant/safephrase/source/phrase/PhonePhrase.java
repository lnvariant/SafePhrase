package com.invariant.safephrase.source.phrase;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;

import com.invariant.safephrase.GUI.home.Home;

public class PhonePhrase extends Phrase {


    /* ************************************************************************* *
     *                                                                           *
     * Instance Variables                                                        *
     *                                                                           *
     * ************************************************************************  */

    /** The number to call when this phrase is aid */
    private String phone_number;


    /* ************************************************************************* *
     *                                                                           *
     * Constructors                                                              *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Initialize this PhonePhrase with the given phone number
     *
     * @param phrase_txt the text this phrase represents
     * @param phone_number the phone number to call when this phrase is said
     */
    public PhonePhrase(String phrase_txt, String phone_number) {
        super(phrase_txt);
        this.phone_number = phone_number;
        this.type = PhraseAction.PHONE;
        this.optional_txt = phone_number;
    }


    /* ************************************************************************* *
     *                                                                           *
     * Instance Methods                                                          *
     *                                                                           *
     * ************************************************************************  */

    @Override
    public void triggerAction(Context context, PhraseService home_model) {
        super.triggerAction(context, home_model);

        Intent i = new Intent(Intent.ACTION_CALL);
        i.setData(Uri.parse("tel:" + phone_number));

        /* Make sure we have permissions to make a call */
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(context, Home.class);
            intent.putExtra("PERMISSION", "CALL_PHONE");
            context.startActivity(intent);
            return;
        }

        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }


    /* ************************************************************************* *
     *                                                                           *
     * Getters & Setters                                                         *
     *                                                                           *
     * ************************************************************************  */

    public String getPhoneNumber() {
        return phone_number;
    }
}
