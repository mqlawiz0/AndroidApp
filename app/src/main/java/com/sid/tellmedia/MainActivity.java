package com.sid.tellmedia;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * First Step:
 *
 */
public class MainActivity extends Activity {

    String user,password,urlImg;
    String nextStep;
    String myUrl = "http://tellmedia.herokuapp.com/api/user";
    LinearLayout lm ;

    /**
     * Sets an HtppURLConnecion to the url given, gets the response and return the String content of it.
     * @param myurl
     * @return
     * @throws IOException
     */
    private static String downloadUrl(String myurl) throws IOException {
        StringBuilder contentAsString = new StringBuilder();
        InputStream is = null;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("Debug_TAG", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                contentAsString.append(line);
            }
            return contentAsString.toString();

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //---------Recuperation de notre Layout----------//
        lm = (LinearLayout) findViewById(R.id.MainActivity);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Log.d(msg, "The onStart() event");
        if (isOnline()) {

            new DownloadWebpageTask().execute(myUrl);
        } else {
            Toast.makeText(this, "Internet Down :)", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        lm.removeAllViews();
    }

    /**
     * Allows us to test if the phone is connected to a network
     * @return True if he is connected, False else.
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /*
     *
     */
    protected void JsonDisplay(String result)
    {
        JSONObject jsonRootObject = null;
        try {
            jsonRootObject = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert jsonRootObject != null;
        String msg = jsonRootObject.optString("message");

        //affichage du contenu de l'attribut "message
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);

        // Create LinearLayout
        LinearLayout ll = new LinearLayout(this);
        //params.gravity = Gravity.CENTER_HORIZONTAL;
        ll.setLayoutParams(params);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER_VERTICAL);

        // Create TextView

        TextView myLabel = new TextView(this);
        myLabel.setText("Message :");
        ll.addView(myLabel);

        // Create TextView
        TextView message = new TextView(this);
        message.setText(msg);

        ll.addView(message);
        lm.addView(ll);
        Log.i("TAG", "index :" + "VIEW ADDED.");


        //Recuperation de l'attribut "image"
        urlImg = jsonRootObject.optString("image");

        //Recuperation des "attributs" user et "password"
        user = jsonRootObject.optString("username");
        password = jsonRootObject.optString("password");
        Log.i("TAG", "user + password :" + user +" - "+ password);

        //Recuperation de nextStep
        nextStep = jsonRootObject.optString("nextStep");
        Log.i("TAG", "nextStep :" + nextStep);

    }

    /**
     * DownloadWebpageTask allows us to download the content of the first URL
     */
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Display the informations we need
            JsonDisplay(result);

            ImageView imp = new ImageView(MainActivity.this);
            new DownloadImageTask(imp).execute(urlImg);

            Button suivant = new Button(MainActivity.this);
            suivant.setGravity(Gravity.CENTER);
            suivant.setText("Suivant");
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            suivant.setLayoutParams(params);

            lm.addView(suivant);
            //-----Button action----//
            suivant.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //on decale vers la deuxieme activité
                    Intent secondactivity = new Intent(MainActivity.this, SecondActivity.class);
                    //on passe le message recuperé par le Intent
                    secondactivity.putExtra("user", user);
                    secondactivity.putExtra("password", password);
                    //on lance la deuxieme activité
                    startActivity(secondactivity);

                }
            });
        }
    }

    /**
     * DownloadImageTask allows us to download the IMAGE from the URL given using a Bitmap.
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result)
        {
            bmImage.setImageBitmap(result);
            lm.addView(bmImage);
        }
    }

}
