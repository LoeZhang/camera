package com.loe.camera;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.fragment.app.FragmentActivity;

public class LoeCamera
{
    public static void takePhotoAlbum(final FragmentActivity activity, final OnPathCallback onPathCallback)
    {
        takePhotoAlbum(activity, new PhotoConfig(), onPathCallback);
    }

    public static void takePhotoAlbum(final FragmentActivity activity, final PhotoConfig config, final OnPathCallback onPathCallback)
    {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        CameraResultUtil.startResult(activity, intent, new CameraResultUtil.OnActivityResultListener()
        {
            @Override
            public void onActivityResult(int resultCode, Intent data)
            {
                if (resultCode == Activity.RESULT_OK && data != null)
                {
                    // 相册选择
                    Uri uri = data.getData();
                    // 选择返回
                    if (uri != null)
                    {
                        try
                        {
                            // 得到全路径
                            String path = CameraUriUtil.getUriPath(activity, uri);
                            if (!config.isCompress())
                            {
                                onPathCallback.onPath(path);
                            }
                            else
                            {
                                String newPath = null;
                                if (config.getSavePath() != null)
                                {
                                    newPath = config.getSavePath();
                                }
                                else
                                {
                                    newPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                                            .getPath() + "/LoePhoto/" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.CHINA).format(System.currentTimeMillis()) + ".jpg";
                                }
                                CameraImgUtil.compressSize(path, newPath, config.getMaxWidth(), config.getMaxHeight(), config.getMaxSize());
                                onPathCallback.onPath(newPath);
                            }
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                            Toast.makeText(activity, "保存出错", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    public static void takePhoto(FragmentActivity activity, final OnPathCallback onPathCallback)
    {
        takePhoto(activity, null, onPathCallback);
    }

    public static void takePhoto(FragmentActivity activity, PhotoConfig config, final OnPathCallback onPathCallback)
    {
        Intent intent = new Intent(activity, TakePhotoActivity.class);
        intent.putExtra(PhotoConfig.KEY, config);
        CameraResultUtil.startResult(activity, intent, new CameraResultUtil.OnActivityResultListener()
        {
            @Override
            public void onActivityResult(int resultCode, Intent data)
            {
                if (resultCode == Activity.RESULT_OK && data != null)
                {
                    String path = data.getStringExtra("path");
                    onPathCallback.onPath(path);
                }
            }
        });
        if (onDestroyAnimate != null)
        {
            onDestroyAnimate.onEnter(activity);
        }
    }

    public static void takeVideoAlbum(final FragmentActivity activity, final OnPathCallback onPathCallback)
    {
        takeVideoAlbum(activity, new VideoConfig(), onPathCallback);
    }

    public static void takeVideoAlbum(final FragmentActivity activity, VideoConfig config, final OnPathCallback onPathCallback)
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        CameraResultUtil.startResult(activity, intent, new CameraResultUtil.OnActivityResultListener()
        {
            @Override
            public void onActivityResult(int resultCode, Intent data)
            {
                if (resultCode == Activity.RESULT_OK && data != null)
                {
                    try
                    {
                        // 相册选择
                        Uri uri = data.getData();
                        // 选择返回
                        if (uri != null)
                        {
                            // 得到全路径
                            String path = CameraUriUtil.getUriPath(activity, uri);
                            onPathCallback.onPath(path);
                        }
                    } catch (Exception e)
                    {
                    }
                }
            }
        });
    }

    public static void takeVideo(FragmentActivity activity, final OnPathCallback onPathCallback)
    {
        takeVideo(activity, null, onPathCallback);
    }

    public static void takeVideo(FragmentActivity activity, VideoConfig config, final OnPathCallback onPathCallback)
    {
        Intent intent = new Intent(activity, TakeVideoActivity.class);
        intent.putExtra(VideoConfig.KEY, config);
        CameraResultUtil.startResult(activity, intent, new CameraResultUtil.OnActivityResultListener()
        {
            @Override
            public void onActivityResult(int resultCode, Intent data)
            {
                if (resultCode == Activity.RESULT_OK && data != null)
                {
                    String path = data.getStringExtra("path");
                    onPathCallback.onPath(path);
                }
            }
        });
        if (onDestroyAnimate != null)
        {
            onDestroyAnimate.onEnter(activity);
        }
    }

    public interface OnPathCallback
    {
        void onPath(String path);
    }

    static onDestroyAnimate onDestroyAnimate = new onDestroyAnimate()
    {
        @Override
        public void onEnter(Activity activity)
        {
            activity.overridePendingTransition(R.anim.camera_scale_in, R.anim.camera_on);
        }

        @Override
        public void onOut(Activity activity)
        {
            activity.overridePendingTransition(R.anim.camera_on, R.anim.camera_scale_out);
        }
    };

    public static void setOnDestroyAnimate(LoeCamera.onDestroyAnimate onDestroyAnimate)
    {
        LoeCamera.onDestroyAnimate = onDestroyAnimate;
    }

    public interface onDestroyAnimate
    {
        void onEnter(Activity activity);

        void onOut(Activity activity);
    }

    public static boolean clearPhoto()
    {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/LoePhoto/";
        return CameraImgUtil.delete(new File(path));
    }

    public static boolean clearVideo()
    {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/LoeVideo/";
        return CameraImgUtil.delete(new File(path));
    }


}
