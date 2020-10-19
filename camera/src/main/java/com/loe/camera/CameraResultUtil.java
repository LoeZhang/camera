package com.loe.camera;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class CameraResultUtil
{
    private static final String TAG = "CameraResultUtil";

    public static void startResult(FragmentActivity activity, Intent intent, OnActivityResultListener listener)
    {
        FragmentManager manager = activity.getSupportFragmentManager();
        ActivityResultFragment fragment = (ActivityResultFragment) manager.findFragmentByTag(TAG);
        if (fragment == null)
        {
            fragment = new ActivityResultFragment();
            manager.beginTransaction()
                    .add(fragment, TAG)
                    .commitNowAllowingStateLoss();
        }
        fragment.listener = listener;
        fragment.startActivityForResult(intent, 14);
    }

    public static class ActivityResultFragment extends Fragment
    {
        public OnActivityResultListener listener;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode != 14) return;

            if (listener != null)
            {
                listener.onActivityResult(resultCode, data);
            }
        }
    }

    public interface OnActivityResultListener
    {
        void onActivityResult(int resultCode, Intent data);
    }
}