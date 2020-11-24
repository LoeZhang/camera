package com.loe.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
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
                                    newPath = CameraImgUtil.getPhotoPath() + CameraImgUtil.getDate() + ".jpg";
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

    public static void cropImage(final FragmentActivity activity, String path, final OnPathCallback onPathCallback)
    {
        cropImage(activity, path, 1, 1, 400, 400, onPathCallback);
    }

    public static void cropImage(final FragmentActivity activity, String path, int aspectX, int aspectY, int outputX, int outputY, final OnPathCallback onPathCallback)
    {
        File file = new File(path);
        if (!file.exists())
        {
            return;
        }

        // 这是比较流氓的方法，绕过7.0的文件权限检查
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        Uri uri = CameraUriUtil.fileToUri(activity, file);

        String newPath = CameraImgUtil.getPhotoPath() + CameraImgUtil.getDate() + ".jpg";
        File newFile = new File(newPath);
        if (!newFile.getParentFile().exists())
        {
            newFile.getParentFile().mkdirs();
        }
        Uri newUri = CameraUriUtil.fileToUri(activity, newFile);

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, newUri);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        CameraResultUtil.startResult(activity, intent, new CameraResultUtil.OnActivityResultListener()
        {
            @Override
            public void onActivityResult(int resultCode, Intent data)
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
                clearPhotoTemp();
            }
        });
        if (onDestroyAnimate != null)
        {
            onDestroyAnimate.onEnter(activity);
        }
    }

    public static void takeVideoAlbum(final FragmentActivity activity, final OnPathCallback onPathCallback)
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
        String path = CameraImgUtil.getPhotoPath();
        return CameraImgUtil.delete(new File(path));
    }

    public static void clearPhotoTemp()
    {
        File tempFile = new File(CameraImgUtil.getPhotoTemp());
        if (tempFile.exists())
        {
            tempFile.delete();
        }
    }

    public static boolean clearVideo()
    {
        String path = CameraImgUtil.getVideoPath();
        return CameraImgUtil.delete(new File(path));
    }

    public static void takeFile(final FragmentActivity activity, final OnPathCallback onPathCallback)
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
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

    public static void open(Context context, String pathOrUrl)
    {
        if (pathOrUrl.toLowerCase().startsWith("http"))
        {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(pathOrUrl));
            context.startActivity(intent);
        }
        else
        {
            openFile(context, new File(pathOrUrl));
        }
    }

    public static void openFile(Context context, File file)
    {
        Intent intent = new Intent();
        // 这是比较流氓的方法，绕过7.0的文件权限检查
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        //        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//设置标记
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setAction(Intent.ACTION_VIEW);//动作，查看
        intent.setDataAndType(Uri.fromFile(file), getMIMEType(file));//设置类型
        context.startActivity(intent);
    }

    private static final String[][] MIME_MapTable = {
            //{后缀名，    MIME类型}
            {".3gp", "video/3gpp"}, {".apk", "application/vnd.android.package-archive"}, {".asf", "video/x-ms-asf"}, {".avi", "video/x-msvideo"}, {".bin", "application/octet-stream"}, {".bmp",
            "image/bmp"}, {".c", "text/plain"}, {".class", "application/octet-stream"}, {".conf", "text/plain"}, {".cpp", "text/plain"}, {".doc", "application/msword"}, {".docx", "application" +
            "/msword"}, {".exe", "application/octet-stream"}, {".gif", "image/gif"}, {".gtar", "application/x-gtar"}, {".gz", "application/x-gzip"}, {".h", "text/plain"}, {".htm", "text/html"}, {
                ".html", "text/html"}, {".jar", "application/java-archive"}, {".java", "text/plain"}, {".jpeg", "image/jpeg"}, {".JPEG", "image/jpeg"}, {".jpg", "image/jpeg"}, {".js",
            "application" + "/x-javascript"}, {".log", "text/plain"}, {".m3u", "audio/x-mpegurl"}, {".m4a", "audio/mp4a-latm"}, {".m4b", "audio/mp4a-latm"}, {".m4p", "audio/mp4a-latm"}, {".m4u",
            "video/vnd" + ".mpegurl"}, {".m4v", "video/x-m4v"}, {".mov", "video/quicktime"}, {".mp2", "audio/x-mpeg"}, {".mp3", "audio/x-mpeg"}, {".mp4", "video/mp4"}, {".mpc", "application/vnd" +
            ".mpohun" + ".certificate"}, {".mpe", "video/mpeg"}, {".mpeg", "video/mpeg"}, {".mpg", "video/mpeg"}, {".mpg4", "video/mp4"}, {".mpga", "audio/mpeg"}, {".msg", "application/vnd" +
            ".ms-outlook"}, {".ogg", "audio/ogg"}, {".pdf", "application/pdf"}, {".png", "image/png"}, {".pps", "application/vnd.ms-powerpoint"}, {".ppt", "application/vnd.ms-powerpoint"}, {".pptx"
            , "application/vnd" + ".ms-powerpoint"}, {".prop", "text/plain"}, {".rar", "application/x-rar-compressed"}, {".rc", "text/plain"}, {".rmvb", "audio/x-pn-realaudio"}, {".rtf",
            "application/rtf"}, {".sh", "text/plain"}, {".tar", "application/x-tar"}, {".tgz", "application/x-compressed"}, {".txt", "text/plain"}, {".wav", "audio/x-wav"}, {".wma", "audio/x-ms-wma"
    }, {".wmv", "audio/x-ms-wmv"}, {".wps", "application/vnd.ms-works"},
            //{".xml",    "text/xml"},
            {".xml", "text/plain"}, {".z", "application/x-compress"}, {".zip", "application/zip"}, {"", "*/*"}};

    public static String getMIMEType(File file)
    {

        String type = "*/*";
        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0)
        {
            return type;
        }
        /* 获取文件的后缀名 */
        String fileType = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (fileType == null || "".equals(fileType))
        {
            return type;
        }
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (int i = 0; i < MIME_MapTable.length; i++)
        {
            if (fileType.equals(MIME_MapTable[i][0]))
            {
                type = MIME_MapTable[i][1];
            }
        }
        return type;
    }
}
