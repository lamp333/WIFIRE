package com.example.yuzur.maps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

public class DisplayImageActivity extends AppCompatActivity {
    File image;
    View topBar;
    View bottomBar;

    Animation fadeOutTopAnim;
    Animation fadeOutBotAnim;
    Animation fadeInAnim;
    boolean showMenu;

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
        String filename = getIntent().getStringExtra(ImageGalleryActivity.IMAGE_FILENAME);
        image = new File(filename);
        showMenu = true;
        setAnimations();
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

    public void setAnimations(){
        topBar = findViewById(R.id.l1);
        bottomBar = findViewById(R.id.l2);

        fadeOutTopAnim = AnimationUtils.loadAnimation(DisplayImageActivity.this, android.R.anim.fade_out);
        fadeOutBotAnim = AnimationUtils.loadAnimation(DisplayImageActivity.this, android.R.anim.fade_out);
        fadeInAnim = AnimationUtils.loadAnimation(DisplayImageActivity.this, android.R.anim.fade_in);


        fadeOutTopAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                topBar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeOutBotAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bottomBar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void fadeOut(){
        topBar.startAnimation(fadeOutTopAnim);
        bottomBar.startAnimation(fadeOutBotAnim);
        showMenu = false;
    }
    public void fadeIn(){
        topBar.setVisibility(View.VISIBLE);
        bottomBar.setVisibility(View.VISIBLE);
        topBar.startAnimation(fadeInAnim);
        bottomBar.startAnimation(fadeInAnim);
        showMenu = true;
    }

    public void onClick(View v) {
        if(v.getId() == R.id.back){
            goBackToGallery();
        }
        else if (v.getId() == R.id.delete){
            SharedPreferences uploads =  getSharedPreferences("Uploads", MODE_PRIVATE);
            uploads.edit().remove(image.getAbsolutePath()).commit();
            image.delete();
            goBackToGallery();
        }
        else{
            if(showMenu){
                fadeOut();
            }
            else{
                fadeIn();
            }
        }


    }
    @Override
    public void onBackPressed() {
        goBackToGallery();
    }

    private void goBackToGallery() {
        /*
        Intent i = new Intent (DisplayImageActivity.this, ImageGalleryActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);*/
        this.finish();
    }
}
