package com.jerry.sweetcamera.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;

import com.jerry.sweetcamera.R;
import com.jerry.sweetcamera.SweetApplication;


public class StringUtils {
    final static String TAG = "StringUtils";

    public static String truncateZero(String s) {
        try {
            double d = Double.parseDouble(s);
            if (d * 10 % 10 == 0) {
                return "" + ((int) d);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return s;
    }

    public static boolean isEmpty(String s) {
        return null == s || "".equals(s);
    }

    public static boolean isNotEmpty(String s){
        return !isEmpty(s);
    }

    public static boolean equals(Object a, Object b){
        if(a == null){
            return false;
        }
        return a.equals(b);
    }

    /**
     * 将文本做成bitmap
     *
     * @param text    传入的文本
     * @param size    文本字体大小
     * @param color   文本字体颜色
     * @param font    文本字体类型
     * @param bgColor 文本背景颜色
     * @return 返回图片
     */
//    public static Bitmap stringToBitmap(String text, int size, int color, Typeface font, int bgColor) {
//        return stringToBitmap(text, size, color, font, bgColor, 10, 5);
//    }

    /**
     * 将文本做成bitmap
     *
     * @param text    传入的文本
     * @param size    文本字体大小
     * @param color   文本字体颜色
     * @param font    文本字体类型
     * @param bgColor 文本背景颜色
     * @return
     */
    public static Bitmap stringToBitmap(String text, int size, int color, Typeface font, int bgColor) {
        Log.d(TAG, "Size:" + size);

        TextPaint p = new TextPaint();
        p.setColor(color);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(font);
        p.setAntiAlias(true);//去除锯齿
        p.setFilterBitmap(true);//对位图进行滤波处理
        p.setTextSize(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX, size,
                SweetApplication.CONTEXT.getResources().getDisplayMetrics()));

        float space =  SweetApplication.CONTEXT.getResources().getDimensionPixelSize(R.dimen.px_2);  //背景padding

        int textHeight = (int) getTextHeight(p);    //字的高度
        int pixLen = (int) (getFontHeight(p) + 2* space);//正方形边长

        int offsetY =   SweetApplication.CONTEXT.getResources().getDimensionPixelSize(R.dimen.px_2);    //y方向上的偏移  手动调整
        float width = Math.max(getFontlength(p, text)+space*2,pixLen);    //宽度
        float height = textHeight + getLineSpaceHeight(); //高度

        int centerY = (int) getCenterY(p);  //汉字的中心 实际情况可能有点差异
        int paddingLeft = (int) (getFontlength(p, text)/(text.length())/2 - pixLen/2);  //汉字最左边的偏移

        Log.d(TAG, "===========width:" + width + "     height:" + height);
        //图象大小要根据文字大小算下,以和文本长度对应
        Bitmap bmp = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        Canvas canvasTemp = new Canvas(bmp);

        Paint bgPaint = new Paint();
        bgPaint.setColor(bgColor);
        canvasTemp.drawRect(paddingLeft,centerY - pixLen/2 - offsetY ,width-paddingLeft,centerY+pixLen/2-offsetY,bgPaint);  //绘制背景

        canvasTemp.drawText(text, width/2,getDistanceTopToBase(p)-offsetY, p);  //绘制汉字
        p.setColor(Color.WHITE);

        return bmp;
    }

    /**
     * @return 返回指定笔和指定字符串的长度
     */
    public static float getFontlength(Paint paint, String str) {
        return paint.measureText(str);
    }

    /**
     * @return 返回指定笔的文字高度
     */
    public static float getFontHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (float) Math.ceil(fm.descent - fm.ascent);
    }

    /**
     * @return 返回指定笔离文字顶部的基准距离
     */
    public static float getFontLeading(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.leading - fm.ascent;
    }

    /**
     * 获取top到baseline的距离
     *
     * @param paint
     * @return
     */
    public static float getDistanceTopToBase(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return -fm.top;
    }

    /**
     * 获取汉字的高度  从top到bottom
     * @param paint
     * @return
     */
    public static float getTextHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.bottom - fm.top;
    }

    /**
     * 根据xml中的TextView的行间距调整
     * @return
     */
    public static float getLineSpaceHeight(){
        return   SweetApplication.CONTEXT.getResources().getDimensionPixelSize(R.dimen.px_4);
    }

    /**
     * 获取汉字的中心Y
     * @param paint
     * @return
     */
    public static float getCenterY(Paint paint){
        int offset =  SweetApplication.CONTEXT.getResources().getDimensionPixelSize(R.dimen.px_2);
        return getTextHeight(paint)/2 + offset;
    }

    /**
     * 截掉小数点后面的0
     * @param d
     * @return
     */
    public static String getFloatString(double d){
//        int n = Integer.parseInt(String.format("%.0f", d));
//        double d1 = Float.parseFloat(String.format("%.1f", d));
//        double d2 = Float.parseFloat(String.format("%.2f", d));
//        Log.d(TAG,"======================getFloatString:" + " =d:" + d  + "=n:" + n + "=d1:" + d1 + "= d2:" + d2);
//        if(d2 - d1 > 0.009){
//            return  String.format("%.2f", d);
//        } else if(d1 - n > 0.09){
//            return  String.format("%.1f", d);
//        } else {
//            return  String.valueOf(n);
//        }

//        DecimalFormat df1   = new DecimalFormat("######0.00");

        String str = String.format("%.2f", d);
        if(str.endsWith(".00")){
//            return str.substring(0,str.length() - 3);
            return String.format("%.0f", d);
        } else if(str.contains(".") && str.endsWith("0")){
            return String.format("%.1f", d);
        } else {
            return str;
        }
    }
}
