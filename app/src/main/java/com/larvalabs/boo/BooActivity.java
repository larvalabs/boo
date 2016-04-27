package com.larvalabs.boo;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import java.io.IOException;

import static com.larvalabs.boo.Util.log;

public class BooActivity extends Activity implements SurfaceHolder.Callback {

    private static final int[][] BACKGROUNDS = {
            {R.color.background_pink_start, R.color.background_pink_end},
            {R.color.background_yellow_start, R.color.background_yellow_end},
            {R.color.background_blue_start, R.color.background_blue_end},
            {R.color.background_green_start, R.color.background_green_end},
            {R.color.background_orange_start, R.color.background_orange_end},
            {R.color.background_purple_start, R.color.background_purple_end},
            {R.color.background_red_start, R.color.background_red_end},
            {R.color.background_teal_start, R.color.background_teal_end},
    };

    // Turn this off when testing the creature behavior so you can actually look at the phone
    private static final boolean HIDE_FROM_FACE = true;

    // Don't hide right away after intro
    private static final long FIRST_HIDE_BUFFER = 2500;

    // After hiding, don't hide again too quickly
    private static final long HIDE_BACKOFF_TIME = 1500;

    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private boolean preview = false;
    private boolean foundFaces = false;

    private long lastHideTime = 0;

    private CreaturesView introView;
    private CreaturesView creaturesView;

    private RadialGradientView gradientView;

    private int backgroundIndex;

    private boolean introRunning = true;
    private long startTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_boo);
        surfaceView = (SurfaceView)findViewById(R.id.preview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        introView = (CreaturesView) findViewById(R.id.intro);
        introView.setIntroMode(true);
        creaturesView = (CreaturesView) findViewById(R.id.creatures);
        gradientView = (RadialGradientView) findViewById(R.id.background);
        backgroundIndex = 0;
    }

    Camera.FaceDetectionListener faceDetectionListener = new Camera.FaceDetectionListener(){

        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            // Don't do this until the intro is done, and after intro wait a bit before detecting faces
            if (introRunning) {
                return;
            } else if (System.currentTimeMillis() - startTime < FIRST_HIDE_BUFFER) {
                return;
            }
            boolean found = faces.length >= 1;
            if (foundFaces != found && HIDE_FROM_FACE) {
                long time = System.currentTimeMillis();
                if (found) {
                    log(" - Found a face!");
                    boolean hiding = creaturesView.setFaceVisible(true);
                    if (hiding) {
                        creaturesView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                changeBackground();
                            }
                        }, 500);
                    }
                    foundFaces = true;
                    lastHideTime = time;
                } else if (time - lastHideTime > HIDE_BACKOFF_TIME) {
                    log(" - No faces!");
                    creaturesView.setFaceVisible(false);
                    foundFaces = false;
                } else {
                    long delay = HIDE_BACKOFF_TIME - (time - lastHideTime);
                    creaturesView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (foundFaces == true) {
                                log(" - No faces (delayed)!");
                                creaturesView.setFaceVisible(false);
                                foundFaces = false;
                            }
                        }
                    }, delay);
                }
            }
        }
    };

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
        if(preview){
            camera.stopFaceDetection();
            camera.stopPreview();
            preview = false;
        }

        if (camera != null){
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.setDisplayOrientation(90);
                camera.startPreview();
                camera.startFaceDetection();
                preview = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int n = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < n; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                camera = Camera.open(i);
                camera.setFaceDetectionListener(faceDetectionListener);
                return;
            }
        }
        Util.error("Could not open front-facing camera!");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            camera.stopFaceDetection();
            camera.stopPreview();
            camera.release();
            camera = null;
            preview = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void endIntro() {
        // Fade out the experiment title view
        final View title = findViewById(R.id.title);
        Util.log("ENDING INTRO");
        title.animate().alpha(0).setDuration(600).setStartDelay(1000).start();
        introView.postDelayed(new Runnable() {
            @Override
            public void run() {
                changeBackground();
            }
        }, 500);
        introView.postDelayed(new Runnable() {
            @Override
            public void run() {
                creaturesView.setVisibility(View.VISIBLE);
                introView.setVisibility(View.GONE);
                introRunning = false;
                startTime = System.currentTimeMillis();
            }
        }, 2000);
    }

    private void changeBackground() {
        backgroundIndex++;
        if (backgroundIndex == BACKGROUNDS.length) {
            backgroundIndex = 0;
        }
        gradientView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int startColor = getResources().getColor(BACKGROUNDS[backgroundIndex][0]);
                int endColor = getResources().getColor(BACKGROUNDS[backgroundIndex][1]);
                gradientView.changeColor(startColor, endColor);
            }
        }, 500);
    }

}
