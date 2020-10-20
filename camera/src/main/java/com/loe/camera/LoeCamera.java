package com.loe.camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;

import java.io.File;

import androidx.fragment.app.FragmentActivity;

public class LoeCamera
{
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
