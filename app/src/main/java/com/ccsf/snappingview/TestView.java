package com.ccsf.snappingview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.ccsf.snappingview.SignatureTextView.isPolygonContainsPoint;

public class TestView extends View {
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
    /**
     * 图片四个点坐标
     */
    private Point mInitLTPoint = new Point();
    private Point mInitRTPoint = new Point();
    private Point mInitRBPoint = new Point();
    private Point mInitLBPoint = new Point();
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
    private SignatureTextView.IClickListener mClickListener = null;

    private String content;
    private StaticLayout staticLayout;
    private TextPaint textPaint;
    private int textColor = Color.BLACK;
    private Layout.Alignment textAlign = Layout.Alignment.ALIGN_CENTER;
    private int textSize = 34;

    public TestView(Context context, String text, int num) {
        super(context);
        this.content = text;
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(500, 500);
    }

    private int getContentWidth() {
        return mInitContentWidth + (mDrawableWidth * 2);
    }

    private int getContentHeight() {
        return mInitContentHeight + (mDrawableHeight * 2);
    }

    public TestView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TestView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        textPaint = new TextPaint();
        textPaint.setColor(textColor);
        int defaultSize = 34;
        textPaint.setTextSize(defaultSize);
        textPaint.setAntiAlias(true);
        int contentWidth = (int) textPaint.measureText(content);
        staticLayout = new StaticLayout(content, textPaint, contentWidth / 2, textAlign, 1.0f, 0.0f, false);

        mInitContentHeight = staticLayout.getHeight();
        mInitContentWidth = staticLayout.getWidth();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(frameColor);
        mPaint.setStrokeWidth(frameWidth);
        mPaint.setStyle(Paint.Style.STROKE);
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
        initPoint();
    }

    private void initPoint() {
        mLTPoint.set(0, halfDrawableHeight);
        mLBPoint.set(0, halfDrawableHeight + mInitContentHeight);
        mRBPoint.set(mInitContentWidth, halfDrawableHeight + mInitContentHeight);
        mRTPoint.set(mInitContentWidth, halfDrawableHeight);

        mInitLBPoint.set(mLBPoint.x, mLBPoint.y);
        mInitLTPoint.set(mLTPoint.x, mLTPoint.y);
        mInitRBPoint.set(mRBPoint.x, mRBPoint.y);
        mInitRTPoint.set(mRTPoint.x, mRTPoint.y);
    }

    private void transform() {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //处于可编辑状态才画边框和控制图标
        if (isEditable && mRTPoint != null) {
            mPath.reset();
            mPath.moveTo(mLTPoint.x, mLTPoint.y);
            mPath.lineTo(mRTPoint.x, mRTPoint.y);
            mPath.lineTo(mRBPoint.x, mRBPoint.y);
            mPath.lineTo(mLBPoint.x, mLBPoint.y);
            mPath.close();
            canvas.drawPath(mPath, mPaint);
//            leftEditBitmapOffset = (mViewWidth - getAllEditBitmapWidth()) / 2;
//            topEditBitmapOffset = getMinValue(mLTPoint.y, mRTPoint.y, mLBPoint.y, mRBPoint.y) - getEditBitMapOffsetHeight() - framePadding - halfDrawableHeight;
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
//            if (isShowEditBitmap) {
//                for (int i = 0; i < tabBitmapList.size(); i++) {
//                    canvas.drawBitmap(tabBitmapList.get(i), getEditBitmapLeft(i), topEditBitmapOffset, null);
//                }
//            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStatus = JudgeStatus(event.getX(), event.getY());
                if (mStatus != STATUS_OUT || isPolygonContainsPoint(new Point((int) event.getX(), (int) event.getY()), mRTPoint, mRBPoint, mLBPoint, mLTPoint)) {
                    isEditable = true;
                    mPreMovePointF.set(event.getX() + mViewPaddingLeft, event.getY() + mViewPaddingTop);
                } else {
                    return super.onTouchEvent(event);
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
                        actionTextZoom();
//                        actionBitmapZoom();
                    } else if (mStatus == STATUS_DRAG) {
//                        actionDrag();
                    } else {
                        actionRotate();
                    }
                    mPreMovePointF.set(mCurMovePointF);
                }
                break;
        }
        return true;
    }

    private void actionTextZoom() {
        Point contentCenterPoint = new Point((int) (mCurMovePointF.x + mLTPoint.x) / 2, (int) (mCurMovePointF.y + mLTPoint.y) / 2);
        obtainRoationPoint(mRBPoint, contentCenterPoint, mCurMovePointF, -mDegree);
        obtainRoationPoint(mLTPoint, contentCenterPoint, mInitLTPoint, -mDegree);
        int offsetWidth = mRBPoint.x - mInitRBPoint.x;
        int offsetHeight = mRBPoint.y - mInitRBPoint.y;

        mLBPoint.set(mInitLBPoint.x, mInitLBPoint.y + offsetHeight);
        mRTPoint.set(mInitRTPoint.x + offsetWidth, mInitRTPoint.y);

        obtainRoationPoint(mLBPoint, contentCenterPoint, mLBPoint, mDegree);
        obtainRoationPoint(mRTPoint, contentCenterPoint, mRTPoint, mDegree);

        invalidate();
    }

    private void actionRotate() {
        Point contentCenterPoint = new Point(mLTPoint.x + mRBPoint.x / 2, mLTPoint.y + mRBPoint.y / 2);

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

        obtainRoationPoint(mLBPoint, contentCenterPoint, mLBPoint, mDegree);
        obtainRoationPoint(mLTPoint, contentCenterPoint, mLTPoint, mDegree);
        obtainRoationPoint(mRBPoint, contentCenterPoint, mRBPoint, mDegree);
        obtainRoationPoint(mRTPoint, contentCenterPoint, mRTPoint, mDegree);

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
     * 获取旋转某个角度之后的点
     *
     * @param source
     * @param degree
     * @return
     */
    public void obtainRoationPoint(Point targetPoint, Point center, PointF source, float degree) {
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
}
