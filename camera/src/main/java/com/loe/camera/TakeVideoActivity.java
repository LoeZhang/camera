package com.loe.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class TakeVideoActivity extends AppCompatActivity
{
    private VideoConfig config;

    private Camera camera;

    // 相机信息
    private ProcessCameraProvider cameraProvider;
    // 预览对象
    private Preview preview;

    // 当前相机
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

    // 录像用例
    private VideoCapture videoCapture;

    private boolean isRecording;

    private int padding0;
    private int padding1;

    private String tempPath;

    private boolean isFront;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        int statusHeight = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.parseColor("#cc000000"));
            statusHeight = dp_px(22);
        }
        setContentView(R.layout.take_video_activity);

        try
        {
            config = (VideoConfig) getIntent().getSerializableExtra(VideoConfig.KEY);
        }catch (Exception e)
        {
            config = new VideoConfig();
        }

        padding0 = dp_px(6);
        padding1 = dp_px(17);

        initEvent();

        if (config.isFront())
        {
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
            imageLight.setVisibility(View.GONE);
        }
        else
        {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
            imageLight.setVisibility(View.VISIBLE);
        }
        int vh = (int) (getVirtualBarHeight() * 0.7);
        layoutContent0.setPadding(0, statusHeight, 0, vh);
        layoutContent1.setPadding(0, statusHeight, 0, vh);
    }

    private CameraXCustomPreviewView viewFinder;
    private ImageView buttonRecord;
    private ImageView imageLight;
    private View imageSwitch;
    private View layoutRecord;
    private TextView textTime;
    private View layoutContent0;
    private View layoutContent1;
    private VideoView videoView;

    private CameraAsyncTimer timer = new CameraAsyncTimer(1000, 1000)
    {
        @Override
        protected void logic()
        {
            textTime.setText(String.format("%02d:%02d", time / 60, time % 60));

            if (time >= config.getMaxSeconds())
            {
                TakeVideoActivity.this.stop();
            }
        }
    };

    private void initEvent()
    {
        viewFinder = findViewById(R.id.viewFinder);
        imageLight = findViewById(R.id.imageLight);
        buttonRecord = findViewById(R.id.buttonRecord);
        imageSwitch = findViewById(R.id.imageSwitch);
        layoutRecord = findViewById(R.id.layoutRecord);
        textTime = findViewById(R.id.textTime);
        layoutContent0 = findViewById(R.id.layoutContent0);
        layoutContent1 = findViewById(R.id.layoutContent1);
        videoView = findViewById(R.id.videoView);

        float videoProportion = 16 / 9f;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels + getVirtualBarHeight();
        float screenProportion = (float) screenHeight / (float) screenWidth;
        android.view.ViewGroup.LayoutParams lp = videoView.getLayoutParams();

        if (videoProportion < screenProportion)
        {
            lp.height = screenHeight;
            lp.width = (int) ((float) screenHeight / videoProportion);
        }
        else
        {
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth * videoProportion);
        }
        videoView.setLayoutParams(lp);

        findViewById(R.id.layoutRecord).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!isRecording)
                {
                    // 开始录制
                    textTime.setVisibility(View.VISIBLE);
                    isRecording = true;
                    imageSwitch.setVisibility(View.GONE);
                    textTime.setText("00:00");
                    buttonRecord.setImageResource(R.drawable.camera_record_1);
                    layoutRecord.setPadding(padding1, padding1, padding1, padding1);
                    takeVideo();
                }
                else
                {
                    // 停止录制
                    stop();
                }
            }
        });

        findViewById(R.id.imageBack).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        if(!config.isShowAlbum()) findViewById(R.id.imageAlbum).setVisibility(View.INVISIBLE);
        findViewById(R.id.imageAlbum).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                startActivityForResult(intent, 0);
            }
        });

        imageLight.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (imageLight.getTag() == null)
                {
                    camera.getCameraControl().enableTorch(true);
                    imageLight.setImageResource(R.drawable.camera_light_1);
                    imageLight.setTag(1);
                }
                else
                {
                    resetLight();
                }
            }
        });

        imageSwitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                resetLight();
                if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                {
                    cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                    imageLight.setVisibility(View.GONE);
                }
                else
                {
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                    imageLight.setVisibility(View.VISIBLE);
                }
                startCamera();
            }
        });

        findViewById(R.id.videoBack).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                resetRecord();
            }
        });

        findViewById(R.id.videoOk).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                save();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            try
            {
                // 相册选择
                Uri uri = data.getData();
                // 选择返回
                if (uri != null)
                {
                    // 得到全路径
                    String path = CameraUriUtil.getUriPath(this, uri);
                    setResult(RESULT_OK, new Intent().putExtra("path", path));
                    finish();
                }
            } catch (Exception e)
            {
            }
        }
    }

    private void playVideo()
    {
        viewFinder.setVisibility(View.GONE);
        layoutContent0.setVisibility(View.GONE);
        layoutContent1.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoPath(tempPath);
        videoView.start();
    }

    private void resetRecord()
    {
        viewFinder.setVisibility(View.VISIBLE);
        layoutContent0.setVisibility(View.VISIBLE);
        layoutContent1.setVisibility(View.GONE);
        videoView.setVisibility(View.GONE);
        videoView.stopPlayback();
        videoView.suspend();
        startCamera();
    }

    @SuppressLint("RestrictedApi")
    private void stop()
    {
        if(isRecording)
        {
            timer.stop();
            textTime.setVisibility(View.GONE);
            imageSwitch.setVisibility(View.VISIBLE);
            videoCapture.stopRecording();
            buttonRecord.setImageResource(R.drawable.camera_record_0);
            layoutRecord.setPadding(padding0, padding0, padding0, padding0);
            isRecording = false;
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        isFront = true;

        if (viewFinder.getVisibility() == View.VISIBLE)
        {
            if (allPermissionsGranted())
            {
                startCamera();
            }
            else
            {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 0);
            }
        }
        else
        {
            playVideo();
        }
    }

    private void startCamera()
    {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable()
        {
            @SuppressLint("RestrictedApi")
            @Override
            public void run()
            {
                try
                {
                    cameraProvider = cameraProviderFuture.get();//获取相机信息

                    //预览配置
                    preview = new Preview.Builder().build();
                    preview.setSurfaceProvider(viewFinder.createSurfaceProvider());

                    videoCapture = new VideoCapture.Builder() //录像用例配置
                            .setBitRate(config.getBitRate() * 1000).setTargetAspectRatio(AspectRatio.RATIO_16_9) //设置高宽比
                            //                .setTargetRotation(viewFinder.display.rotation) //设置旋转角度
                            .setAudioRecordSource(MediaRecorder.AudioSource.MIC)//设置音频源麦克风
                            .build();

                    cameraProvider.unbindAll(); //先解绑所有用例
                    camera = cameraProvider.bindToLifecycle(TakeVideoActivity.this, cameraSelector, videoCapture,
                            // imageCapture,
                            // imageAnalyzer,
                            preview);//绑定用例

                    // initCameraListener();

                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("RestrictedApi")
    private void takeVideo()
    {
        // 视频保存路径
        File tempFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/LoeVideo/temp.mp4");
        if (!tempFile.getParentFile().exists())
        {
            tempFile.getParentFile().mkdirs();
        }

        timer.start();
        // 开始录像
        videoCapture.startRecording(tempFile, Executors.newSingleThreadExecutor(), new VideoCapture.OnVideoSavedCallback()
        {
            @Override
            public void onVideoSaved(@NonNull final File file)
            {
                // 保存视频成功回调，会在停止录制时被调用
                if (isFront)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            tempPath = file.getAbsolutePath();
                            resetLight();
                            playVideo();
                        }
                    });
                }
            }

            @Override
            public void onError(int videoCaptureError, @NonNull final String message, @Nullable Throwable cause)
            {
                if (isFront)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(getBaseContext(), "录制出错：" + message, Toast.LENGTH_SHORT).show();
                            stop();
                        }
                    });
                }
            }
        });
    }

    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};

    private boolean allPermissionsGranted()
    {
        for (String requiredPermission : REQUIRED_PERMISSIONS)
        {
            if (ContextCompat.checkSelfPermission(this, requiredPermission) != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (allPermissionsGranted())
        {
            startCamera();
        }
        else
        {
            Toast.makeText(this, "缺少拍摄相关权限", Toast.LENGTH_SHORT).show();
            finish();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public int dp_px(float dpValue)
    {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void save()
    {
        File tempFile = new File(tempPath);
        try
        {
            File newFile = null;
            if(config.getSavePath() != null)
            {
                newFile = new File(config.getSavePath());
            }else
            {
                newFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                        .getPath() + "/LoeVideo/" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.CHINA).format(System.currentTimeMillis()) + ".mp4");
            }
            if (!newFile.getParentFile().exists())
            {
                newFile.getParentFile().mkdirs();
            }
            tempFile.renameTo(newFile);
            setResult(RESULT_OK, new Intent().putExtra("path", newFile.getAbsolutePath()));
        } catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(this, "保存出错", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    public int getVirtualBarHeight()
    {
        int vh = 0;
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try
        {
            @SuppressWarnings("rawtypes") Class c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked") Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            vh = dm.heightPixels - windowManager.getDefaultDisplay().getHeight();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return vh;
    }

    private void resetLight()
    {
        if (imageLight.getTag() != null)
        {
            camera.getCameraControl().enableTorch(false);
            imageLight.setImageResource(R.drawable.camera_light_0);
            imageLight.setTag(null);
        }
    }

    @Override
    protected void onPause()
    {
        isFront = false;
        stop();
        resetLight();
        cameraProvider.unbindAll();
        super.onPause();
    }

    @Override
    public void finish()
    {
        super.finish();
        if (LoeCamera.onDestroyAnimate != null)
        {
            LoeCamera.onDestroyAnimate.onOut(this);
        }
    }

    @Override
    protected void onDestroy()
    {
        if (timer != null)
        {
            timer.finish();
        }

        if (tempPath != null)
        {
            File tempFile = new File(tempPath);
            if (tempFile.exists())
            {
                tempFile.delete();
            }
        }

        super.onDestroy();
    }
}