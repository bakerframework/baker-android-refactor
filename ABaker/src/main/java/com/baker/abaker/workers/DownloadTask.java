package com.baker.abaker.workers;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import com.baker.abaker.client.GindMandator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class DownloadTask extends AsyncTask<String, Long, String> {

    // Task management
    private GindMandator mandator;
    private int taskId;

    // Input arguments
    private String downloadUrl;
    private String fileName;
    private String fileTitle;
    private String fileDescription;
    private String downloadDirectoryPath;
    private int visibility;

    // Download handling
    Uri downloadedFile;
    private long downloadId = -1L;
    private boolean overwrite = true;
    private Context context;
    private String android_id;
    private HttpURLConnection connection;

    // Helper variables
    private String tag;

    public DownloadTask(Context context, GindMandator mandator, final int taskId, final String downloadUrl, final String fileName, final String fileTitle, final String fileDescription, final String downloadDirectoryPath, final int visibility) {

        // Task management
        this.context = context;
        this.mandator = mandator;
        this.taskId = taskId;

        // Replace URL
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.fileTitle = fileTitle;
        this.fileDescription = fileDescription;
        this.downloadDirectoryPath = downloadDirectoryPath;
        this.visibility = visibility;

        // Helper variables
        this.tag = this.getClass().toString();

    }

    // Public methods

    public boolean isDownloading() {
        return false;
    }

    public void cancelDownload() {
        // Todo: cancel download
    }

    // Main routine

    @Override
    protected String doInBackground(String... params) {
        Log.d(tag, "DOWNLOADING FILE: " + downloadUrl);

        try {

            // Prepare download
            File file = this.createTargetFile();

            // Download file
            downloadedFile = this.downloadFile(file);

            Log.w(tag, "DOWNLOAD COMPLETED");

        } catch (Exception e) {
            Log.w(tag, "Error while retrieving file from " + downloadUrl, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return "SUCCESS";
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        mandator.updateProgress(taskId, progress[0], progress[1], progress[2]);
    }

    @Override
    protected void onPostExecute(String result) {
        String path = (null == downloadedFile) ? "" : downloadedFile.getPath();
        mandator.postExecute(taskId, result, path);
    }

    // Helper methods

    private String getFilePath() {
        return downloadDirectoryPath.concat(File.separator).concat(fileName);
    }

    private File createTargetFile() throws IOException {
        File file = new File(this.getFilePath());
        if(!file.exists()) { file.createNewFile(); }
        return file;
    }

    private Uri downloadFile(File file) throws IOException {

        // Prepare download
        connection = null;
        URL url = new URL(downloadUrl);
        connection = (HttpURLConnection) url.openConnection();
        connection.setUseCaches(true);

        // Prepare streams
        long totalBytes = connection.getContentLength();
        InputStream inputStream = connection.getInputStream();
        OutputStream output = new FileOutputStream( file );

        // Prepare iterators
        long bytesSoFar = 0;
        long progress = 0;
        byte[] buffer = new byte[4096];
        int bytesRead = - 1;

        // Download stream
        while ( (bytesRead = inputStream.read(buffer)) != -1) {
            if (bytesRead > 0) {
                // Update progress
                if (totalBytes != -1) {
                    bytesSoFar = bytesSoFar + bytesRead;
                    progress = (long)((float)bytesSoFar/totalBytes*100);
                    publishProgress(progress, bytesSoFar, totalBytes);
                    Log.i(tag, "Percentage complete: " + progress);
                }
                // Write to file
                output.write(buffer, 0, bytesRead);
            }
        }
        output.close();

        // Return file uri
        return Uri.parse("file://".concat(this.getFilePath()));
    }

}
