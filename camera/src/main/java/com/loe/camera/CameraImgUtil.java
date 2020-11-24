package com.loe.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CameraImgUtil
{
    /**
     * 图片按比例大小压缩方法（根据路径获取图片并压缩）
     */
    public static File compressSize(String path, String newPath, int maxW, int maxH, int maxSize)
    {
        /**
         * 获取图片的旋转角度，有些系统把拍照的图片旋转了，有的没有旋转
         */
        int degree = readPictureDegree(path);
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w >= h && w > maxW)
        {//如果宽度大的话根据宽度固定大小缩放
            be = newOpts.outWidth / maxW;
        } else
        {
            if (w < h && h > maxH)
            {//如果高度高的话根据宽度固定大小缩放
                be = newOpts.outHeight / maxH;
            }
        }
        if (be <= 0)
        {
            be = 1;
        }
        newOpts.inSampleSize = be;//设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(path, newOpts);
        // 旋转图片
        bitmap = rotateImageView(degree, bitmap);
        return compressQuality(bitmap, newPath, maxSize);//压缩好比例大小后再进行质量压缩
    }

    /**
     * 质量压缩方法
     */
    private static File compressQuality(Bitmap bitmap, String newPath, int maxSize)
    {
        /*if (ext.equals("png"))
        {
            max *= 2;
        }*/
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int quality = 100;
        //质量压缩方法，这里quality = 100表示不压缩，把压缩后的数据存放到os中
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os);
        //循环判断如果压缩后图片是否大于100kb,大于继续压缩
        while (os.toByteArray().length > maxSize)
        {
            //重置os即清os
            os.reset();
            //这里压缩options%，把压缩后的数据存放到os中
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os);
            //每次都减少10
            quality -= 10;
        }
        return saveFile(newPath, os.toByteArray());
    }

    /**
     * 读取图片旋转的角度
     */
    public static int readPictureDegree(String path)
    {
        int degree = 0;
        try
        {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation)
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转图片
     */
    public static Bitmap rotateImageView(int angle, Bitmap bitmap)
    {
        if (angle != 0)
        {
            //旋转图片 动作
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            System.out.println("angle2=" + angle);
            // 创建新的图片
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap
                    .getHeight(), matrix, true);
            return resizedBitmap;
        }
        return bitmap;
    }

    /**
     * 保存文件
     */
    public static File saveFile(String path, byte[] data)
    {
        File file = null;
        FileOutputStream fos = null;
        try
        {
            file = new File(path);
            // 如果文件存在则删除
            if (file.exists())
            {
                file.delete();
            }
            // 如果文件夹路径不存在，则创建路径
            if (!file.getParentFile().exists())
            {
                file.getParentFile().mkdirs();
            }
            fos = new FileOutputStream(file, true);
            fos.write(data);
            fos.close();
        } catch (Exception e)
        {

            if (fos != null)
            {
                try
                {
                    fos.close();
                } catch (Exception e0)
                {
                }
            }
        }
        return file;
    }

    /**
     * 删除文件（文件夹）
     */
    public static boolean delete(File file)
    {
        if(!file.exists())
        {
            return true;
        }
        if (file.isFile())
        {
            return file.delete();
        }
        else
        {
            if (file.isDirectory())
            {
                File[] childFiles = file.listFiles();
                if (childFiles == null || childFiles.length == 0)
                {
                    return file.delete();
                }

                for (int i = 0; i < childFiles.length; i++)
                {
                    delete(childFiles[i]);
                }
                return file.delete();
            }
        }
        return false;
    }

    public static String getPhotoPath()
    {
        return getDCIM() + "LoePhoto/";
    }

    public static String getPhotoTemp()
    {
        return getPhotoPath() + "temp.jpg";
    }

    public static String getVideoPath()
    {
        return getDCIM() + "LoeVideo/";
    }

    public static String getVideoTemp()
    {
        return getVideoPath() + "temp.mp4";
    }

    public static String getDCIM()
    {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/";
    }

    public static String getDate()
    {
        return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.CHINA).format(System.currentTimeMillis());
    }
}