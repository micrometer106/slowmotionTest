package com.example.micro.slowmotiontest;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.TextureView;
import android.view.View;

public class ScaleTextureView extends TextureView {
    private static final String TAG = ScaleTextureView.class.getSimpleName();

    private Context mContext;
    private ScaleGestureDetector mScaleDetector;
    private Matrix mMatrix = new Matrix();
    private float mScaleFactor;
    private float[] m;

    private PointF last = new PointF();
    private PointF start = new PointF();
    private float right, bottom;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private float minScale = 1f;
    private float maxScale = 5f;
    private float saveScale = 1f;

    public ScaleTextureView(Context context) {
        super(context);
        initView(context);
    }

    public ScaleTextureView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
//        this.context = context;
        initView(context);
    }

    public ScaleTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        this.context = context;
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        setOnTouchListener(new ScaleListeners());
    }

    @Override
    public void setSurfaceTextureListener(SurfaceTextureListener listener) {
        super.setSurfaceTextureListener(listener);
        Log.d(TAG, "TTT setSurfaceTextureListener");
    }

    private class ScaleListeners implements OnTouchListener {
        public ScaleListeners() {
            super();
            m = new float[9];
            mScaleDetector = new ScaleGestureDetector(mContext, new ScaleListener());
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            mScaleDetector.onTouchEvent(motionEvent);

            mMatrix.getValues(m);
            float x = m[Matrix.MTRANS_X];
            float y = m[Matrix.MTRANS_Y];
            PointF curr = new PointF(motionEvent.getX(), motionEvent.getY());

            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    last.set(motionEvent.getX(), motionEvent.getY());
                    start.set(last);
                    mode = DRAG;
                    break;
                case MotionEvent.ACTION_UP:
                    mode = NONE;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    last.set(motionEvent.getX(), motionEvent.getY());
                    start.set(last);
                    mode = ZOOM;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == ZOOM || (mode == DRAG && saveScale > minScale)) {
                        float deltaX = curr.x - last.x;// x difference
                        float deltaY = curr.y - last.y;// y difference
                        if (y + deltaY > 0)
                            deltaY = -y;
                        else if (y + deltaY < -bottom)
                            deltaY = -(y + bottom);

                        if (x + deltaX > 0)
                            deltaX = -x;
                        else if (x + deltaX < -right)
                            deltaX = -(x + right);
                        mMatrix.postTranslate(deltaX, deltaY);
                        last.set(curr.x, curr.y);
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
            }
            setTransform(mMatrix);
            invalidate();
            return true;
        }

        private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                mode = ZOOM;
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float mScaleFactor = detector.getScaleFactor();
                float origScale = saveScale;
                saveScale *= mScaleFactor;
                if (saveScale > maxScale) {
                    saveScale = maxScale;
                    mScaleFactor = maxScale / origScale;
                } else if (saveScale < minScale) {
                    saveScale = minScale;
                    mScaleFactor = minScale / origScale;
                }
                right = getWidth() * saveScale - getWidth();
                bottom = getHeight() * saveScale - getHeight();
                if (0 <= getWidth() || 0 <= getHeight()) {
                    mMatrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
                    if (mScaleFactor < 1) {
                        mMatrix.getValues(m);
                        float x = m[Matrix.MTRANS_X];
                        float y = m[Matrix.MTRANS_Y];
                        if (mScaleFactor < 1) {
                            if (0 < getWidth()) {
                                if (y < -bottom)
                                    mMatrix.postTranslate(0, -(y + bottom));
                                else if (y > 0)
                                    mMatrix.postTranslate(0, -y);
                            } else {
                                if (x < -right)
                                    mMatrix.postTranslate(-(x + right), 0);
                                else if (x > 0)
                                    mMatrix.postTranslate(-x, 0);
                            }
                        }
                    }
                } else {
                    mMatrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
                    mMatrix.getValues(m);
                    float x = m[Matrix.MTRANS_X];
                    float y = m[Matrix.MTRANS_Y];
                    if (mScaleFactor < 1) {
                        if (x < -right)
                            mMatrix.postTranslate(-(x + right), 0);
                        else if (x > 0)
                            mMatrix.postTranslate(-x, 0);
                        if (y < -bottom)
                            mMatrix.postTranslate(0, -(y + bottom));
                        else if (y > 0)
                            mMatrix.postTranslate(0, -y);
                    }
                }
                return true;
            }
        }
    }
}
