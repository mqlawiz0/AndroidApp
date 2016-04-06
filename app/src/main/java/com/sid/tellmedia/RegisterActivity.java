package com.sid.tellmedia;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;




/**
 * Final Step:
 *
 *
 */

@SuppressWarnings("deprecation")
public class RegisterActivity extends Activity {

    private final String serverUrl = "http://tellmedia.herokuapp.com/api/signup";
    protected EditText username;
    LinearLayout lm;
    private EditText password;
    private EditText firstname;
    private EditText lastname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        lm = (LinearLayout) findViewById(R.id.registerActivity);

        username = (EditText)findViewById(R.id.username_field);
        password = (EditText)findViewById(R.id.password_field);
        firstname = (EditText)findViewById(R.id.firstname_field);
        lastname = (EditText)findViewById(R.id.lastname_field);

        Button signUpButton = (Button)findViewById(R.id.sign_up);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredUsername = username.getText().toString();
                String enteredPassword = password.getText().toString();
                String enteredFirstname = firstname.getText().toString();
                String enteredLastname = lastname.getText().toString();

                if(enteredUsername.equals("") || enteredPassword.equals("") || enteredLastname.equals("") || enteredFirstname.equals("")){
                    Toast.makeText(RegisterActivity.this, "All the fields must be filled", Toast.LENGTH_LONG).show();
                    return;
                }
                if(enteredUsername.length() <= 5 || enteredPassword.length() <= 5){
                    Toast.makeText(RegisterActivity.this, "Username or password length must be greater than 6", Toast.LENGTH_LONG).show();
                    return;
                }
                // request authentication with remote server
                AsyncDataClass asyncRequestObject = new AsyncDataClass();
                asyncRequestObject.execute(serverUrl, enteredUsername, enteredFirstname, enteredLastname, enteredPassword);
            }
        });
    }

    private class AsyncDataClass extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
            HttpConnectionParams.setSoTimeout(httpParameters, 5000);

            @SuppressWarnings("resource")
            HttpClient httpClient = new DefaultHttpClient(httpParameters);
            HttpPost httpPost = new HttpPost(params[0]);

            String jsonResult = "";
            try {
                JSONObject jsonobj_signUp = new JSONObject();
                JSONObject jsonobj_name = new JSONObject();
                //creating the NAME JsonObj
                jsonobj_name.accumulate("first",params[2]);
                jsonobj_name.accumulate("last",params[3]);
                //accumulating the different JsonObjs in the Sign_up Jsonobj
                jsonobj_signUp.accumulate("username", params[1]);
                jsonobj_signUp.accumulate("name", jsonobj_name);
                jsonobj_signUp.accumulate("password", params[4]);
                StringEntity se = new StringEntity(jsonobj_signUp.toString());

                Log.d("TAG", "The jsonObj : " + jsonobj_signUp.toString());
                httpPost.setEntity(se);

                String credentials = params[2]+ ":" + params[4]; //"user"+":"+"password";
                Log.d("TAG", "Identification by : " + credentials);

                //Encode the logins
                String base64Encoded = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                //setting the headers
                httpPost.addHeader("Authorization","Basic "+ base64Encoded);
                //Json content
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
                //executing the request
                HttpResponse response = httpClient.execute(httpPost);
                //recover the result
                jsonResult = inputStreamToString(response.getEntity().getContent()).toString();

                Log.d("TAG","the result of the request : "+ jsonResult);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return jsonResult;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            System.out.println("Resulted Value: " + result);
            if(result.equals("")){
                Toast.makeText(RegisterActivity.this, "Server connection failed", Toast.LENGTH_LONG).show();
                return;
            }

            if(result == null){
                Toast.makeText(RegisterActivity.this, "Invalid username or password or email", Toast.LENGTH_LONG).show();
                return;
            }else{
                //Identification done
                //Create a new button and a CALL-ACTIVITY
                try {
                    JSONObject jsonRootObject = new JSONObject(result);
                    final String phoneNumber = jsonRootObject.getString("phone");

                    //BUTTON __
                    Button phoneCall = new Button(RegisterActivity.this);
                    phoneCall.setText("Call");
                    // phoneCall.setGravity(Gravity.CENTER_HORIZONTAL);

                    LinearLayout.LayoutParams params =new LinearLayout.LayoutParams(
                            ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.CENTER_HORIZONTAL;
                    phoneCall.setLayoutParams(params);

                    lm.addView(phoneCall);

                    phoneCall.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            //on decale vers la deuxieme activité
                            Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                            phoneIntent.setData(Uri.parse("tel:" + phoneNumber));
                            startActivity(phoneIntent);

                        }
                    });



                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }

        private StringBuilder inputStreamToString(InputStream is) {
            String rLine;
            StringBuilder answer = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            try {
                while ((rLine = br.readLine()) != null) {
                    answer.append(rLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return answer;
        }
    }


}