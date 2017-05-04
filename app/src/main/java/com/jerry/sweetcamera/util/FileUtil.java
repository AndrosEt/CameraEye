package com.jerry.sweetcamera.util;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.FileChannel;

public class FileUtil {
    public static File updateDir = null;
    public static File updateFile = null;

    public static final String FOLDER = "SweetCamera";

    /***
     * 创建文件
     */
    public static void createFile(String name) {
        if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment
                .getExternalStorageState())) {
            updateDir = new File(Environment.getExternalStorageDirectory()
                    + "/" + FOLDER);
            updateFile = new File(updateDir + "/" + name + ".apk");

            if (!updateDir.exists()) {
                updateDir.mkdirs();
            }
            if (!updateFile.exists()) {
                try {
                    updateFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public static void initFolder() {
        File imgFile = new File(getCameraImageFolder());
        if (!imgFile.exists() || imgFile.isFile()) {
            imgFile.mkdirs();
        }
    }

    public static String getAppFoler() {
        return Environment.getExternalStorageDirectory()
                + "/" + FOLDER;
    }

    /**
     * 获取拍照照片保存的父目录
     *
     * @return
     */
    public static String getCameraImageFolder() {
        return getAppFoler() + "/" + "cameraImg";
    }

    /**
     * 根据时间获取生成的相机照片路径
     *
     * @return
     */
    public static String getCameraImgPath() {
        return getCameraImageFolder() + "/" + System.currentTimeMillis() + ".jpg";
    }

    /**
     * 根据路径获取父目录
     *
     * @param path
     */
    public static String getFolder(String path) {
        return path.substring(0, path.lastIndexOf("/"));
    }

    /**
     * 根据路径获取文件名
     *
     * @param path
     * @return
     */
    public static String getFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    /*
     * 递归删除目录
     */
    public static void deleteDirRecursive(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (f.isFile()) {
                f.delete();
            } else {
                deleteDirRecursive(f);
            }
        }
        dir.delete();
    }

    /**
     * 递归删除当前目录下的空目录
     *
     * @param dir
     */
    public static void deleteEmptyDirsRecursive(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        if (files.length == 0) {
            dir.delete();
        } else {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteEmptyDirsRecursive(f);
                }
            }

            //子目录处理完，再处理本目录
            files = dir.listFiles();
            if (files != null && files.length == 0) {
                dir.delete();
            }
        }
    }

    /**
     * 检测SD卡是否存在
     */
    public static boolean checkSDcard() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }

    /**
     * 将文件保存到本地
     */
    public static void saveFileCache(byte[] fileData,
                                     String folderPath, String fileName) {
        File folder = new File(folderPath);
        folder.mkdirs();
        File file = new File(folderPath, fileName);
        ByteArrayInputStream is = new ByteArrayInputStream(fileData);
        OutputStream os = null;
        if (!file.exists()) {
            try {
                file.createNewFile();
                os = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len = 0;
                while (-1 != (len = is.read(buffer))) {
                    os.write(buffer, 0, len);
                }
                os.flush();
            } catch (Exception e) {
                Log.e(FileUtil.class.getClass()
                        .getName(), e.getMessage());
            } finally {
                closeIO(is, os);
            }
        }
    }

    /**
     * 从指定文件夹获取文件
     *
     * @return 如果文件不存在则创建, 如果如果无法创建文件或文件名为空则返回null
     */
    public static File getSaveFile(String folderPath, String fileNmae) {
        File file = new File(getSavePath(folderPath) + File.separator
                + fileNmae);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 获取SD卡下指定文件夹的绝对路径
     *
     * @return 返回SD卡下的指定文件夹的绝对路径
     */
    public static String getSavePath(String folderName) {
        return getSaveFolder(folderName).getAbsolutePath();
    }

    /**
     * 获取文件夹对象
     *
     * @return 返回SD卡下的指定文件夹对象，若文件夹不存在则创建
     */
    public static File getSaveFolder(String folderName) {
        File file = new File(Environment
                .getExternalStorageDirectory().getAbsoluteFile()
                + File.separator + folderName + File.separator);
        file.mkdirs();
        return file;
    }

    /**
     * 输入流转byte[]<br>
     * <p/>
     * <b>注意</b> 你必须手动关闭参数inStream
     */
    public static final byte[] input2byte(InputStream inStream) {
        if (inStream == null) {
            return null;
        }
        byte[] in2b = null;
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        try {
            while ((rc = inStream.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            in2b = swapStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(swapStream);
        }
        return in2b;
    }


    /**
     * 复制文件
     *
     * @param from
     * @param to
     */
    public static void copyFile(File from, File to) {
        if (null == from || !from.exists()) {
            return;
        }
        if (null == to) {
            return;
        }
        FileInputStream is = null;
        FileOutputStream os = null;
        try {
            is = new FileInputStream(from);
            if (!to.exists()) {
                to.createNewFile();
            }
            os = new FileOutputStream(to);
            copyFileFast(is, os);
        } catch (Exception e) {
            Log.e(FileUtil.class.getClass()
                    .getName(), e.getMessage());
        } finally {
            closeIO(is, os);
        }
    }

    /**
     * 快速复制文件（采用nio操作）
     *
     * @param is 数据来源
     * @param os 数据目标
     * @throws IOException
     */
    public static void copyFileFast(FileInputStream is,
                                    FileOutputStream os) throws IOException {
        FileChannel in = is.getChannel();
        FileChannel out = os.getChannel();
        in.transferTo(0, in.size(), out);
    }

    /**
     * 关闭流
     *
     * @param closeables
     */
    public static void closeIO(Closeable... closeables) {
        if (null == closeables || closeables.length <= 0) {
            return;
        }
        for (Closeable cb : closeables) {
            try {
                if (null == cb) {
                    continue;
                }
                cb.close();
            } catch (IOException e) {
                Log.e(FileUtil.class.getClass()
                        .getName(), e.getMessage());
            }
        }
    }

    /**
     * 关闭流
     *
     * @param closeables
     */
    public static void closeSocket(Socket... socket) {
        if (null == socket || socket.length <= 0) {
            return;
        }
        for (Socket st : socket) {
            try {
                if (null == st) {
                    continue;
                }
                st.close();
            } catch (IOException e) {
                Log.e(FileUtil.class.getClass()
                        .getName(), e.getMessage());
            }
        }
    }

    /**
     * 图片写入文件
     *
     * @param bitmap   图片
     * @param filePath 文件路径
     * @return 是否写入成功
     */
    public static boolean bitmapToFile(Bitmap bitmap, String filePath) {
        boolean isSuccess = false;
        if (bitmap == null) {
            return isSuccess;
        }
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(
                    filePath), 8 * 1024);
            isSuccess = bitmap.compress(CompressFormat.PNG, 70, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            closeIO(out);
        }
        return isSuccess;
    }

    /**
     * 从文件中读取文本
     *
     * @param filePath
     * @return
     */
    public static String readFile(String filePath) {
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
        } catch (Exception e) {
            Log.e(FileUtil.class.getClass()
                    .getName(), e.getMessage());
        }
        return inputStream2String(is);
    }

    /**
     * 从assets中读取文本
     *
     * @param name
     * @return
     */
    public static String readFileFromAssets(Context context,
                                            String name) {
        InputStream is = null;
        try {
            is = context.getResources().getAssets().open(name);
        } catch (Exception e) {
            Log.e(FileUtil.class.getClass()
                    .getName(), e.getMessage());
        }
        return inputStream2String(is);
    }

    /**
     * 输入流转字符串
     *
     * @param is
     * @return 一个流中的字符串
     */
    public static String inputStream2String(InputStream is) {
        if (null == is) {
            return null;
        }
        StringBuilder resultSb = null;
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(is));
            resultSb = new StringBuilder();
            String len;
            while (null != (len = br.readLine())) {
                resultSb.append(len);
            }
        } catch (Exception ex) {
        } finally {
            closeIO(is);
        }
        return null == resultSb ? null : resultSb.toString();
    }


}
