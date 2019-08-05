package com.example.micro.slowmotiontest;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private VideoView mVideoView;
    private ScaleTextureView mTextureview;
    private int mPosition = 0;
    private MediaController mMediaController;
    private MediaPlayer mMediaPlayer;
    private final int UPDATE_PROGRESS = 0;
    private final int UPDATE_PROGRESS_IN_MS = 50;
    boolean mIsSlowRange = false;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PROGRESS:
                    if (mMediaPlayer == null)
                        return;
                    checkInsideRange();
                    try{
                        if (mMediaPlayer.isPlaying()) {
                            msg = obtainMessage(UPDATE_PROGRESS);
                            sendMessageDelayed(msg, UPDATE_PROGRESS_IN_MS);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
            }
        }
    };

    private void checkInsideRange() {
        long pos = mMediaPlayer.getCurrentPosition();
        Log.d(TAG, "TTT checkInsideRange pos = "+pos);
        if (pos > 3000 && pos <= 5000) {
            if (!mIsSlowRange) {
                mIsSlowRange = true;
                Log.d(TAG, "TTT setMute vol");
                mMediaPlayer.setVolume(0, 0);
                mMediaPlayer.setPlaybackParams(mMediaPlayer.getPlaybackParams().setSpeed(0.3f));
            }
        } else if (pos > 5000) {
            if (mIsSlowRange) {
                mIsSlowRange = false;
                Log.d(TAG, "TTT setBack vol");
                mMediaPlayer.setPlaybackParams(mMediaPlayer.getPlaybackParams().setSpeed(1.0f));
                mMediaPlayer.setVolume(1, 1);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isStoragePermissionGranted();

//        mVideoView = (VideoView) findViewById(R.id.videoView);
        mTextureview = findViewById(R.id.textureView);
        TextView playTypeTextView = findViewById(R.id.player_type);
        playTypeTextView.append("MediaPlayer");

        // Set the media controller buttons
//        if (mMediaController == null) {
//            mMediaController = new MediaController(MainActivity.this);
//
//            // Set the videoView that acts as the anchor for the MediaController.
//            mMediaController.setAnchorView(mVideoView);
//
//            // Set MediaController for VideoView
//            mVideoView.setMediaController(mMediaController);
//        }


//        try {
////            Uri uri = getLastContentUri(this);
////            mMediaPlayer.setDataSource(this, uri);
//////            mVideoView.setVideoURI(uri);
////
////        } catch (Exception e) {
////            Log.e("Error", e.getMessage());
////            e.printStackTrace();
////        }

//        mVideoView.requestFocus();
//
//        // When the video file ready for playback.
//        mVideoView.setOnPreparedListener(this);
        final MediaPlayer.OnPreparedListener listener = this;
        final Context context = this;
        final Uri uri = getLastContentUri(this);
        mTextureview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.d(TAG, "TTT onSurfaceTextureAvailable");
                Surface s = new Surface(surface);
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setSurface(s);
                try {
                    mMediaPlayer.setDataSource(context, uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mMediaPlayer.prepareAsync();
                mMediaPlayer.setOnPreparedListener(listener);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "TTT onPrepared");
//        mVideoView.seekTo(mPosition);
//        if (mPosition == 0) {
//            mVideoView.start();
//        }

//        mMediaPlayer = mediaPlayer;
        mediaPlayer.start();
//        mHandler.sendEmptyMessage(UPDATE_PROGRESS);
        // When video Screen change size.
        mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

                // Re-Set the videoView that acts as the anchor for the MediaController
//                mMediaController.setAnchorView(mVideoView);
            }
        });
    }

    // Find ID corresponding to the name of the resource (in the directory raw).
    public int getRawResIdByName(String resName) {
        String pkgName = this.getPackageName();
        // Return 0 if not found.
        int resID = this.getResources().getIdentifier(resName, "raw", pkgName);
        Log.i("AndroidVideoView", "Res Name: " + resName + "==> Res ID = " + resID);
        return resID;
    }


    // When you change direction of phone, this method will be called.
    // It store the state of video (Current position)
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Store current position.
//        savedInstanceState.putInt("CurrentPosition", mVideoView.getCurrentPosition());
//        mVideoView.pause();
    }


    // After rotating the phone. This method is called.
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Get saved position.
//        mPosition = savedInstanceState.getInt("CurrentPosition");
//        mVideoView.seekTo(mPosition);
    }

    private static Uri getLastContentUri(Context context) {
        final Uri fileUri = MediaStore.Files.getContentUri("external");
        final String[] projection = {MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Files.FileColumns.MEDIA_TYPE};
        final String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " = "+MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
//        final String[] selectionArgs = {String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)};
        final String limit_1 = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC, " + MediaStore.Images.ImageColumns._ID + " DESC limit 1";
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(fileUri, projection, selection, null, limit_1);
            if (cursor != null && cursor.moveToFirst()) {
//                return cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                String latestData = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                Log.d(TAG, "TTT latestData = "+latestData);
                return ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID)));
            }
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG,"Permission is granted");
            return true;
        } else {

            Log.v(TAG,"Permission is revoked");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }
}
