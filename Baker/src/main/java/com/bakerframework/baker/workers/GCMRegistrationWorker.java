package com.bakerframework.baker.workers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.bakerframework.baker.BakerApp;
import com.bakerframework.baker.client.TaskMandator;
import com.bakerframework.baker.R;
import com.bakerframework.baker.settings.Configuration;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Holland on 10-23-13.
 * Refactored by Tobias Strebitzer <tobias.strebitzer@magloft.com> on 10-23-13.
 * http://www.magloft.com
 */
public class GCMRegistrationWorker extends AsyncTask<Void, Long, String[]> {

    private GoogleCloudMessaging gcm;
    private Context context;
    private TaskMandator mandator;
    private int taskId;

    public GCMRegistrationWorker(Context context, int taskId, TaskMandator mandator) {
        this.gcm = GoogleCloudMessaging.getInstance(context);
        this.context = context;
        this.taskId = taskId;
        this.mandator = mandator;
    }

    @Override
    protected String[] doInBackground(Void... params) {
        String msg = "ERROR";
        String registrationId = "";

        try {

            // Retrieve registration id
            registrationId = gcm.register(context.getString(R.string.sender_id));

            // Send registration id to backend
            if (sendRegistrationIdToBackend(registrationId)) {
                msg = "SUCCESS";
            }

            // Update preferences with new value
            BakerApp.setPreferenceString(Configuration.PREF_REGISTRATION_ID, registrationId);
            BakerApp.setPreferenceInt(Configuration.PREF_APP_VERSION, BakerApp.getVersion());

        } catch (IOException e) {
            msg = "ERROR";
            Log.e(this.getClass().getName(), "registration error", e);
        }

        // Return result
        return new String[]{msg, registrationId};
    }

    @Override
    protected void onPostExecute(String[] results) {
        if(mandator != null) {
            mandator.postExecute(this.taskId, results);
        }
    }

    private boolean sendRegistrationIdToBackend(String registrationId) {
        boolean result = false;
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(context.getString(R.string.post_apns_token_url));

            ArrayList<NameValuePair> postParameters = new ArrayList<>();

            postParameters.add(new BasicNameValuePair("app_id", context.getString(R.string.app_id)));
            postParameters.add(new BasicNameValuePair("user_id", Configuration.getUserId()));
            postParameters.add(new BasicNameValuePair("apns_token", registrationId));
            postParameters.add(new BasicNameValuePair("device", "ANDROID"));

            httpPost.setEntity(new UrlEncodedFormEntity(postParameters));

            HttpResponse response = httpClient.execute(httpPost);

            if (null != response) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    result = true;
                } else {
                    Log.e(this.getClass().toString(), "Device Registration ID failed, response from server was " + response.getStatusLine());
                }
            } else {
                Log.e(this.getClass().toString(), "Device Registration ID failed, response is null");
            }

        } catch (Exception ex) {
            Log.e(this.getClass().toString(), "Fatal error when trying to send the registration ID: " + ex.toString());
        }

        return result;
    }
}