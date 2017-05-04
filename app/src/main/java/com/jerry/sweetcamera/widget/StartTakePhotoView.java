package com.jerry.sweetcamera.widget;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


import com.jerry.sweetcamera.CameraManager;
import com.jerry.sweetcamera.R;
import com.jerry.sweetcamera.util.BitmapUtils;
import com.jerry.sweetcamera.util.FastBlur;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jp.co.cyberagent.android.gpuimage.GPUImageNativeLibrary;

/**
 * 开始拍摄按钮
 *
 * @author jerry
 * @date 2015-09-06
 */
public class StartTakePhotoView extends SurfaceView implements SurfaceHolder.Callback,
        PreviewCallback {
    public static final String TAG = "StartTakePhotoView";

    public static final int SURF_TEXTURE_NAME = 10;
    public static final int ONE_FRAME_DURATION = 200;  //绘制一帧最大时长

    public static final int TEXTSIZE = 22; //绘制文本字体大小

    private Camera mCamera;
    private CameraManager mCameraManager;
    private Context mContext;

    private Paint mPaint;
    private SurfaceHolder holder;
    private SurfaceTexture mSurfaceTexture;


    private int srcResId;

    private boolean mCameraExist;
    private int ch, cw;

    BlockingQueue<Runnable> workQueue;
    ThreadPoolExecutor pool;

    public StartTakePhotoView(Context context) {
        this(context, null);
    }

    public StartTakePhotoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StartTakePhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.startTakePhotoView);
        srcResId = a.getResourceId(R.styleable.startTakePhotoView_src, -1);
        a.recycle();

        mContext = context;
        mCameraManager = CameraManager.getInstance(context);

        mPaint = new Paint();
//        isDrawing = new AtomicBoolean();
        holder = getHolder();
        getHolder().addCallback(this);
        setBackgroundColor(ComponentCallbacks2.TRIM_MEMORY_BACKGROUND);

        setWillNotDraw(false);

        if (Build.VERSION.SDK_INT >= 11) {
            mSurfaceTexture = new SurfaceTexture(SURF_TEXTURE_NAME);
        }

//        Log.i(TAG,"main Thread pid:"+Thread.currentThread().getId());

        this.workQueue = new LinkedBlockingQueue<Runnable>();
        this.pool = new ThreadPoolExecutor(1, 1, 600, TimeUnit.SECONDS, workQueue);
    }

    boolean initCamera() {
        try {
            mCamera = mCameraManager.openCameraFacing(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        mCameraManager.setDispaly(mCamera);
        mCamera.setPreviewCallback(this);
        mCameraManager.setUpPreviewSizeMin(mCamera);

        cw = mCamera.getParameters().getPreviewSize().width;
        ch = mCamera.getParameters().getPreviewSize().height;

        if (Build.VERSION.SDK_INT >= 11) {
            try {
                mCamera.setPreviewTexture(mSurfaceTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewFpsRange(20, 25);
        try {
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mCameraExist = true;
//        isDrawing.set(false);
        mCameraManager.setStartTakePhotoCamera(mCamera);
        return true;
    }

    //配合生命周期使用
    public void onStart() {
        if (holder != null && !mCameraExist) {
            surfaceCreated(holder);
        }
    }

    public void onStop() {
        releaseCamera();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated:" + mCamera);

//        mVector.clear();
        mCameraManager.releaseActivityCamera();

        initCamera();

        if (mCamera != null) {
            try {
                mCamera.startPreview();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera == null) {
            return;
        }

        Log.i(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        releaseCamera();

        //释放资源
        if(holder!=null){
            holder.getSurface().release();
        }


    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.i(TAG, "onPreviewFrame Thread:" + Thread.currentThread().getName() + " isDrawing:" + (workQueue.size() != 0));

        if (workQueue.size() == 0) {
            DrawCallBack drawCallBack = new DrawCallBack(data);
            Future<Boolean> future = pool.submit(drawCallBack);

            try {
                future.get(ONE_FRAME_DURATION, TimeUnit.MILLISECONDS); //取得结果，同时设置超时执行时间为5秒。同样可以用future.get()，不设置执行超时时间取得结果
            } catch (InterruptedException e) {
                future.cancel(true);
            } catch (ExecutionException e) {
                future.cancel(true);
            } catch (TimeoutException e) {
                future.cancel(true);
            }
        }
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCameraManager.releaseCamera(mCamera);
            mCamera = null;
            mCameraExist = false;
        }
    }

    public Bitmap scaleImage(Bitmap bitmap, float sx, float sy) {
        Matrix m = new Matrix();
        m.postScale(sx, sy);
        Bitmap outBitmap = null;
        try {
            outBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            bitmap.recycle();
        }
        return outBitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (srcResId != -1) {
            int centerX = canvas.getWidth() / 2;
            int centerY = canvas.getHeight() / 2;

            int topOfBitmap = getResources().getDimensionPixelSize(R.dimen.px_100);

            Bitmap srcBitmap = BitmapFactory.decodeResource(getResources(), srcResId);
            canvas.drawBitmap(srcBitmap, centerX - srcBitmap.getWidth() / 2, topOfBitmap, mPaint);

            recycle(srcBitmap);

            Paint countPaint = new Paint(Paint.ANTI_ALIAS_FLAG
                    | Paint.DEV_KERN_TEXT_FLAG);
            countPaint.setTextSize(TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.px_22), getResources().getDisplayMetrics()));
            countPaint.setTextAlign(Paint.Align.CENTER);
            countPaint.setColor(Color.WHITE);
            Rect textBounds = new Rect();
            String numberStr = "拍摄照片";
            countPaint.getTextBounds(numberStr, 0, numberStr.length(), textBounds);//get text bounds, that can get the text width and height
            int textHeight = textBounds.bottom - textBounds.top;

            Paint.FontMetrics fontMetrics =countPaint.getFontMetrics();
            int disTopToBaseline = (int) (fontMetrics.bottom - fontMetrics.descent);  //从top到baseline的距离

            canvas.drawText(numberStr, centerX, topOfBitmap + srcBitmap.getHeight() + getResources().getDimensionPixelSize(R.dimen.px_12) + textHeight / 2 + disTopToBaseline,
                    countPaint);
        }
    }

    private void draw(Bitmap bitmap) {
        float scaleUp = 3.5f; //缩放比例
        float scaleDown = 1 / scaleUp;

        long lastTime = System.currentTimeMillis();
        bitmap = BitmapUtils.scale(bitmap, scaleDown);
        bitmap = FastBlur.doBlur(bitmap, 8, true);
        bitmap = BitmapUtils.scale(bitmap, scaleUp);

        Log.i(TAG, "blurCommon time:" + (System.currentTimeMillis() - lastTime));

        Canvas canvas = holder.lockCanvas();  // 获取画布
        if (canvas == null) {
            return;
        }

        int centerX = canvas.getWidth() / 2;
        int centerY = canvas.getHeight() / 2;

        canvas.drawRGB(255, 255, 255);

        //开始绘制
        canvas.save();
        canvas.rotate(90, centerX, centerY);
        canvas.drawBitmap(bitmap, 0, 0, mPaint);
        canvas.restore();

        holder.unlockCanvasAndPost(canvas);  // 解锁画布，提交图像
        recycle(bitmap);
    }

    private void recycle(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    private class DrawCallBack implements Callable<Boolean> {

        private byte data[];

        public DrawCallBack(byte[] data) {
            this.data = data;
        }

        @Override
        public Boolean call() throws Exception {
            int width = getMeasuredWidth();
            int height = getMeasuredHeight();


            int[] rgb = new int[cw * ch];

            long lastTime = System.currentTimeMillis();

            GPUImageNativeLibrary.YUVtoARBG(data, cw, ch,
                    rgb);
            Log.i(TAG, "decodeYUV420SP time:" + (System.currentTimeMillis() - lastTime));

            Bitmap bitmap = Bitmap.createBitmap(rgb, cw, ch, Bitmap.Config.ARGB_8888);
            //图片切割成正方形
            bitmap = BitmapUtils.cropImage(bitmap);
            //图片缩放
            bitmap = scaleImage(bitmap, (float) width / bitmap.getWidth(), (float) height / bitmap.getHeight());

            if (bitmap != null) {
                draw(bitmap);
            } else {
                return false;
            }

            return true;
        }
    }
}






