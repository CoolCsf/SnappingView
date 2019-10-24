package com.ccsf.snappingview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SnappingTextView extends View {

    private String drawableString;
    /**
     * 图片的最大缩放比例
     */
    public static final float MAX_SCALE = 10.0f;

    /**
     * 图片的最小缩放比例
     */
    public static final float MIN_SCALE = 0.5f;

    /**
     * 控制缩放，旋转图标所在四个点得位置
     */
    public static final int LEFT_TOP = 0;
    public static final int RIGHT_TOP = 1;
    public static final int RIGHT_BOTTOM = 2;
    public static final int LEFT_BOTTOM = 3;

    /**
     * 一些默认的常量
     */
    public static final int DEFAULT_EDIT_BITMAP_PADDING = 8;
    public static final int DEFAULT_FRAME_PADDING = 0;
    public static final int DEFAULT_FRAME_WIDTH = 2;
    public static final float DEFAULT_SCALE = 1.0f;
    public static final float DEFAULT_DEGREE = 0;
    //    public static final int DEFAULT_CONTROL_LOCATION = RIGHT_TOP;
//    public static final int DEFAULT_DELETE_LOCATION = LEFT_TOP;
    public static final int DEFAULT_CONTROL_LOCATION = RIGHT_BOTTOM;
    public static final int DEFAULT_DELETE_LOCATION = RIGHT_TOP;
    public static final boolean DEFAULT_EDITABLE = true;


    private int mContentWidth; // 内容的宽度，会随着拉伸变换
    private int mContentHeight; //内容的高度，会随着拉伸变换
    private int mInitContentWidth; // 初始内容的宽度，不会随着拉伸变换
    private int mInitContentHeight; //初始内容的高度，不会随着拉伸变换

    /**
     * SingleTouchView的中心点坐标，相对于其父类布局而言的
     */
    private PointF mCenterPoint = new PointF();

    /**
     * View的宽度和高度，随着图片的旋转而变化(不包括控制旋转，缩放图片的宽高)
     */
    private int mViewWidth, mViewHeight;

    /**
     * 图片的旋转角度
     */
    private float mDegree = DEFAULT_DEGREE;

    /**
     * 图片的缩放比例
     */
    private float mWidthScale = DEFAULT_SCALE;

    /**
     * 用于缩放，旋转，平移的矩阵
     */
    private Matrix matrix = new Matrix();

    /**
     * 距离父类布局的左间距
     */
    private int mViewPaddingLeft;

    /**
     * 距离父类布局的上间距
     */
    private int mViewPaddingTop;

    /**
     * 图片四个点坐标
     */
    private Point mLTPoint = new Point();
    private Point mRTPoint = new Point();
    private Point mRBPoint = new Point();
    private Point mLBPoint = new Point();
    private Point cp = new Point();
    /**
     * 用于缩放，旋转的控制点的坐标
     */
    private Point mControlPoint = new Point();
    /**
     * 用于缩放，旋转的控制点的坐标
     */
    private Point mDeletePoint = new Point();

    /**
     * 用于缩放，旋转的图标
     */
    private Drawable zoomDrawable;
    /**
     * 用于点击移除的图标
     */
    private Drawable rotateDrawable;

    /**
     * 缩放，旋转图标的宽和高
     */
    private int mDrawableWidth, mDrawableHeight;
    private int mDeleteDrawableWidth, mDeleteDrawableHeight;
    private int halfDrawableWidth, halfDrawableHeight;
    /**
     * 画外围框的Path
     */
    private Path mPath = new Path();

    /**
     * 画外围框的画笔
     */
    private Paint mPaint;

    /**
     * 初始状态
     */
    public static final int STATUS_INIT = 0;

    /**
     * 拖动状态
     */
    public static final int STATUS_DRAG = 1;

    /**
     * 放大状态
     */
    public static final int STATUS_ZOOM = 2;
    /**
     * 旋转状态
     */
    public static final int STATUS_ROTATE = 3;
    /**
     * 编辑
     */
    public static final int STATUS_EDIT = 5;
    /**
     * 删除
     */
    public static final int STATUS_DELETE = 4;
    /**
     * 时间戳
     */
    public static final int STATUS_TIMESTAMP = 6;
    public static final int STATUS_OUT = 7;
    /**
     * 当前所处的状态
     */
    private int mStatus = STATUS_INIT;

    /**
     * 外边框与图片之间的间距, 单位是dip
     */
    private int framePadding = DEFAULT_FRAME_PADDING;

    /**
     * 外边框颜色
     */
    private int frameColor = 0xffC53837;

    /**
     * 外边框线条粗细, 单位是 dip
     */
    private int frameWidth = DEFAULT_FRAME_WIDTH;
    /**
     * 是否处于可以缩放，平移，旋转状态
     */
    private boolean isEditable = DEFAULT_EDITABLE;


    private PointF mPreMovePointF = new PointF();
    private PointF mCurMovePointF = new PointF();
    private boolean isClick = true;
    /**
     * 控制图标所在的位置（比如左上，右上，左下，右下）
     */
    private int controlLocation = DEFAULT_CONTROL_LOCATION;

    private boolean isShowEditBitmap = true;
    private int editBitmapWidth = 0;
    private int editBitmapHeight = 0;
    private int leftEditBitmapOffset;
    private int topEditBitmapOffset;
    private List<Bitmap> tabBitmapList = new ArrayList<>();
    private IClickListener mClickListener = null;

    public SnappingTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SnappingTextView(Context context, String content, int tabNum) {
        this(context, null);
        this.content = content;
        textPaint = new TextPaint();
        textPaint.setColor(textColor);
        int defaultSize = 34;
        textPaint.setTextSize(defaultSize);
        textPaint.setAntiAlias(true);
        int contentWidth = (int) textPaint.measureText(content);
        staticLayout = new StaticLayout(content, textPaint, contentWidth, textAlign, 1.0f, 0.0f, false);
        initTab(tabNum);
        setInitContentSize(staticLayout.getWidth(), staticLayout.getHeight());
        init();
    }

    private void initTab(int tabNum) {
        switch (tabNum) {
            case 1:
                Bitmap deleteFilletBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.tab_delete_fillet);
                tabBitmapList.add(deleteFilletBitmap);
                break;
            case 2:
                Bitmap deleteBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.tab_delete);
                tabBitmapList.add(deleteBitmap);
                Bitmap timeBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.tab_time);
                tabBitmapList.add(timeBitmap);
                break;
            case 3:
                Bitmap dBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.tab_delete);
                tabBitmapList.add(dBitmap);
                Bitmap editBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.tab_edit);
                tabBitmapList.add(editBitmap);
                Bitmap tBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.tab_time);
                tabBitmapList.add(tBitmap);
                break;
        }
        if (!tabBitmapList.isEmpty()) {
            editBitmapWidth = tabBitmapList.get(0).getWidth();
            editBitmapHeight = tabBitmapList.get(0).getHeight();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed && mStatus == STATUS_INIT) {
            ViewGroup mViewGroup = (ViewGroup) getParent();
            if (null != mViewGroup) {
                int parentWidth = mViewGroup.getWidth();
                int parentHeight = mViewGroup.getHeight();
                mCenterPoint.set(parentWidth / 2, parentHeight / 2);
                mViewPaddingLeft = (int) (mCenterPoint.x - mViewWidth / 2);
                mViewPaddingTop = (int) (mCenterPoint.y - mViewHeight / 2);
            }
        }
    }

    public SnappingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(frameColor);
        mPaint.setStrokeWidth(frameWidth);
        mPaint.setStyle(Style.STROKE);
        if (zoomDrawable == null) {
            zoomDrawable = getContext().getResources().getDrawable(R.mipmap.icon_zoom);
        }
        if (rotateDrawable == null) {
            rotateDrawable = getContext().getResources().getDrawable(R.mipmap.icon_rotate);
        }
        mDrawableWidth = zoomDrawable.getIntrinsicWidth();
        mDrawableHeight = zoomDrawable.getIntrinsicHeight();
        halfDrawableWidth = mDrawableWidth / 2;
        halfDrawableHeight = mDrawableHeight / 2;
        if (rotateDrawable != null) {
            mDeleteDrawableHeight = rotateDrawable.getIntrinsicHeight();
            mDeleteDrawableWidth = rotateDrawable.getIntrinsicWidth();
        }
        mControlPoint = LocationToPoint(controlLocation);
        mDeletePoint = LocationToPoint(DEFAULT_DELETE_LOCATION);
        transform();
    }

    /**
     * 调整View的大小，位置
     */
    private void adjustLayout() {
        int newPaddingLeft = (int) (mCenterPoint.x - mViewWidth / 2);
        int newPaddingTop = (int) (mCenterPoint.y - mViewHeight / 2);

        if (mViewPaddingLeft != newPaddingLeft || mViewPaddingTop != newPaddingTop) {
            mViewPaddingLeft = newPaddingLeft;
            mViewPaddingTop = newPaddingTop;
            layout(newPaddingLeft, newPaddingTop, newPaddingLeft + mViewWidth, newPaddingTop + mViewHeight);
        }
    }

    private int getEditBitMapOffsetHeight() {
        return editBitmapHeight;
    }

    public Bitmap getSignatureViewBitmap() {
        isEditable = false;
        invalidate();
        setDrawingCacheEnabled(true);
        buildDrawingCache();  //启用DrawingCache并创建位图
        Bitmap bitmap = Bitmap.createBitmap(getDrawingCache()); //创建一个DrawingCache的拷贝，因为DrawingCache得到的位图在禁用后会被回收
        setDrawingCacheEnabled(false);  //禁用DrawingCahce否则会影响性能
        return bitmap;
    }

    private String content;
    private StaticLayout staticLayout;
    private TextPaint textPaint;
    private int textColor = Color.BLACK;
    private Layout.Alignment textAlign = Layout.Alignment.ALIGN_CENTER;
    private int textSize = 34;

    public void setTextColor(int color) {
        this.textColor = color;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextAlign(int textAlign) {
        if (textAlign == Gravity.LEFT) {
            this.textAlign = Layout.Alignment.ALIGN_LEFT;
        } else if (textAlign == Gravity.CENTER) {
            this.textAlign = Layout.Alignment.ALIGN_CENTER;
        } else if (textAlign == Gravity.RIGHT) {
            this.textAlign = Layout.Alignment.ALIGN_RIGHT;
        }
    }

    public Layout.Alignment getTextAlign() {
        return textAlign;
    }

//    public void setDrawText(String content) {
//        this.content = content;
//        textPaint = new TextPaint();
//        textPaint.setColor(textColor);
//        textPaint.setTextSize(defaultSize * textSize);
//        textPaint.setAntiAlias(true);
//
//        staticLayout = new StaticLayout(content, textPaint, contentWidth, textAlign, 1.0f, 0.0f, false);
//        contentWidth = staticLayout.getWidth() + 10;
//        contentHeight = staticLayout.getHeight() + 15;
//        minContentWidth = staticLayout.getWidth();
//        minContentHeight = staticLayout.getHeight();
//        Log.i("test3", "setDrawText: contentWidth>>" + contentWidth + " contentHeight>>" + contentHeight
//                + " minContentWidth>>" + minContentWidth + " minContentHeight>>" + minContentHeight);
//        transformDraw();
//    }


    public float getScale() {
        return mWidthScale;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (staticLayout != null) {
            int offsetTop = mViewHeight / 2 - (mContentHeight / 2) - mDrawableHeight + getEditBitMapOffsetHeight() - framePadding;
            int offsetLeft = (mViewWidth - mContentWidth - mDrawableWidth) / 2;
            canvas.save();
            //设置画该图片的起始点
            canvas.translate(mDrawableWidth / 2 + offsetLeft, mDrawableHeight / 2 + offsetTop);
            //绕着图片中心进行旋转
            canvas.rotate(mDegree, mContentWidth / 2, mContentHeight / 2);
            staticLayout.draw(canvas);
            canvas.restore();
        }
        //处于可编辑状态才画边框和控制图标
        if (isEditable && mRTPoint != null) {
            mPath.reset();
            mPath.moveTo(mLTPoint.x, mLTPoint.y);
            mPath.lineTo(mRTPoint.x, mRTPoint.y);
            mPath.lineTo(mRBPoint.x, mRBPoint.y);
            mPath.lineTo(mLBPoint.x, mLBPoint.y);
            mPath.close();
            canvas.drawPath(mPath, mPaint);
            leftEditBitmapOffset = (mViewWidth - getAllEditBitmapWidth()) / 2;
            topEditBitmapOffset = getMinValue(mLTPoint.y, mRTPoint.y, mLBPoint.y, mRBPoint.y) - getEditBitMapOffsetHeight() - framePadding - halfDrawableHeight;
            //画旋转, 缩放图标
            zoomDrawable.setBounds(mControlPoint.x - mDrawableWidth / 2,
                    mControlPoint.y - mDrawableHeight / 2, mControlPoint.x + mDrawableWidth
                            / 2, mControlPoint.y + mDrawableHeight / 2);
            zoomDrawable.draw(canvas);
            if (rotateDrawable != null) {
                rotateDrawable.setBounds(mDeletePoint.x - mDeleteDrawableWidth / 2, mDeletePoint.y - mDeleteDrawableHeight / 2,
                        mDeletePoint.x + mDeleteDrawableWidth / 2, mDeletePoint.y + mDeleteDrawableHeight / 2);
                rotateDrawable.draw(canvas);
            }
            if (isShowEditBitmap) {
                for (int i = 0; i < tabBitmapList.size(); i++) {
                    canvas.drawBitmap(tabBitmapList.get(i), getEditBitmapLeft(i), topEditBitmapOffset, null);
                }
            }
        }
    }

    private int getEditBitmapLeft(int index) {
        return leftEditBitmapOffset + (editBitmapWidth + DEFAULT_EDIT_BITMAP_PADDING) * index;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getViewWidth(), getViewHeight());
    }

    private void setContentWidth() {
        if (content != null && staticLayout != null) {
            mContentWidth = (int) (mInitContentWidth * mWidthScale);
        } else {
            throw new NullPointerException("内容或者bitmap不允许为空");
        }
    }

    private void setContentHeight() {
        if (content != null && staticLayout != null) {
            mContentHeight = staticLayout.getHeight();
        } else {
            throw new NullPointerException("内容或者bitmap不允许为空");
        }
    }

    /**
     * 设置Matrix, 强制刷新
     */
    private void transformDraw() {
        transform();
        invalidate();
        adjustLayout();
    }

    private void transform() {
        setContentWidth();
        textPaint.setTextSize(textSize);
        staticLayout = new StaticLayout(content, textPaint, mContentWidth, textAlign, 1.0f, 0.0f, false);
        setContentHeight();
        mViewWidth = getViewWidth();
        mViewHeight = getViewHeight();
        int offsetTop = mViewHeight / 2 - (mContentHeight / 2) - mDrawableHeight + getEditBitMapOffsetHeight() - framePadding;
        int offsetLeft = (mViewWidth - mContentWidth - mDrawableWidth) / 2;
        computeRect(offsetLeft, offsetTop, mContentWidth + offsetLeft, mContentHeight + offsetTop, mDegree);
    }

    private int getAllEditBitmapWidth() {
        return (editBitmapWidth * tabBitmapList.size()) + (DEFAULT_EDIT_BITMAP_PADDING * (tabBitmapList.size() - 1));
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getX() > leftEditBitmapOffset && event.getX() < getAllEditBitmapWidth() + leftEditBitmapOffset
                        && event.getY() > topEditBitmapOffset && event.getY() < topEditBitmapOffset + editBitmapHeight) {
                    isClick = false;
                    setStateWhenClickTab(event);
                } else {
                    mStatus = JudgeStatus(event.getX(), event.getY());
                    if (mStatus != STATUS_OUT || isPolygonContainsPoint(new Point((int) event.getX(), (int) event.getY()), mRTPoint, mRBPoint, mLBPoint, mLTPoint)) {
                        isEditable = true;
                        mPreMovePointF.set(event.getX() + mViewPaddingLeft, event.getY() + mViewPaddingTop);
                    } else {
                        return super.onTouchEvent(event);
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                isShowEditBitmap = true;
                if (mStatus == STATUS_DRAG || mStatus == STATUS_ZOOM || mStatus == STATUS_ROTATE) {
                    invalidate();
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) this.getLayoutParams();
                    lp.setMargins(mViewPaddingLeft, mViewPaddingTop, 0, 0);
                    lp.removeRule(RelativeLayout.CENTER_IN_PARENT);
                    setLayoutParams(lp);
                } else if (mClickListener != null) {
                    if (isClick) {
                        mClickListener.onClickView();
                    } else {
                        switch (mStatus) {
                            case STATUS_DELETE:
                                mClickListener.onClickDelete();
                                break;
                            case STATUS_EDIT:
                                mClickListener.onClickEdit();
                                break;
                            case STATUS_TIMESTAMP:
                                mClickListener.onClickTimestamp();
                                break;
                        }
                    }
                }
                isClick = true;
                mStatus = STATUS_INIT;
                break;
            case MotionEvent.ACTION_MOVE:
                mCurMovePointF.set(event.getX() + mViewPaddingLeft, event.getY() + mViewPaddingTop);
                if (mStatus == STATUS_OUT && distance4PointF(mPreMovePointF, mCurMovePointF) >= 10) {//移动超过10才算滑动
                    mStatus = STATUS_DRAG;
                }
                if (mStatus == STATUS_DRAG || mStatus == STATUS_ZOOM || mStatus == STATUS_ROTATE) {
                    isClick = false;
                    isShowEditBitmap = false;
                    if (mStatus == STATUS_ZOOM) {
                        actionTextZoom(event);
//                        actionBitmapZoom();
                    } else if (mStatus == STATUS_DRAG) {
                        actionDrag();
                    } else {
                        actionRotate();
                    }
                    mPreMovePointF.set(mCurMovePointF);
                }
                break;
        }
        return true;
    }

    private void setStateWhenClickTab(MotionEvent event) {
        // 点击了上面的编辑按钮那一排
        if (event.getX() > leftEditBitmapOffset && event.getX() <= leftEditBitmapOffset + editBitmapWidth) {
            //点击了第一个按钮
            mStatus = STATUS_DELETE;
        } else if (event.getX() > leftEditBitmapOffset + editBitmapWidth && event.getX() <= leftEditBitmapOffset + editBitmapWidth * 2 && tabBitmapList.size() >= 2) {
            //点击了第二个按钮
            if (tabBitmapList.size() == 2) {
                mStatus = STATUS_TIMESTAMP;
            } else if (tabBitmapList.size() == 3) {
                mStatus = STATUS_EDIT;
            }
        } else if (event.getX() > leftEditBitmapOffset + (editBitmapWidth * 2) && event.getX() <= leftEditBitmapOffset + (editBitmapWidth * 3) && tabBitmapList.size() >= 3) {
            //点击了第三个按钮
            if (tabBitmapList.size() == 3) {
                mStatus = STATUS_TIMESTAMP;
            }
        }
    }

    private void actionRotate() {
        double a = distance4PointF(mCenterPoint, mPreMovePointF);
        double b = distance4PointF(mPreMovePointF, mCurMovePointF);
        double c = distance4PointF(mCenterPoint, mCurMovePointF);
        double cosb = (a * a + c * c - b * b) / (2 * a * c);
        if (cosb >= 1) {
            cosb = 1f;
        }
        double radian = Math.acos(cosb);
        float newDegree = (float) radianToDegree(radian);

        //center -> proMove的向量， 我们使用PointF来实现
        PointF centerToProMove = new PointF((mPreMovePointF.x - mCenterPoint.x), (mPreMovePointF.y - mCenterPoint.y));

        //center -> curMove 的向量
        PointF centerToCurMove = new PointF((mCurMovePointF.x - mCenterPoint.x), (mCurMovePointF.y - mCenterPoint.y));

        //向量叉乘结果, 如果结果为负数， 表示为逆时针， 结果为正数表示顺时针
        float result = centerToProMove.x * centerToCurMove.y - centerToProMove.y * centerToCurMove.x;

        if (result < 0) {
            newDegree = -newDegree;
        }

        mDegree = mDegree + newDegree;
        transformDraw();
    }

    private void actionDrag() {
        // 修改中心点
        mCenterPoint.x += mCurMovePointF.x - mPreMovePointF.x;
        mCenterPoint.y += mCurMovePointF.y - mPreMovePointF.y;
        adjustLayout();
    }

    private void setInitContentSize(int width, int height) {
        mInitContentHeight = height;
        mInitContentWidth = width;
    }

    private void actionBitmapZoom() {
        float scale = 1f;

        int halfBitmapWidth = mInitContentWidth / 2;
        int halfBitmapHeight = mInitContentHeight / 2;
        //图片某个点到图片中心的距离
        float bitmapToCenterDistance = (float) Math.sqrt(halfBitmapWidth * halfBitmapWidth + halfBitmapHeight * halfBitmapHeight);

        //移动的点到图片中心的距离
        float moveToCenterDistance = distance4PointF(mCenterPoint, mCurMovePointF);

        //计算缩放比例
        scale = moveToCenterDistance / bitmapToCenterDistance;


        //缩放比例的界限判断
        if (scale <= MIN_SCALE) {
            scale = MIN_SCALE;
        } else if (scale >= MAX_SCALE) {
            scale = MAX_SCALE;
        }
        textSize = (int) (34 * scale);
//        mWidthScale = scale;
        transformDraw();
    }

    private void actionTextZoom(MotionEvent event) {
        PointF movePoint = new PointF(event.getX(), event.getY());

        int halfBitmapWidth = mInitContentWidth / 2;
        int halfBitmapHeight = mInitContentHeight / 2;
        float widthScale = 0;
        float heightScale = 0;
        float ae = distance4PointF(movePoint, mCenterPoint);
        float ce = distance4Point(movePoint, mRBPoint);
        float ac = distance4Point(mRBPoint, mCenterPoint);

        float cosa = (float) ((Math.pow(ae, 2) + Math.pow(ac, 2) - Math.pow(ce, 2)) / (2 * ae * ac));
        float angleA = (float) Math.toDegrees(Math.acos(cosa));
        float cosB = halfBitmapHeight / ac;
        float angleB = (float) Math.toDegrees(Math.acos(cosB));
        float angleP = angleB - angleA;
        float newHeight = (float) (ae * Math.cos(Math.toRadians(angleP)));
        float newWidth = (float) (ae * Math.sin(Math.toRadians(angleP)));

        widthScale = newWidth / halfBitmapWidth;
        heightScale = newHeight / halfBitmapHeight;

        Log.d("TEST", "widthScale:" + widthScale + ",heightScale:" + heightScale);
//        float scale = 1f;
//        int halfBitmapWidth = mInitContentWidth / 2;
//        int halfBitmapHeight = mInitContentHeight / 2;
//        //图片某个点到图片中心的距离
//        float bitmapToCenterDistance = (float) Math.sqrt(halfBitmapWidth * halfBitmapWidth + halfBitmapHeight * halfBitmapHeight);
//
//        //移动的点到图片中心的距离
//        float moveToCenterDistance = distance4PointF(mCenterPoint, mCurMovePointF);
//
//        //计算缩放比例
//        scale = moveToCenterDistance / bitmapToCenterDistance;
////        //图片某个点到图片中心的距离
//        float pointToCenterDistance = (float) Math.sqrt(halfBitmapWidth * halfBitmapWidth + halfBitmapHeight * halfBitmapHeight);
//        float cosa = halfBitmapWidth / pointToCenterDistance;
//        float sina = halfBitmapHeight / pointToCenterDistance;
//
//        float widthScale = (moveToCenterDistance * cosa) / halfBitmapWidth;
//
//        float heightScale = (moveToCenterDistance * sina) / halfBitmapHeight;

        //缩放比例的界限判断
        if (widthScale <= MIN_SCALE) {
            widthScale = MIN_SCALE;
        } else if (widthScale >= MAX_SCALE) {
            widthScale = MAX_SCALE;
        }
        if (heightScale <= MIN_SCALE) {
            heightScale = MIN_SCALE;
        } else if (heightScale >= MAX_SCALE) {
            heightScale = MAX_SCALE;
        }
//        textSize = (int) (34 * scale);
        Log.d("TEST", "mCurMovePointF：" + mCurMovePointF.toString() + "，mPreMovePointF：" + mPreMovePointF.toString());
        mWidthScale = widthScale;
        textSize = (int) (34 * heightScale);
        transformDraw();
    }

    private int getViewWidth() {
        return Math.max(getAllEditBitmapWidth(), (getBitmapDiagonalLength() + mDeleteDrawableWidth + (framePadding * 2)));
    }

    private int getViewHeight() {
        return getBitmapDiagonalLength() + getEditBitMapOffsetHeight() + editBitmapHeight + (framePadding * 2);
    }

    private int getBitmapDiagonalLength() {
        return (int) Math.sqrt(Math.pow(mContentHeight, 2) + Math.pow(mContentWidth, 2));
    }

    /**
     * 获取四个点和View的大小
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param degree
     */
    private void computeRect(int left, int top, int right, int bottom, float degree) {
        Point lt = new Point(left, top);
        Point rt = new Point(right, top);
        Point rb = new Point(right, bottom);
        Point lb = new Point(left, bottom);
        cp = new Point((right - left + 1) / 2 + left, (bottom - top + 1) / 2 + top);
        obtainRoationPoint(mLTPoint, cp, lt, degree);
        obtainRoationPoint(mRTPoint, cp, rt, degree);
        obtainRoationPoint(mRBPoint, cp, rb, degree);
        obtainRoationPoint(mLBPoint, cp, lb, degree);

        mLTPoint.x += halfDrawableWidth;
        mRTPoint.x += halfDrawableWidth;
        mRBPoint.x += halfDrawableWidth;
        mLBPoint.x += halfDrawableWidth;

        //将Bitmap的四个点的Y坐标移动offsetY + halfDrawableHeight
        mLTPoint.y += halfDrawableHeight;
        mRTPoint.y += halfDrawableHeight;
        mRBPoint.y += halfDrawableHeight;
        mLBPoint.y += halfDrawableHeight;
    }


    /**
     * 根据位置判断控制图标处于那个点
     *
     * @return
     */
    private Point LocationToPoint(int location) {
        switch (location) {
            case LEFT_TOP:
                return mLTPoint;
            case RIGHT_TOP:
                return mRTPoint;
            case RIGHT_BOTTOM:
                return mRBPoint;
            case LEFT_BOTTOM:
                return mLBPoint;
        }
        return mLTPoint;
    }


    /**
     * 获取变长参数最大的值
     *
     * @param array
     * @return
     */
    public int getMaxValue(Integer... array) {
        List<Integer> list = Arrays.asList(array);
        Collections.sort(list);
        return list.get(list.size() - 1);
    }


    /**
     * 获取变长参数最大的值
     *
     * @param array
     * @return
     */
    public int getMinValue(Integer... array) {
        List<Integer> list = Arrays.asList(array);
        Collections.sort(list);
        return list.get(0);
    }


    /**
     * 获取旋转某个角度之后的点
     *
     * @param source
     * @param degree
     * @return
     */
    public void obtainRoationPoint(Point targetPoint, Point center, Point source, float degree) {
        targetPoint.x = (int) (((source.x - center.x) * Math.cos(degreeToRadian(degree))) - ((source.y - center.y) * Math.sin(degreeToRadian(degree))) + center.x);
        targetPoint.y = (int) (((source.x - center.x) * Math.sin(degreeToRadian(degree))) + ((source.y - center.y) * Math.cos(degreeToRadian(degree))) + center.y);
    }


    /**
     * 弧度换算成角度
     *
     * @return
     */
    public static double radianToDegree(double radian) {
        return radian * 180 / Math.PI;
    }


    /**
     * 角度换算成弧度
     *
     * @param degree
     * @return
     */
    public static double degreeToRadian(double degree) {
        return degree * Math.PI / 180;
    }

    /**
     * 根据点击的位置判断是否点中控制旋转，缩放的图片， 初略的计算
     *
     * @param x
     * @param y
     * @return
     */
    private int JudgeStatus(float x, float y) {
        PointF touchPoint = new PointF(x, y);
        PointF controlPointF = new PointF(mControlPoint);
        PointF deletePointF = new PointF(mDeletePoint);

        //点击的点到控制旋转，缩放点的距离
        float distanceToControl = distance4PointF(touchPoint, controlPointF);
        float distanceToDelete = distance4PointF(touchPoint, deletePointF);

        //如果两者之间的距离小于 控制图标的宽度，高度的最小值，则认为点中了控制图标
        if (distanceToControl < Math.min(mDrawableWidth / 2, mDrawableHeight / 2)) {
            return STATUS_ZOOM;
        }

        //如果两者之间的距离小于 控制图标的宽度，高度的最小值，则认为点中了移除按钮
        if (distanceToDelete < Math.min(mDrawableWidth / 2, mDrawableHeight / 2)) {
            return STATUS_ROTATE;
        }

        return STATUS_OUT;

    }


    /**
     * 设置是否处于可缩放，平移，旋转状态
     *
     * @param isEditable
     */
    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
        invalidate();
    }


    /**
     * 两个点之间的距离
     *
     * @param pf1
     * @param pf2
     * @return
     */
    private float distance4PointF(PointF pf1, PointF pf2) {
        float disX = pf2.x - pf1.x;
        float disY = pf2.y - pf1.y;
        return (float) Math.sqrt(disX * disX + disY * disY);
    }

    /**
     * 两个点之间的距离
     *
     * @param pf1
     * @param pf2
     * @return
     */
    private float distance4Point(PointF pf1, Point pf2) {
        float disX = pf2.x - pf1.x;
        float disY = pf2.y - pf1.y;
        return (float) Math.sqrt(disX * disX + disY * disY);
    }

    /**
     * 两个点之间的距离
     *
     * @param pf1
     * @param pf2
     * @return
     */
    private float distance4Point(Point pf1, PointF pf2) {
        float disX = pf2.x - pf1.x;
        float disY = pf2.y - pf1.y;
        return (float) Math.sqrt(disX * disX + disY * disY);
    }

    public interface IClickListener {
        void onClickDelete();

        void onClickEdit();

        void onClickTimestamp();

        void onClickView();
    }

    public void setTabClickListener(IClickListener listener) {
        this.mClickListener = listener;
    }

    private int getMaxY() {
        return getMaxValue(mRTPoint.y, mRBPoint.y, mLBPoint.y, mLTPoint.y);
    }

    private int getMinY() {
        return getMinValue(mRTPoint.y, mRBPoint.y, mLBPoint.y, mLTPoint.y);
    }

    private int getMaxX() {
        return getMaxValue(mRTPoint.x, mRBPoint.x, mLBPoint.x, mLTPoint.x);
    }

    private int getMinX() {
        return getMinValue(mRTPoint.x, mRBPoint.x, mLBPoint.x, mLTPoint.x);
    }

    public int[] getBorderMargin() {
        int[] array = new int[4];
        array[0] = getMinX();//左边距
        array[1] = mViewWidth - getMaxX();//右边距
        array[2] = getMinY();//上边距
        array[3] = mViewHeight - getMaxY();//下边距
        return array;
    }

    public static boolean isPolygonContainsPoint(Point point, Point... mPoints) {
        int nCross = 0;
        for (int i = 0; i < mPoints.length; i++) {
            Point p1 = mPoints[i];
            Point p2 = mPoints[(i + 1) % mPoints.length];
            // 取多边形任意一个边,做点point的水平延长线,求解与当前边的交点个数
            // p1p2是水平线段,要么没有交点,要么有无限个交点
            if (p1.y == p2.y)
                continue;
            // point 在p1p2 底部 --> 无交点
            if (point.y < Math.min(p1.y, p2.y))
                continue;
            // point 在p1p2 顶部 --> 无交点
            if (point.y >= Math.max(p1.y, p2.y))
                continue;
            // 求解 point点水平线与当前p1p2边的交点的 X 坐标
            double x = (point.y - p1.y) * (p2.x - p1.x) / (p2.y - p1.y) + p1.x;
            if (x > point.x) // 当x=point.x时,说明point在p1p2线段上
                nCross++; // 只统计单边交点
        }
        // 单边交点为偶数，点在多边形之外 ---
        return (nCross % 2 == 1);
    }

}
