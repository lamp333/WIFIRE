package com.example.yuzur.maps;

import android.content.Intent;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

public class DisplayImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        setContentView(R.layout.activity_display_image);

        ImageView imageView = (ImageView) findViewById(R.id.image_full);

        Intent i = getIntent();
        String filename = i.getStringExtra(ImageGalleryActivity.IMAGE_FILENAME);
        Log.wtf("Image Path", filename);
        File image = new File(filename);
        if(image != null) {
            Picasso.with(DisplayImageActivity.this)
                    .load(image)
                    .error(R.drawable.qualitylogo)
                    .noFade()
                    .resize(size.y, 0)
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                        }
                    });
        } else {
            goBackToGallery();
        }
    }

    public void onClick(View v) {
        if(v.getId() == R.id.back){
            goBackToGallery();
        }
        else if (v.getId() == R.id.delete){
            Log.wtf("DELETE", "pls");
        }
    }
    @Override
    public void onBackPressed() {
        goBackToGallery();
    }

    private void goBackToGallery() {
        /*Intent i = new Intent(DisplayImageActivity.this, ImageGalleryActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);*/
        finish();
    }
}
