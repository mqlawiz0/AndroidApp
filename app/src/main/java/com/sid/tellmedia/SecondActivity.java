package com.sid.tellmedia;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;



/**
 * Second step:
 * -1- get the logins in the Extra
 * -2- Send an HTTP [GET] request with basic autenticator to the new URL
 * -3-get the respone from the URL
 * -3.1- Display the "nextSteps" in a List
 * -3.2-
 */
public class SecondActivity extends ListActivity {

    TextView myMessage;
    Button seConnecter;
    String base64Encoded,nextStep;
    ArrayList<String> listItems=new ArrayList<String>();
    ArrayAdapter<String> adapter;
    String myUrl = "http://tellmedia.herokuapp.com/api/list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity);
        //------My TextView --------  //
        myMessage = (TextView) findViewById(R.id.message);
        //--------------My Button------------------//
        seConnecter = (Button) findViewById(R.id.bouton);
        //-------------My List ---------------------------------//
        adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,listItems);
        setListAdapter(adapter);

        String username = (String) getIntent().getSerializableExtra("user");
        String password = (String) getIntent().getSerializableExtra("password");
        //Encode the logins
        String credentials = username+ ":" + password;
        base64Encoded = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
    }


    protected void onStart() {
        super.onStart();
        // Log.d(msg, "The onStart() event");

        new DownloadWebpageTask().execute(myUrl);
        //-----configuration de l'action du bouton seConnecter----//
        seConnecter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent thirdActivity = new Intent(SecondActivity.this, RegisterActivity.class);
                startActivity(thirdActivity);
            }
        });
    }

    private String downloadUrl(String myurl) throws IOException {
        StringBuilder contentAsString = new StringBuilder();
        InputStream is = null;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty ("Authorization","Basic " +base64Encoded);
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
            //String contentAsString = readIt(is, len);
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
            String msg;
            try {
                //"message" Recovery
                JSONObject jsonRootObject = new JSONObject(result);
                msg = jsonRootObject.optString("message").toString();

                //Display it
                myMessage.setText(msg);

                //"steps" Recovery
                JSONArray array = new JSONArray(jsonRootObject.getString("steps"));
                //Display it in our static listView
                for (int i = 0; i < array.length(); i++) {
                    adapter.add(array.getString(i));
                }
                //"nextStep" Recovery
                nextStep = jsonRootObject.optString("nextStep");
                Log.i("TAG","nextStep : "+nextStep);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}