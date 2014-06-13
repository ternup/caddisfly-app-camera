/*
 * Copyright (C) TernUp Research Labs
 *
 * This file is part of Caddisfly
 *
 * Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package com.ternup.caddisfly.activity;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.util.FileUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PhotoIntentActivity extends Activity {

    public static final String PREFS_NAME = "Caddisfly_Preferences";

    private static final int ACTION_TAKE_PHOTO_B = 1;

    Button.OnClickListener mTakePicOnClickListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
                }
            };

    private static final int ACTION_TAKE_PHOTO_S = 2;

    Button.OnClickListener mTakePicSOnClickListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakePictureIntent(ACTION_TAKE_PHOTO_S);
                }
            };

    private static final String BITMAP_STORAGE_KEY = "viewBitmap";

    private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageViewVisibility";

    private ImageView mImageView;

    private LinearLayout mImageLayout;

/*
    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = new File(Utility.getAlbumStorageDir());

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }*/

    private Bitmap mImageBitmap;

    private String mCurrentPhotoPath;

    /*

        public boolean saveImageToInternalStorage(Bitmap image,Context context)
        {
            try {
                FileOutputStream fos = context.openFileOutput("photo.jpg", Context.MODE_WORLD_READABLE);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                // 100 means no compression, the lower you go, the stronger the compression
                fos.close();
                return true;
            }
            catch (Exception e) {
                Log.e("saveToInternalStorage()", e.getMessage());
            }
            return false;
        }
    */
    public static Bitmap loadBitmap(String path, int orientation, final int targetWidth,
            final int targetHeight) {
        Bitmap bitmap;
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            // Adjust extents
            int sourceWidth, sourceHeight;
            if (orientation == 90 || orientation == 270) {
                sourceWidth = options.outHeight;
                sourceHeight = options.outWidth;
            } else {
                sourceWidth = options.outWidth;
                sourceHeight = options.outHeight;
            }

            // Calculate the maximum required scaling ratio if required and load the bitmap
            if (sourceWidth > targetWidth || sourceHeight > targetHeight) {
                float widthRatio = (float) sourceWidth / (float) targetWidth;
                float heightRatio = (float) sourceHeight / (float) targetHeight;
                float maxRatio = Math.max(widthRatio, heightRatio);
                options.inJustDecodeBounds = false;
                options.inSampleSize = (int) maxRatio;
                bitmap = BitmapFactory.decodeFile(path, options);
            } else {
                bitmap = BitmapFactory.decodeFile(path);
            }

            // Rotate the bitmap if required
            if (orientation > 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(orientation);
                bitmap = Bitmap
                        .createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                                true);
            }

            // Re-scale the bitmap if necessary
            sourceWidth = bitmap.getWidth();
            sourceHeight = bitmap.getHeight();
            if (sourceWidth != targetWidth || sourceHeight != targetHeight) {
                float widthRatio = (float) sourceWidth / (float) targetWidth;
                float heightRatio = (float) sourceHeight / (float) targetHeight;
                float maxRatio = Math.max(widthRatio, heightRatio);
                sourceWidth = (int) ((float) sourceWidth / maxRatio);
                sourceHeight = (int) ((float) sourceHeight / maxRatio);
                bitmap = Bitmap.createScaledBitmap(bitmap, sourceWidth, sourceHeight, true);
            }
        } catch (Exception e) {
            return null;
        }
        return bitmap;
    }

    /**
     * Indicates whether the specified action can be used as an intent. This
     * method queries the package manager for installed packages that can
     * respond to an intent with the specified action. If no suitable package is
     * found, this method returns false.
     * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
     *
     * @param context The application's environment.
     * @param action  The Intent action to check for availability.
     * @return True if an Intent with the specified action can be sent and
     * responded to, false otherwise.
     */
    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        long imageFileName = getIntent().getLongExtra(getString(R.string.currentLocationId), -1);
        //File albumF = getAlbumDir();
        File imageF = new File(FileUtils.getStoragePath(this, imageFileName, "", true) + "/photo");
        imageF.createNewFile();
        return imageF;
    }

    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }

    private void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
        /* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
        //int targetW = mImageLayout.getWidth();
        //int targetH = mImageLayout.getHeight();
        int targetW;
        int targetH;

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

//        if(photoW > photoH){
//            int tempH = photoW;
//            photoW = photoH;
//            photoH = tempH;
//        }

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        targetH = metrics.heightPixels;
        targetW = metrics.widthPixels;
                /* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        //Bitmap bitmap = loadBitmap(mCurrentPhotoPath, 90, targetW, targetH);

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        bitmap = Bitmap
                .createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        mImageView.setImageBitmap(bitmap);
        mImageView.setVisibility(View.VISIBLE);
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void dispatchTakePictureIntent(int actionCode) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        switch (actionCode) {
            case ACTION_TAKE_PHOTO_S:
            case ACTION_TAKE_PHOTO_B:
                File f;

                try {
                    f = setUpPhotoFile();
                    mCurrentPhotoPath = f.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                } catch (IOException e) {
                    e.printStackTrace();
                    mCurrentPhotoPath = null;
                }
                break;

            default:
                break;
        } // switch

        startActivityForResult(takePictureIntent, actionCode);
    }

    private void handleSmallCameraPhoto(Intent intent) {
        if (mCurrentPhotoPath != null) {

            Bundle extras = intent.getExtras();
            mImageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(mImageBitmap);

            //Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            //Matrix matrix = new Matrix();
            //matrix.postRotate(90);
            //bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            mImageView.setVisibility(View.VISIBLE);

            //galleryAddPic();

            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("photoPath", mCurrentPhotoPath);
            editor.commit();

            mCurrentPhotoPath = null;
        }
    }

    private void handleBigCameraPhoto() {

        if (mCurrentPhotoPath != null) {
            setPic();
            //galleryAddPic();

            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("photoPath", mCurrentPhotoPath);
            editor.commit();

            mCurrentPhotoPath = null;

            Intent intent = new Intent();
            this.setResult(Activity.RESULT_OK, intent);

            finish();
        }

    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageLayout = (LinearLayout) findViewById(R.id.imageLayout);
        mImageBitmap = null;

/*
        Button picBtn = (Button) findViewById(R.id.btnIntend);
        setBtnListenerOrDisable(
                picBtn,
                mTakePicOnClickListener,
                MediaStore.ACTION_IMAGE_CAPTURE
        );

        Button picSBtn = (Button) findViewById(R.id.btnIntendS);
        setBtnListenerOrDisable(
                picSBtn,
                mTakePicSOnClickListener,
                MediaStore.ACTION_IMAGE_CAPTURE
        );
*/
    }

    @Override
    public void onStart() {
        super.onStart();

        //String imageFileName = "temp";
        //File albumF = getAlbumDir();
        //File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);

        mImageLayout.post(new Runnable() {
            public void run() {
                //  String imageFileName = getIntent().getStringExtra("fileName");
                //mCurrentPhotoPath = Utility.getAlbumStorageDir() +  imageFileName;

                //if (mCurrentPhotoPath.isEmpty()) {
                dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
                // } else {
                //String path = getAlbumDir() + "/caddisfly_temp.jpg";
                //File file = new File(path);
                //   setPic();
                //}
            }
        });


/*
        if (file.exists()) {
            setPic();
        } else {

        }
*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTION_TAKE_PHOTO_B: {
                if (resultCode == RESULT_OK) {
                    handleBigCameraPhoto();
                } else {
                    finish();
                }
                break;
            } // ACTION_TAKE_PHOTO_B

            case ACTION_TAKE_PHOTO_S: {
                if (resultCode == RESULT_OK) {
                    handleSmallCameraPhoto(data);
                } else {
                    finish();
                }
                break;
            } // ACTION_TAKE_PHOTO_S

        } // switch
    }

    // Some lifecycle callbacks so that the image can survive orientation change
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
        outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
        mImageView.setImageBitmap(mImageBitmap);
        mImageView.setVisibility(
                savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ?
                        ImageView.VISIBLE : ImageView.INVISIBLE
        );
    }

    private void setBtnListenerOrDisable(
            Button btn,
            Button.OnClickListener onClickListener,
            String intentName
    ) {
        if (isIntentAvailable(this, intentName)) {
            btn.setOnClickListener(onClickListener);
        } else {
            btn.setText(
                    "Cannot" + " " + btn.getText());
            btn.setClickable(false);
        }
    }

}