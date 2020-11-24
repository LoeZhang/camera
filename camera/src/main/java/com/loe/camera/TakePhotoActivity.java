package com.loe.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.FocusMeteringResult;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TakePhotoActivity extends AppCompatActivity
{
    private PhotoConfig config;

    private Camera camera;
    private ExecutorService cameraExecutor;

    // 相机信息
    private ProcessCameraProvider cameraProvider;
    // 预览对象
    private Preview preview;

    // 当前相机
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

    // 拍照用例
    private ImageCapture imageCapture;

    private String tempPath;

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
        setContentView(R.layout.camera_take_photo_activity);

        config = (PhotoConfig) getIntent().getSerializableExtra(VideoConfig.KEY);
        if(config == null) config = new PhotoConfig();

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
    private ImageView imagePreview;
    private ImageView buttonRecord;
    private ImageView imageLight;
    private View layoutContent0;
    private View layoutContent1;
    private CameraFocusView focusView;

    private void initEvent()
    {
        imagePreview = findViewById(R.id.imagePreview);
        imageLight = findViewById(R.id.imageLight);
        viewFinder = findViewById(R.id.viewFinder);
        buttonRecord = findViewById(R.id.buttonRecord);
        layoutContent0 = findViewById(R.id.layoutContent0);
        layoutContent1 = findViewById(R.id.layoutContent1);
        focusView = findViewById(R.id.focusView);

        buttonRecord.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                takePhoto();
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

        if (!config.isShowAlbum())
        {
            findViewById(R.id.imageAlbum).setVisibility(View.INVISIBLE);
        }
        findViewById(R.id.imageAlbum).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
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

        findViewById(R.id.imageSwitch).setOnClickListener(new View.OnClickListener()
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

        viewFinder.setCustomTouchListener(new CameraXCustomPreviewView.CustomTouchListener()
        {
            @Override
            public void zoom(float delta)
            {
            }

            @Override
            public void click(float x, float y)
            {
                if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                {
                    MeteringPointFactory factory = viewFinder.createMeteringPointFactory(cameraSelector);
                    FocusMeteringAction action = new FocusMeteringAction.Builder(factory.createPoint(x, y), FocusMeteringAction.FLAG_AF).setAutoCancelDuration(3, TimeUnit.SECONDS).build();
                    focusView.startFocus(new Point((int) x, (int) y));
                    final ListenableFuture<?> future = camera.getCameraControl().startFocusAndMetering(action);
                    future.addListener(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                FocusMeteringResult result = (FocusMeteringResult) future.get();
                                if (result.isFocusSuccessful())
                                {
                                    focusView.onFocusSuccess();
                                }
                                else
                                {
                                    focusView.onFocusFailed();
                                }
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }, cameraExecutor);
                }
            }

            @Override
            public void doubleClick(float x, float y)
            {
            }

            @Override
            public void longClick(float x, float y)
            {
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
                    tempPath = CameraUriUtil.getUriPath(this, uri);
                    if (!config.isCompress())
                    {
                        setResult(RESULT_OK, new Intent().putExtra("path", tempPath));
                        finish();
                    }
                    else
                    {
                        save();
                    }
                }
            } catch (Exception e)
            {
            }
        }
    }

    private void playImage()
    {
        viewFinder.setVisibility(View.GONE);
        layoutContent0.setVisibility(View.GONE);
        layoutContent1.setVisibility(View.VISIBLE);
        imagePreview.setVisibility(View.VISIBLE);
        try
        {
            imagePreview.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(tempPath)));
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void resetRecord()
    {
        buttonRecord.setEnabled(true);
        viewFinder.setVisibility(View.VISIBLE);
        layoutContent0.setVisibility(View.VISIBLE);
        layoutContent1.setVisibility(View.GONE);
        imagePreview.setVisibility(View.GONE);
        startCamera();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (allPermissionsGranted())
        {
            startCamera();
        }
        else
        {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 0);
        }
    }

    private void startCamera()
    {
        cameraExecutor = Executors.newSingleThreadExecutor();
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

                    //                    ImageAnalysis imageAnalyzer = new ImageAnalysis.Builder().build();
                    //
                    //                    imageAnalyzer.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer()
                    //                    {
                    //                        @Override
                    //                        public void analyze(@NonNull ImageProxy image)
                    //                        {
                    //                        }
                    //                    });

                    imageCapture = new ImageCapture.Builder().build();//拍照用例配置

                    cameraProvider.unbindAll(); //先解绑所有用例
                    camera = cameraProvider.bindToLifecycle(TakePhotoActivity.this, cameraSelector, imageCapture,
                            //                             imageAnalyzer,
                            preview); //绑定用例

                    // initCameraListener();

                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto()
    {
        buttonRecord.setEnabled(false);
        // 保存路径
        final File tempFile = new File(CameraImgUtil.getPhotoTemp());
        if (!tempFile.getParentFile().exists())
        {
            tempFile.getParentFile().mkdirs();
        }

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(tempFile).build();
        // 开始拍照
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback()
        {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults)
            {
                tempPath = tempFile.getAbsolutePath();
                playImage();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception)
            {
                Toast.makeText(getBaseContext(), "拍照出错：" + exception, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

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
            Toast.makeText(this, "缺少拍照相关权限", Toast.LENGTH_SHORT).show();
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
            String newPath = null;
            if (config.getSavePath() != null)
            {
                newPath = config.getSavePath();
            }
            else
            {
                newPath = CameraImgUtil.getPhotoPath() + CameraImgUtil.getDate() + ".jpg";
            }
            CameraImgUtil.compressSize(tempFile.getAbsolutePath(), newPath, config.getMaxWidth(), config.getMaxHeight(), config.getMaxSize());
            setResult(RESULT_OK, new Intent().putExtra("path", newPath));
        } catch (Exception e)
        {
            Log.e("runtime", e.toString());
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
        resetLight();
        cameraProvider.unbindAll();
        cameraExecutor.shutdown();
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
        super.onDestroy();
    }
}