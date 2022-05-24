package com.example.myapp01;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    String result = "";
    String url = "https://prod-storyly-media.s3.eu-west-1.amazonaws.com/test-scenarios/sample_3.json";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e("TAG", "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new jsonTask().execute();
    }

    class jsonTask extends AsyncTask<Void, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL myurl = new URL(url);
                HttpURLConnection urlConnection =  (HttpURLConnection) myurl.openConnection();
                InputStreamReader streamReader = new InputStreamReader(urlConnection.getInputStream());
                BufferedReader reader =  new BufferedReader(streamReader);
                StringBuilder builder = new StringBuilder();
                String line;

                while((line = reader.readLine()) != null){
                    builder.append(line);

                }
                result = builder.toString();
                //Log.e("Json", builder.toString());
            } catch (MalformedURLException e) {
                Log.e("ERROR_IN_URL", e.toString());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("ERROR_IN_CONNECTION", e.toString());
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                int cnt_parent;
                cnt_parent = Integer.parseInt(jsonObject.get("count").toString());
                if(jsonObject.has("items")){
                    calculatePrice(jsonObject, 0);
                    int total_price = 0;
                    JSONArray jsonArray = jsonObject.getJSONArray("items");
                    for(int i = 0; i < jsonArray.length(); i++){
                        total_price += calculatePrice(jsonArray.getJSONObject(i), 0 );
                    }
                    //Log.e("jsonobject", jsonObject.get("name").toString());
                    Log.e("Total price: ",  (cnt_parent * total_price) + "");
                }
            } catch (JSONException e) {
                Log.e("ERROR_FINDING_OBJ", e.toString());
                e.printStackTrace();
            }
        }

        private int calculatePrice(JSONObject jsonObject, int total_price_func) {

            if(jsonObject.has("items")){
                int cnt_child_owner = -1;
                total_price_func = 0;
                String name = "";
                JSONArray array = null;
                try {
                    array = jsonObject.getJSONArray("items");
                    cnt_child_owner = Integer.parseInt(jsonObject.get("count").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for(int i = 0; i < array.length(); i++){
                    try {
                        total_price_func += calculatePrice(array.getJSONObject(i), total_price_func);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return (total_price_func * cnt_child_owner);
            }
            else{
                String name = "";
                int count;
                int price;
                try {
                    count = Integer.parseInt(jsonObject.get("count").toString());
                    price = Integer.parseInt(jsonObject.get("price").toString());
                    return (count * price);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return 0;
        }
    }
}