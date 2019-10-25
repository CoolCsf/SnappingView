package com.ccsf.snappingview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;

public class Text2BitmapUtils {
    /**
     * @param str         需要转换的文本
     * @param width       当前需要显示区域的宽度（自动适配文字大小）
     * @param maxLineSize 每行最大的字符数
     * @param minLineSize 每行最少的字符数（如设置3，实际只有两个字符的时候，会显示两个字符，但是字体的大小是适配3个字体的大小）
     * @param fontColor   字体颜色
     * @param backColor   图片的背景颜色
     * @return
     */
    public static Bitmap getBitmap(String str, int width, int maxLineSize, int minLineSize, int fontColor, int backColor) {
        if (width == 0 || maxLineSize == 0) {
            return null;
        }
        if (TextUtils.isEmpty(str)) {
            return null;
        }

        int size = str.length();//字体个数
        int fontSize = 0;//字体大小
        int line = 1;//字体行数
        int oneLineSize;//单行字数
        /**
         * 计算单行字数
         */
        if (size <= minLineSize) {
            oneLineSize = minLineSize;
        } else if (size < maxLineSize) {
            oneLineSize = size;
        } else {
            line = (size - 1) / maxLineSize + 1;
            oneLineSize = maxLineSize;
        }
        fontSize = width * 9 / 10 / oneLineSize;

        /**
         * 字体相关配置
         */
        Typeface font = Typeface.create("宋体", Typeface.BOLD);
        Paint p = new Paint();
        p.setColor(fontColor);
        p.setTypeface(font);
        p.setAntiAlias(true);//去除锯齿
        p.setFilterBitmap(true);//对位图进行滤波处理
        p.setTextSize(fontSize);

        /**
         * 先画背景
         */
        int height = (line) * fontSize + (fontSize / 3);
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(backColor);
        /**
         * 一行一行的画文字
         */
        for (int i = 0; i < line; i++) {
            int start = i * oneLineSize;
            int end = (i + 1) * oneLineSize;
            if (end >= size) {
                end = size;
            }
            int x;
            if (size < oneLineSize) {
                //字数小于最小的行字数，设置居中（如果不需要居中，直接用else部分即可）
                x = (width - (size * fontSize)) / 2;
            } else {
                x = (width - (oneLineSize * fontSize)) / 2;
            }
            int top = 0;
            int y = (i + 1) * fontSize + top;
            canvas.drawText(str, start, end, x, y, p);
        }
        return bmp;
    }

    public static Bitmap getBitmap(Activity activity, String text, int textColor, int textAlign, int textWidth) {
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(DisplayUtils.sp2px(activity, 16));
        textPaint.setAntiAlias(true);

        StaticLayout staticLayout = new StaticLayout(text, textPaint, textWidth, getTextAlign(textAlign), 1.0f, 0.0f, false);
        Bitmap bitmap = Bitmap.createBitmap(staticLayout.getWidth(), staticLayout.getHeight(), Bitmap.Config.ARGB_8888);
        Log.i("test3", "getBitmap: getWidth>>" + staticLayout.getWidth() + " getHeight>>" + staticLayout.getHeight() + " " + bitmap.getWidth() + " " + bitmap.getHeight());
        Canvas canvas = new Canvas(bitmap);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        staticLayout.draw(canvas);
        return bitmap;
    }

    @TargetApi(Build.VERSION_CODES.P)
    public static Layout.Alignment getTextAlign(int textAlign) {
        if (textAlign == Gravity.LEFT) {
            return Layout.Alignment.ALIGN_LEFT;
        } else if (textAlign == Gravity.CENTER) {
            return Layout.Alignment.ALIGN_CENTER;
        } else if (textAlign == Gravity.RIGHT) {
            return Layout.Alignment.ALIGN_RIGHT;
        }
        return Layout.Alignment.ALIGN_LEFT;
    }

}
