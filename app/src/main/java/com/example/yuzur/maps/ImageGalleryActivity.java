package com.example.yuzur.maps;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.PopupMenuCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

/**
 * Created by Chris Ozawa on 2/15/2018.
 */

public class ImageGalleryActivity extends AppCompatActivity {
    private DrawerManager mDrawerManager;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mNavMenuOptions;
    private ActionBarDrawerToggle mDrawerToggle;

    private PopupWindow popupWindow;
    private boolean popupShowing = false;

    private static final int REQUEST_STORAGE_CODE = 0;

    public static final String IMAGE_FILENAME = "image_filename";

    public class ImageAdapter extends BaseAdapter {

        private Context mContext;
        ArrayList<String> itemList = new ArrayList<String>();
        ArrayList<Boolean> uploadStatusList = new ArrayList<Boolean>();
        Display display = getWindowManager().getDefaultDisplay();

        public ImageAdapter(Context c) {
            mContext = c;
        }

        void add(String path, Boolean status){
            itemList.add(path);
            uploadStatusList.add(status);
        }

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            /*
            ImageView imageView;

            Point size = new Point();
            display.getSize(size);
            final int imageLength = (size.x / 4) - (size.x / 100);
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
            } else {
                imageView = (ImageView) convertView;
            }
            final File image = new File(itemList.get(position));
            Picasso.with(ImageGalleryActivity.this)
                    .load(image)
                    .error(R.drawable.qualitylogo)
                    .centerCrop()
                    .resize(imageLength,imageLength)
                    .into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(ImageGalleryActivity.this, DisplayImageActivity.class);
                    i.putExtra(IMAGE_FILENAME, itemList.get(position));
                    i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(i);
                }
            });


            return imageView;
            */
            Point size = new Point();
            display.getSize(size);
            final int imageLength = (size.x / 4) - (size.x / 100);

            View gridView = convertView;

            if (convertView == null) {  // if it's not recycled, initialize some attributes
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
                gridView = inflater.inflate(R.layout.custom_gridview_layout, null);

            }


            ImageView imageView = (ImageView) gridView.findViewById(R.id.imageView);
            final File image = new File(itemList.get(position));
            Picasso.with(ImageGalleryActivity.this)
                    .load(image)
                    .error(R.drawable.qualitylogo)
                    .centerCrop()
                    .resize(imageLength,imageLength)
                    .into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(ImageGalleryActivity.this, DisplayImageActivity.class);
                    i.putExtra(IMAGE_FILENAME, itemList.get(position));
                    i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(i);
                }
            });

            ImageView uploadIcon = (ImageView) gridView.findViewById(R.id.uploadstatus);

            if(uploadStatusList.get(position)){
                Picasso.with(ImageGalleryActivity.this)
                        .load(R.drawable.ic_green_check)
                        .error(R.drawable.qualitylogo)
                        .centerCrop()
                        .resize(imageLength/4,imageLength/4)
                        .into(uploadIcon);
            }


            return gridView;
        }

    }

    ImageAdapter myImageAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);

        //checkPermissions(); this is being called on resume

        mDrawerManager = new DrawerManager(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navList);
        mDrawerToggle = mDrawerManager.setUpDrawer(mDrawerLayout, mDrawerList, DrawerManager.GALLERY);
    }

    @Override
    public void onResume(){
        super.onResume();
        checkPermissions();
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void checkPermissions() {
        // If storage permission is not granted yet
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //If user denied access, request permissions again
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.WRITE_EXTERNAL_STORAGE , android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_CODE);
            }
            //Else If user has not denied access, request permissions
            else {
                ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.WRITE_EXTERNAL_STORAGE , android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_CODE);
            }
        } else {
            GridView gridview = (GridView) findViewById(R.id.gridview);
            myImageAdapter = new ImageAdapter(this);
            gridview.setAdapter(myImageAdapter);

            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "WIFIRE");

            String targetPath = mediaStorageDir.getAbsolutePath();

            File targetDirector = new File(targetPath);

            File[] files = targetDirector.listFiles();

            SharedPreferences uploads =  getSharedPreferences("Uploads", MODE_PRIVATE);
            if(files != null){
                for (File file : files){
                    String pathname = file.getAbsolutePath();
                    Boolean uploadstatus = uploads.getBoolean(pathname, false);
                    myImageAdapter.add(pathname, uploadstatus);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    GridView gridview = (GridView) findViewById(R.id.gridview);
                    myImageAdapter = new ImageAdapter(this);
                    gridview.setAdapter(myImageAdapter);

                    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES), "WIFIRE");

                    String targetPath = mediaStorageDir.getAbsolutePath();

                    File targetDirector = new File(targetPath);


                    File[] files = targetDirector.listFiles();
                    SharedPreferences uploads =  getSharedPreferences("Uploads", MODE_PRIVATE);
                    if(files != null){
                        for (File file : files){
                            String pathname = file.getAbsolutePath();
                            Boolean uploadstatus = uploads.getBoolean(pathname, false);
                            myImageAdapter.add(pathname, uploadstatus);
                        }
                    }


                } else {
                    Toast.makeText(this, "Please enable storage permissions to access the gallery.", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

}
