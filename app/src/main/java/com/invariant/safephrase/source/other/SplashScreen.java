package com.invariant.safephrase.source.other;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.invariant.safephrase.GUI.home.Home;
import com.invariant.safephrase.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import edu.cmu.pocketsphinx.Assets;

public class SplashScreen extends AppCompatActivity {

    private ArrayList<String> english_dict;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        LoadDictionaryTask ldt = new LoadDictionaryTask();
        ldt.execute();
    }

    private class LoadDictionaryTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Assets assets = new Assets(getApplicationContext());
                File assetsDir = assets.syncAssets();
                File dict = new File(assetsDir, "cmudict-en-us.dict");
                english_dict = new ArrayList<>();

                BufferedReader reader = new BufferedReader(new FileReader(dict));
                String current_line;

                while ((current_line = reader.readLine()) != null) {
                    english_dict.add(current_line);
                }
            }
            catch (Exception e){}
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Intent i = new Intent(getApplicationContext(), Home.class);
            Home.english_dict = english_dict;
            startActivity(i);
        }
    }
}
