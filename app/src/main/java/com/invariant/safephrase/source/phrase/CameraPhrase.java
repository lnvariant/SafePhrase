package com.invariant.safephrase.source.phrase;


import android.content.Context;
import android.content.Intent;

import com.invariant.safephrase.source.operations.CapPhoto;

public class CameraPhrase extends Phrase {


    /* ************************************************************************* *
     *                                                                           *
     * Instance Variables                                                        *
     *                                                                           *
     * ************************************************************************  */

    /** Whether the this phrase triggers the front or back camera */
    private CameraOption camera_option;


    /* ************************************************************************* *
     *                                                                           *
     * Constructors                                                              *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Initializes a CameraPhrase with the given camera option that this phrase is tied to.
     *
     * @param phrase_txt the text this phrase represents
     * @param camera_option whether to use front or back camera when this phrase is said
     */
    public CameraPhrase(String phrase_txt, CameraOption camera_option){
        super(phrase_txt);
        this.camera_option = camera_option;
        this.type = PhraseAction.CAMERA;
        this.optional_txt = camera_option.toString();
    }


    /* ************************************************************************* *
     *                                                                           *
     * Instance Methods                                                          *
     *                                                                           *
     * ************************************************************************  */

    @Override
    public void triggerAction(Context context, PhraseService home_model) {
        super.triggerAction(context, home_model);

        Intent i = new Intent(context, CapPhoto.class);

        if (camera_option.equals(CameraOption.FRONT_CAMERA)){
            i.putExtra("use_back", false);
        }
        else if (camera_option.equals(CameraOption.BACK_CAMERA)){
            i.putExtra("use_back", true);
        }

        context.startService(i);
    }


    /* ************************************************************************* *
     *                                                                           *
     * Getters & Setters                                                         *
     *                                                                           *
     * ************************************************************************  */

    public CameraOption getCameraOption() {
        return camera_option;
    }


    /* ************************************************************************* *
     *                                                                           *
     * Enums                                                                     *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Represents the camera side that should be used.
     */
    public enum CameraOption{
        FRONT_CAMERA, BACK_CAMERA
    }
}
