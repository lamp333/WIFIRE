package com.example.yuzur.maps;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class UploadFileService extends JobService {
    private static final String TAG = MapsActivity.class.getSimpleName();
    public String encode(String key, String data) {
        try {

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            String encoded = new String(Hex.encodeHex(sha256_HMAC.doFinal(data.getBytes("UTF-8"))));
            return encoded;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                File image = new File((String) params.getExtras().get("input"));

                Notification.Builder uploadNotif = new Notification.Builder(UploadFileService.this)
                        .setContentTitle("Uploading: " + image.getName())
                        .setSmallIcon(R.drawable.qualitylogo)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setShowWhen(true);

                //notificationStart.flags = Notification.FLAG_AUTO_CANCEL;
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                //notificationManager.notify(0,notificationStart);

                HttpURLConnection connection = null;
                Date date = new Date();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
                String timestamp = sdf.format(date);

                try {
                    final String user = (String) params.getExtras().get("user");
                    final String publicKey = (String) params.getExtras().get("publicKey");
                    final String privateKey = (String) params.getExtras().get("privateKey");

                    String url_String = String.format("https://swat.sdsc.edu:5443/users/%s/images?publicKey=%s",user, publicKey);
                    URL url = new URL(url_String);

                    String data = "POST" + "|" + url_String + '|' + publicKey + '|' + timestamp;

                    String signature = encode(privateKey, data);

                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("X-Imbo-Authenticate-Signature", signature);
                    connection.setRequestProperty("X-Imbo-Authenticate-Timestamp", timestamp);


                    //Write data
                    FileInputStream instream = new FileInputStream((String)params.getExtras().get("input"));
                    float totalBytes = instream.available();
                    //IOUtils.copy(instream, connection.getOutputStream());

                    BufferedInputStream bufInput = new BufferedInputStream( instream);
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());

                    int progress = 0;
                    int bytesRead = 0;
                    int percentUploaded = 0;
                    byte buf[] = new byte[1024];
                    while ((bytesRead = bufInput.read(buf)) != -1) {
                        // write output
                        out.write(buf, 0, bytesRead);
                        out.flush();
                        progress += bytesRead;
                        // update progress bar
                        int newPercent = (int)((progress * 100) / totalBytes);
                        if( newPercent != percentUploaded){
                            uploadNotif.setProgress(100, newPercent, false);
                            notificationManager.notify(0, uploadNotif.build());
                        }
                        percentUploaded = newPercent;
                    }

                    //Server Response
                    String res = IOUtils.toString(connection.getInputStream());
                    Log.wtf(TAG,"Server Response: " + res);

                }
                catch ( MalformedURLException e){
                    Log.wtf(TAG, "Bad URL");
                }
                catch (IOException e){
                    Log.wtf(TAG, "Connection Error");
                }
                finally {
                    if ( connection != null){
                        connection.disconnect();

                        uploadNotif.setContentTitle("Uploaded: " + image.getName())
                                // Removes the progress bar
                                .setProgress(0,0,false)
                                .setStyle(new Notification.BigPictureStyle().bigPicture(BitmapFactory.decodeFile(image.getAbsolutePath())));

                        notificationManager.notify(0,uploadNotif.build());

                        SharedPreferences uploads =  getSharedPreferences("Uploads", MODE_PRIVATE);
                        uploads.edit().putBoolean(image.getAbsolutePath(), true).commit();

                        Intent broadcastUploaded = new Intent(ImageGalleryActivity.FILE_UPLOADED);
                        LocalBroadcastManager.getInstance(UploadFileService.this).sendBroadcast(broadcastUploaded);

                        if(params.getExtras().get("delete").equals("true"))
                        {
                            uploads.edit().remove(image.getAbsolutePath()).commit();
                            image.delete();
                        }

                        jobFinished(params, false);
                    }
                }
            }
        });

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        //mJobHandler.removeMessages( 1 );
        return false;
    }
}
