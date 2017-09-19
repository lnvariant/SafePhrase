package com.invariant.safephrase.source.phrase;


import android.content.Context;
import android.content.Intent;

import com.invariant.safephrase.source.operations.CapVideo;

public class RecordPhrase extends Phrase {


    /* ************************************************************************* *
     *                                                                           *
     * Instance Variables                                                        *
     *                                                                           *
     * ************************************************************************  */

    /**
     * The operation this record phrase will perform.
     */
    private RecordOperation record_operation;

    /**
     * The current status of the record recorder
     */
    private boolean record_status;


    /* ************************************************************************* *
     *                                                                           *
     * Constructors                                                              *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Initializes a RecordPhrase with the given record operation it will perform.
     *
     * @param phrase_txt       the text this phrase represents
     * @param record_operation the type of record operation this phrase will perform when said
     */
    public RecordPhrase(String phrase_txt, RecordOperation record_operation, PhraseAction type) {
        super(phrase_txt);
        this.record_operation = record_operation;
        record_status = false;
        this.type = type;
        this.optional_txt = record_operation.toString();
    }


    /* ************************************************************************* *
     *                                                                           *
     * Instance Methods                                                          *
     *                                                                           *
     * ************************************************************************  */

    @Override
    public void triggerAction(Context context, PhraseService home_model) {
        super.triggerAction(context, home_model);

        if (type == PhraseAction.VIDEO) {
            Intent i = new Intent(context, CapVideo.class);

            if (record_operation == RecordOperation.START_RECORDING) {
                i.putExtra("video_status", true);
            } else if (record_operation == RecordOperation.STOP_RECORDING) {
                i.putExtra("video_status", false);
            } else if (record_operation == RecordOperation.BOTH_RECORDING) {

                /* If the record status is true, then stop recording. Otherwise, start recording.*/
                if (record_status) {
                    i.putExtra("video_status", false);
                    record_status = false;
                } else {
                    i.putExtra("video_status", true);
                    record_status = true;
                }
            }

            home_model.recognizer.stopRecording();
            context.startService(i);

        } else {
            if (record_operation == RecordOperation.START_RECORDING) {
                home_model.isRecording = true;
            } else if (record_operation == RecordOperation.STOP_RECORDING) {
                home_model.isRecording = false;
            } else if (record_operation == RecordOperation.BOTH_RECORDING) {
                /* If the record status is true, then stop recording. Otherwise, start recording.*/
                if (record_status) {
                    home_model.isRecording = false;
                    record_status = false;
                } else {
                    home_model.isRecording = true;
                    record_status = true;
                    home_model.recognizer.stop();
                    home_model.setupRecognizer();
                }
            }

            home_model.recognizer.stopRecording();
        }
    }


    /* ************************************************************************* *
     *                                                                           *
     * Getters & Setters                                                         *
     *                                                                           *
     * ************************************************************************  */

    public RecordOperation getRecordOperation() {
        return record_operation;
    }


    /* ************************************************************************* *
     *                                                                           *
     * Enums                                                                     *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Represents the type of record operations available.
     */
    public enum RecordOperation {
        START_RECORDING, STOP_RECORDING, BOTH_RECORDING
    }
}
