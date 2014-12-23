package com.bakerframework.baker.task;

import android.os.AsyncTask;
import android.util.Log;

import com.bakerframework.baker.client.TaskMandator;
import com.bakerframework.baker.settings.Configuration;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ApiRequestTask extends AsyncTask<String, Long, String> {

    private String url;
    private HashMap<String, String> parameters;

    public ApiRequestTask(String url) {
        this.url = url;
        this.parameters = parameters;
    }

    @Override
    protected String doInBackground(String... params) {
        String apiUrl = Configuration.getPurchaseConfirmationUrl();

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        try {
            // Build parameters
            List<NameValuePair> nameValuePairs = new ArrayList<>(params.length / 2);
            for (int i = 0; i < params.length; i+=2) {
                nameValuePairs.add(new BasicNameValuePair(params[i], params[i+1]));
            }
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

            Log.i(getClass().getName(), "Response: " + response);

        } catch (ClientProtocolException e) {
            Log.e(getClass().getName(), "Error: " + e);
        } catch (IOException e) {
            Log.e(getClass().getName(), "Error: " + e);
        }

        return "SUCCESS";
    }

    @Override
    protected void onProgressUpdate(Long... progress) {

    }

    @Override
    protected void onPostExecute(final String result) {
        Log.i("BackendApiRequestTask", "Result: " + result);
    }

}
