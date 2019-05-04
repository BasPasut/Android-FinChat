package com.example.finchat;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadFileFromURL extends AsyncTask<String, String, String> {
    String pathFolder = "";
    String pathFile = "";
    Context context;

    DownloadFileFromURL(Context context){
        this.context = context;
    }

    @Override
    protected String doInBackground(String... f_url) {
        int count;

        try {
            pathFolder = Environment.getExternalStorageDirectory() + "/Finchat" + f_url[1];
            pathFile = pathFolder + f_url[2];
            File finchatFolder = new File(pathFolder);
            if(!finchatFolder.exists()){
                finchatFolder.mkdirs();
            }

            URL url = new URL(f_url[0]);
            URLConnection connection = url.openConnection();
            connection.connect();

            // this will be useful so that you can show a tipical 0-100%
            // progress bar
            int lengthOfFile = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            FileOutputStream output = new FileOutputStream(pathFile);

            byte data[] = new byte[1024]; //anybody know what 1024 means ?
            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress("" + (int) ((total * 100) / lengthOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();


        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return pathFile;
    }


    @Override
    protected void onPostExecute(String file_url) {
        Toast.makeText(context,"Download success!!", Toast.LENGTH_LONG).show();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

}