package com.example.micro.slowmotiontest;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.core.app.ActivityCompat;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioListener;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.Formatter;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private PlayerView mExoPlayerView;
    private SimpleExoPlayer mSimpleExoPlayer;
    private ExoPlayer mExoPlayer;
    Uri playerUri = Uri.parse("http://demos.webmproject.org/exoplayer/glass.mp4");
    private ProgressBar mProgressBar;

    private final int UPDATE_PROGRESS = 0;
    private final int UPDATE_PROGRESS_IN_MS = 50;
    boolean mIsSlowRange = false;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PROGRESS:
                    if (mSimpleExoPlayer == null)
                        return;
                    checkInsideRange();
                    try{
                        if (mSimpleExoPlayer.getPlayWhenReady()) {
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
        long pos = mSimpleExoPlayer.getCurrentPosition();
        Log.d(TAG, "TTT checkInsideRange pos = "+pos);
        if (pos > 5000 && pos <= 10000) {
                if (!mIsSlowRange) {
                    mIsSlowRange = true;
                    Log.d(TAG, "TTT setMute vol");
                    mSimpleExoPlayer.setVolume(0.0f);
                    PlaybackParameters param = new PlaybackParameters(0.0625f);
                    mSimpleExoPlayer.setPlaybackParameters(param);
                }
            } else if (pos > 10000) {
                if (mIsSlowRange) {
                    mIsSlowRange = false;
                    Log.d(TAG, "TTT setBack vol");
                    PlaybackParameters param = new PlaybackParameters(1.0f);
                    mSimpleExoPlayer.setPlaybackParameters(param);
                    mSimpleExoPlayer.setVolume(1.0f);

                }
            }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "TTT package name = "+getApplicationContext().getPackageName());

        isStoragePermissionGranted();
        Uri uri = getLastContentUri(this);
        Log.d(TAG, "TTT lastVideo = "+uri);

        initExoPlayer();
        playVideo();
    }

    private void initExoPlayer() {
        //1. 创建一个默认的 TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        //2.创建ExoPlayer
        mSimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this);
        //3.创建SimpleExoPlayerView
        mExoPlayerView = findViewById(R.id.exoView);
        mExoPlayerView.requestFocus();
        //4.为SimpleExoPlayer设置播放器
        mExoPlayerView.setPlayer(mSimpleExoPlayer);
//        mProgressBar = findViewById(R.id.progressBar);
    }

    private void playVideo() {
//        //测量播放过程中的带宽。 如果不需要，可以为null。
//        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        // 生成加载媒体数据的DataSource实例。
        DataSource.Factory dataSourceFactory
                = new DefaultDataSourceFactory(MainActivity.this,
                Util.getUserAgent(MainActivity.this,this.getString(R.string.app_name)));
        int id = this.getRawResIdByName("balalala");
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + id);
//        uri assetsUri = Uri.parse("asset:///balalala.mp4");
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(getLastContentUri(this));
//        // 生成用于解析媒体数据的Extractor实例。
//        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        mSimpleExoPlayer.seekToDefaultPosition();
        //Prepare the player with the source.
//        mSimpleExoPlayer.prepare(videoSource);
        //添加监听的listener
//        mSimpleExoPlayer.setVideoListener(mVideoListener);
        mSimpleExoPlayer.addListener(eventListener);
//        mSimpleExoPlayer.setTextOutput(mOutput);
        mSimpleExoPlayer.setPlayWhenReady(true);
        mSimpleExoPlayer.prepare(videoSource);
        mHandler.sendEmptyMessage(UPDATE_PROGRESS);
        Log.d(TAG, "TTT getState = "+mSimpleExoPlayer.getPlaybackState());

    }


    public int getRawResIdByName(String resName) {
                String pkgName = this.getPackageName();
                // Return 0 if not found.
                        int resID = this.getResources().getIdentifier(resName, "raw", pkgName);
                Log.i("AndroidVideoView", "Res Name: " + resName + "==> Res ID = " + resID);
                return resID;
            }



    TextRenderer.Output mOutput = new TextRenderer.Output() {
        @Override
        public void onCues(List<Cue> cues) {
            Log.d(TAG, "MainActivity.onCues.");
        }
    };

    private SimpleExoPlayer.VideoListener mVideoListener = new SimpleExoPlayer.VideoListener() {
        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            Log.d(TAG, "MainActivity.onVideoSizeChanged.width:"+width+", height:"+height);

        }

        @Override
        public void onRenderedFirstFrame() {
            Log.d(TAG, "MainActivity.onRenderedFirstFrame.");
        }
    };


    private ExoPlayer.EventListener eventListener = new ExoPlayer.EventListener() {
//        @Override
//        public void onTimelineChanged(Timeline timeline, Object manifest) {
//            Log.d(TAG, "onTimelineChanged");
//        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Log.d(TAG, "onTracksChanged");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Log.d(TAG, "onLoadingChanged");
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.d(TAG, "onPlayerStateChanged: playWhenReady = "+String.valueOf(playWhenReady)
                    +" playbackState = "+playbackState);
            switch (playbackState){
                case ExoPlayer.STATE_ENDED:
                    Log.d(TAG, "Playback ended!");
                    //Stop playback and return to start position
                    setPlayPause(false);
                    mSimpleExoPlayer.seekTo(0);
                    break;
                case ExoPlayer.STATE_READY:
//                    mProgressBar.setVisibility(View.GONE);
                    Log.d(TAG, "ExoPlayer ready! pos: "+mSimpleExoPlayer.getCurrentPosition()
                            +" max: "+stringForTime((int)mSimpleExoPlayer.getDuration()));
                    setProgress(0);
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    Log.d(TAG, "Playback buffering!");
//                    mProgressBar.setVisibility(View.VISIBLE);
                    break;
                case ExoPlayer.STATE_IDLE:
                    Log.d(TAG, "ExoPlayer idle!");
                    break;
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.d(TAG, "onPlaybackError: "+error.getMessage());
        }

//        @Override
//        public void onPositionDiscontinuity() {
//            Log.d(TAG, "onPositionDiscontinuity");
//        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            Log.d(TAG, "MainActivity.onPlaybackParametersChanged."+playbackParameters.toString());
        }
    };

    /**
     * Starts or stops playback. Also takes care of the Play/Pause button toggling
     * @param play True if playback should be started
     */
    private void setPlayPause(boolean play){
        mSimpleExoPlayer.setPlayWhenReady(play);
    }

    private String stringForTime(int timeMs) {
        StringBuilder mFormatBuilder;
        Formatter mFormatter;
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds =  timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "MainActivity.onPause.");
        super.onPause();
        mSimpleExoPlayer.stop();
    }

    private static Uri getLastContentUri(Context context) {
        final Uri fileUri = MediaStore.Files.getContentUri("external");
        final String[] projection = {MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.DISPLAY_NAME};
        final String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " = "+MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;// + " and " +
//                MediaStore.Files.FileColumns.DISPLAY_NAME + " = ?";
//        final String[] selectionArgs = {"TWICE Heart Shaker MV.mp4"};
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

    @Override
    protected void onStop() {
        Log.d(TAG, "MainActivity.onStop.");
        super.onStop();
        mSimpleExoPlayer.release();
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
