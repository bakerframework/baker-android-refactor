/**
 * Copyright (c) 2013-2014. Francisco Contreras, Holland Salazar.
 * Copyright (c) 2015. Tobias Strebitzer, Francisco Contreras, Holland Salazar.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 * Neither the name of the Baker Framework nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/
package com.bakerframework.baker.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.bakerframework.baker.client.TaskMandator;
import com.bakerframework.baker.settings.Configuration;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class UnzipperTask extends AsyncTask<String, Long, String> {

    private Context context;

    private TaskMandator mandator;

    private int taskId;

    private boolean resumed;

    public void setResumed(final boolean _resumed) {
        this.resumed = _resumed;
    }

    public UnzipperTask(Context context, TaskMandator mandator, final int taskId) {
        this.context = context;
        this.mandator = mandator;
        this.taskId = taskId;
    }

    @Override
    protected String doInBackground(String... params) {
        String result;
        String workingDir = "";
        try {
            Log.d(this.getClass().getName(), "Started unzip process for file " + params[0]);

            // First we create a directory to hold the unzipped files.
            workingDir = params[0].substring(0, params[0].lastIndexOf("/")) + File.separator;
            File containerDir = new File(workingDir + params[1]);

            Log.d(this.getClass().getName(), "Issue Directory: " + containerDir);

            if (containerDir.exists()) {
                Configuration.deleteDirectory(containerDir.getPath());
            }

            if (containerDir.mkdirs()) {
                workingDir = workingDir + params[1] + File.separator;
                this.extract(params[0], workingDir);
                result = "SUCCESS";
            } else {
                Log.e(this.getClass().getName(), "Could not create the package directory");
                //TODO: Notify the user
                result = "ERROR";
            }
        } catch (IOException ex) {
            Log.e(this.getClass().getName(), "Error unzipping the issue.", ex);
            result = "ERROR";
        }

        if (result.equals("SUCCESS")) {
            Log.d(this.getClass().getName(), "Unzip process finished successfully.");
        } else {
            Log.d(this.getClass().getName(), "There was a problem extracting the issue.");
            if (!workingDir.isEmpty()) {
                Configuration.deleteDirectory(workingDir);
            }
        }

        return result;
    }

    private void extract(final String inputFile, final String outputDir) throws IOException  {
        FileInputStream fileInputStream = null;
        ZipArchiveInputStream zipArchiveInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {

            Log.d(this.getClass().getName(), "Will extract " + inputFile + " to " + outputDir);

            byte[] buffer = new byte[8192];
            fileInputStream = new FileInputStream(inputFile);

            // We use null as encoding.
            zipArchiveInputStream = new ZipArchiveInputStream(fileInputStream, null, true);
            ArchiveEntry entry;
            while ((entry = zipArchiveInputStream.getNextEntry()) != null) {
                Log.d(this.getClass().getName(), "Extracting entry " + entry.getName());
                File file = new File(outputDir, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                    fileOutputStream = new FileOutputStream(file);
                    int bytesRead;
                    while ((bytesRead = zipArchiveInputStream.read(buffer, 0, buffer.length)) != -1)
                        fileOutputStream.write(buffer, 0, bytesRead);
                    fileOutputStream.close();
                    fileOutputStream = null;
                }
            }
            // Delete the zip file
            File zipFile = new File(inputFile);
            zipFile.delete();
        }catch (Exception e) {
            Log.e("UnzipperTask", "Error unzipping file: " + inputFile + ", " + e);
        } finally {
            try {
                zipArchiveInputStream.close();
                fileInputStream.close();
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (NullPointerException ex) {
                Log.e(this.getClass().getName(), "Error closing the file streams.", ex);
            } catch (IOException ex) {
                Log.e(this.getClass().getName(), "Error closing the file streams.", ex);
            }
        }
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
    }

    @Override
    protected void onPostExecute(final String result) {
        mandator.postExecute(taskId, result);
    }

}
