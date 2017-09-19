package com.invariant.safephrase.source.phrase;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.invariant.safephrase.R;

import java.io.IOException;
import java.util.List;

import edu.cmu.pocketsphinx.Assets;

/**
 * The adapter for the list component that displays phrases.
 */
public class PhraseAdapter extends RecyclerView.Adapter<PhraseAdapter.PhraseViewHolder> {


    /* ************************************************************************* *
     *                                                                           *
     * Instance Variables                                                        *
     *                                                                           *
     * ************************************************************************  */

    /** The list of phrases to display */
    private List<Phrase> phrase_list;

    /** The current context of this adapter */
    private Context context;


    /* ************************************************************************* *
     *                                                                           *
     * Constructors                                                              *
     *                                                                           *
     * ************************************************************************  */

    public PhraseAdapter(Context context, List<Phrase> phrase_list) {
        this.phrase_list = phrase_list;
        this.context = context;
    }


    /* ************************************************************************* *
     *                                                                           *
     * Instance Methods                                                          *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Called when displaying a row. Get the relevent information and set the views of the row to
     * display it.
     *
     * @param holder holds all the views of the row which will display the information
     * @param position the position of the row that is going to be displayed
     */
    @Override
    public void onBindViewHolder(PhraseViewHolder holder, int position) {

        /* Get the phrase object at the position in the list */
        Phrase phrase = phrase_list.get(position);

        holder.phrase_lbl.setText(phrase.getPhraseText());

        /* Based on the type of phrase, display the appropriate subtext */
        switch (phrase.getType()){
            case CAMERA:
                holder.option_lbl.setText("OPTION: " + phrase.getOptionalText().replace("_", " "));
                break;
            case VIDEO:
            case AUDIO:
                holder.option_lbl.setText("OPERATION: " + phrase.getOptionalText().replace("_", " "));
                break;
            case PHONE:
                holder.option_lbl.setText("PHONE: " + phrase.getOptionalText());
                break;
        }
    }

    /**
     * Returns the number of items in the list.
     *
     * @return number of items in the list
     */
    @Override
    public int getItemCount() {
        return phrase_list.size();
    }

    /**
     * Remove the row at given the position inside the list view.
     *
     * @param position the position at which the row to be removed is at
     */
    public void removeAt(int position) {
        try {
            phrase_list.get(position).deletePhrase(new Assets(context));
        } catch (IOException e) { e.printStackTrace();}


        /* Restart the speech recongizer so it knows the phrase has been deleted */
        if(PhraseService.isRunning) {
            Intent i = new Intent(PhraseService.PHRASE_SERVICE_BROADCAST_ID);
            i.putExtra("command", "RESTART RECOGNIZER");
            LocalBroadcastManager.getInstance(context).sendBroadcast(i);
        }

        phrase_list.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, phrase_list.size());
    }


    /* ************************************************************************* *
     *                                                                           *
     * ViewHolder Methods & Classes                                              *
     *                                                                           *
     * ************************************************************************  */

    @Override
    public PhraseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PhraseViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.phrase_row, parent, false));
    }

    /**
     * The ViewHolder which holds all the views that each row component will have. It also defines
     * any click methods of the views inside each row component.
     */
    public class PhraseViewHolder extends RecyclerView.ViewHolder{
        private TextView phrase_lbl, option_lbl;
        private ImageView delete_btn;

        public PhraseViewHolder(final View itemView) {
            super(itemView);
            phrase_lbl = (TextView) itemView.findViewById(R.id.phrase_lbl);
            option_lbl = (TextView) itemView.findViewById(R.id.option_lbl);
            delete_btn = (ImageView) itemView.findViewById(R.id.delete_btn);

            delete_btn.setOnClickListener(v -> {
                AlertDialog.Builder alert = new AlertDialog.Builder(itemView.getContext());
                alert.setMessage("Are you sure you want to delete this phrase?");
                alert.setPositiveButton("YES", (dialog, which) -> removeAt(getAdapterPosition()));
                alert.setNegativeButton("NO", null);
                alert.show();
            });
        }
    }


    /* ************************************************************************* *
     *                                                                           *
     * Getters & Setters                                                         *
     *                                                                           *
     * ************************************************************************  */

    public List<Phrase> getPhraseList() {
        return phrase_list;
    }

    public void setPhraseList(List<Phrase> phrase_list) {
        this.phrase_list = phrase_list;
    }
}
