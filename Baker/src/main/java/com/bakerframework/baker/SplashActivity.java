package com.bakerframework.baker;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_activity);

		/*
		 * Showing splashscreen while making network calls to download necessary
		 * data before launching the app Will use AsyncTask to make http call
		 */
		new PrefetchData().execute();

	}

	/*
	 * Async Task to make http call
	 */
	private class PrefetchData extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// before making http calls
			Log.e("JSON", "Pre execute");

		}

		@Override
		protected Void doInBackground(Void... arg0) {

            // Fetch data from url
            URL url = null;
            HttpURLConnection connection = null;
            try {
                // Create connection
                url = new URL("http://www.magloft.com/manifest/com.magloft.magazine/comgooglemstrebitzergmailcom");
                connection = (HttpURLConnection) url.openConnection();
                connection.setUseCaches(true);

                // Prepare streams
                long totalBytes = connection.getContentLength();
                InputStream inputStream = connection.getInputStream();

                // Prepare iterators
                byte[] buffer = new byte[4096];
                int bytesRead = - 1;

                // Download stream
                while ( (bytesRead = inputStream.read(buffer)) != -1) {
                    Log.i(this.getClass().getName(), "Bytes read: " + bytesRead + "%");
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// After completing http call
			// will close this activity and lauch main activity
			Intent i = new Intent(SplashActivity.this, ShelfActivity.class);
			i.putExtra("foo", "bar");
			startActivity(i);

			// close this activity
			finish();
		}

	}

}
