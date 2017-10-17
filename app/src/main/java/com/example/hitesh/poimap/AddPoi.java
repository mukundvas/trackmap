package com.example.hitesh.poimap;

import android.content.Context;
import android.location.Location;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class AddPoi extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poi);
        Log.e("A;", " "+ MapsActivity.Mylocation.getLatitude());

        Log.e("A;", " "+ MapsActivity.Mylocation.getLongitude());

        Log.e("Step","1");



        ImageView button = (ImageView) findViewById(R.id.setButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText title = (EditText)findViewById(R.id.editText);

                String POIname = title.getText().toString();


                new SendPostRequest(AddPoi.this,POIname,MapsActivity.Mylocation.getLatitude(),MapsActivity.Mylocation.getLongitude()).execute();

            }
        });

    }
}


class SendPostRequest extends AsyncTask<String, Void, String> {

    Double lat, lang;
    String POIname;
    Context con;

    public SendPostRequest(Context con, String POIname,Double lat, Double lang){

        this.con = con;
        this.POIname=POIname;
        this.lat=lat;
        this.lang=lang;

    }

    protected void onPreExecute() {
    }

    protected String doInBackground(String... arg0) {

        try {

            Log.e("Step","2");

            URL url2 = new URL("http://dev.citrans.net:8888/skymeet/poi/add"); // here is your URL path

            HttpURLConnection httpURLConnection = (HttpURLConnection)url2.openConnection();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST"); // here you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
            httpURLConnection.setRequestProperty("Content-Type", "application/json"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`
            httpURLConnection.connect();
            Log.e("Step","3");


            JSONObject jsonObject = new JSONObject();

            ArrayList<Double> list = new ArrayList<>();
            list.add(lat);
            list.add(lang);



            jsonObject.put("status","ACTIVE");
            jsonObject.put("createdBy","HW8");
            jsonObject.put("title",""+POIname);
          //  jsonObject.put("title","Some Title From User");
            jsonObject.put("location",new JSONArray(list));


            DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
            wr.writeBytes(jsonObject.toString());
            Log.e("json",""+ jsonObject.toString());

            int error = httpURLConnection.getResponseCode();
            Log.e("response code","+" + error);
            Log.e("Step","4");


            wr.flush();
            wr.close();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } /*catch (JSONException e) {
                e.printStackTrace();
            }*/ catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }

    @Override
    protected void onPostExecute(String result) {

       Toast.makeText(con ,"POI added succesfully",Toast.LENGTH_SHORT).show();

    }





}
