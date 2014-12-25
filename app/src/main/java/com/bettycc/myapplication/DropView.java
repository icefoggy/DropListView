package com.bettycc.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;


public class DropView extends FrameLayout {

    private GestureDetector mGestureDetector;

    private int mDistanceY = 0;

    private int mRadius1;
    private int mRadius2;
    private float mMinRadius2;
    private int mTopPadding;
    private int mPullRange;
    private int mBzrOffset;
    private Mode mMode = Mode.PULL;

    enum Mode {
        NONE, PULL, LOADING
    }

    private android.view.GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mMode == Mode.PULL) {
                mDistanceY += (int) distanceY;
                if (mDistanceY < -mPullRange) {
                    onLoading();
                    return true;
                }

                if (mDistanceY > 0) {
                    mDistanceY = 0;
                }

                onDistanceYChanged(mDistanceY);

                invalidate();
                return true;
            } else {
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        }
    };

    private void onLoading() {
        mMode = Mode.LOADING;
        enableLoading(true);
    }

    private ProgressBar mLoadingView;

    private void onDistanceYChanged(int distanceY) {
        float v = (1 - getScrollPercent()) * mRadius1;
        if (v < mMinRadius2) {
            v = mMinRadius2;
        }

        mRadius2 = (int) v;
    }

    public float getDistanceY() {
        return mDistanceY;
    }

    public void setDistanceY(int distanceY) {
        mDistanceY = distanceY;
        invalidate();
    }

    enum Side {
        LEFT, RIGHT
    }


    public DropView(Context context) {
        super(context);
        init(null, 0);
    }

    public DropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public DropView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        mGestureDetector = new GestureDetector(getContext(), mGestureListener);
        mRadius1 = dpToPx(25);
        mRadius2 = mRadius1;
        mMinRadius2 = (float) (mRadius2 * 0.4);
        mTopPadding = dpToPx(15);
        mPullRange = dpToPx(200);
        mBzrOffset = dpToPx(20);

        mLoadingView = new ProgressBar(getContext());
        mLoadingView.setIndeterminate(true);
        int size = mRadius1 * 2 + dpToPx(5);
        LayoutParams params = new LayoutParams(size, size);
        params.topMargin = mTopPadding - dpToPx(5);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        mLoadingView.setLayoutParams(params);

        /**
         * Adjust loading view position to fit drop view.
         */
        mLoadingView.setPadding(0, 0, dpToPx(15), 0);

        addView(mLoadingView);
        enableLoading(false);
    }

    private void enableLoading(boolean b) {
        mLoadingView.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mMode == Mode.PULL) {
            drawPull(canvas);
        }

        super.onDraw(canvas);
    }

    private void drawPull(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);

        int cp1Y = mTopPadding + mRadius1;
        Point cp1 = new Point(getWidth() / 2, cp1Y);
        int cp2Y = cp1Y + (-mDistanceY);
        Point cp2 = new Point(getWidth() / 2, cp2Y);

        Path path = new Path();
        path.setFillType(Path.FillType.WINDING);
        RectF rectF = new RectF(cp1.x - mRadius1, cp1.y - mRadius1, cp1.x + mRadius1, cp1.y + mRadius1);
        path.addArc(rectF, -180, 180);

        addBzr(path, cp1.x + mRadius1, cp1.y, cp2.x + mRadius2, cp2.y, Side.RIGHT);
        rectF = new RectF(cp2.x - mRadius2, cp2.y - mRadius2, cp2.x + mRadius2, cp2.y + mRadius2);
        path.addArc(rectF, 0, 180);
        addBzr(path, cp2.x - mRadius2, cp2.y, cp1.x - mRadius1, cp1.y, Side.LEFT);

        canvas.drawPath(path, paint);
    }

    private void addBzr(Path path, int x, int y, int x1, int y1, Side side) {
        int i = (int) (getScrollPercent() * mBzrOffset);
        int mx;
        if (side == Side.RIGHT) {
            mx = (x + x1) / 2 - i;
        } else {
            mx = (x + x1) / 2 + i;
        }
        int my = (y + y1) / 2 - i;
        path.quadTo(mx, my, x1, y1);
    }

    private float getScrollPercent() {
        return (-getDistanceY()) / mPullRange;
    }

    public int dpToPx(float dp) {
        return (int) (getResources().getDisplayMetrics().density * dp);
    }
}
