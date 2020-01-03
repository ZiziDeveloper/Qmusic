package com.zizi.qmusic.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.zizi.qmusic.qmusic.R;
import com.zizi.qmusic.utils.DimensionUtil;


/**
 * Created by wdx on 16/6/1.
 */
public class RecordClipView extends View implements  View.OnTouchListener {


    // 拖动条
    private Bitmap mThumbBitmap;
    // 提示语
    private Paint mAssistedTextPaint = new Paint();
    int mAssistedTextBaseLine;
    // 刻度
    private Paint mScalePaint = new Paint();
    private Paint mTimePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private String mTimeText;
    // 剪辑选中区域
    private Rect mEditRect = new Rect();
    private Paint mEditPaint = new Paint();
    // 数据
    private boolean mIsTouchingThumb;

    private int mPaddingLR = DimensionUtil.dipToPx(getContext(), 18);

    private float mThumbMinLeft;
    private float mThumbWidth = 41.f;
    private float mThumbX;

    private float mWidth;
    private float mHeight;

    // 监听
    RecordClipViewListener mListener;


    public RecordClipView(Context context, AttributeSet attrs) {
        super(context, attrs);
//TODO R        inflate(getContext(), R.layout.view_record_clip_mode, null);
        setOnTouchListener(this);

        initViews();
    }

    private void initViews(){

        // 提示语
        mAssistedTextPaint.setColor(Color.parseColor("#7f66625b"));
        mAssistedTextPaint.setTextSize(DimensionUtil.dipToPx(getContext(), 12));
        mAssistedTextPaint.setAntiAlias(true);
        mAssistedTextPaint.setTextAlign(Paint.Align.CENTER);
        mAssistedTextPaint.setStrokeWidth(DimensionUtil.dipToPx(getContext(), 1));

        // 拖动条
        mThumbBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.record_clip_thumb)).getBitmap();
        // 刻度条
        mScalePaint.setStyle(Paint.Style.FILL);
        mScalePaint.setColor(Color.parseColor("#e0ddd6"));
        mScalePaint.setStrokeWidth(DimensionUtil.dipToPx(getContext(), 1));
        // 刻度条时间
        mTimePaint.setColor(Color.parseColor("#fe5353"));
        mTimePaint.setTextSize(DimensionUtil.dipToPx(getContext(), 10));
        // 剪辑区域
        mEditPaint.setAntiAlias(true);
        mEditPaint.setStyle(Paint.Style.FILL);
        mEditPaint.setColor(Color.parseColor("#4cf0354b"));
    }

    // 设置剪辑区域最远的位置
    public void setEditRectRight(float r) {
        mEditRect.right = (int) r;
    }

    // 设置时间
    public void setTimeText(String timeText) {

        mTimeText = timeText;
    }

    // 设置拖动条位置
    public void setThumbPositionX(float x) {
        updateThumbForTouchMove(x, false);
    }

    public float getThumbOffset() {
        float offset = mThumbBitmap.getWidth()/2;
        return offset;
    }

    public float getThumbPaddingLR()
    {
        return mPaddingLR;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if ((mWidth != w || mHeight != h))
        {
            mWidth = w;
            mHeight = h;

            mThumbMinLeft = mPaddingLR-getThumbOffset();
        }
    }

    private float getThumbMaxRight()
    {
        float maxPosition = mEditRect.right;
        if (maxPosition <= 0)
            maxPosition = mWidth;

        float thumbMaxRight = maxPosition - mPaddingLR - mThumbWidth + getThumbOffset();

        return thumbMaxRight;
    }

    private int updateThumbForTouchMove(float x, boolean correctBounds) {

        int ret = 0;

        if (correctBounds) {

            if (x < mThumbMinLeft) { // 往左过界了
                x = mThumbMinLeft;
                ret = 1;
            } else if (x > getThumbMaxRight()) { // 往右过界了
                x = getThumbMaxRight();
                ret = 2;
            } else {
                ret = 0;
            }
        }

        if (mThumbX != x)
        {
            mThumbX = x;
            drawView();
        }

        return ret;
    }

    private int updateThumbForTouchMove(float x) {

        return updateThumbForTouchMove(x, true);
    }

    public void drawView()
    {
        invalidate();
    }

    private boolean isContainTouch(float x, float y) {

        float l = mThumbX - DimensionUtil.dipToPx(getContext(), 20);
        float r = mThumbX + mThumbWidth + DimensionUtil.dipToPx(getContext(), 20);

        return x >= l && x <= r;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        final float x = event.getX();
        final float y = event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                mIsTouchingThumb = isContainTouch(x, y);

                if (mIsTouchingThumb) {

                    if (mListener != null) {
                        mListener.onClipViewThumbTouchDown();
                    }

                } else {

                }

                break;
            case MotionEvent.ACTION_MOVE:

                if (mIsTouchingThumb) {
                    int ret = updateThumbForTouchMove(x);
                    if (mListener != null) {
                        float offsetX = mThumbX + getThumbOffset();
                        mListener.onClipViewThumbTouchMove(offsetX, ret);
                    }
                }

                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                if (mIsTouchingThumb)
                {

                    if (mListener != null) {
                        mListener.onClipViewThumbTouchUp();
                    }
                }

                mIsTouchingThumb = false;

                break;
            default:
                break;
        }

        return mIsTouchingThumb;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // 提示语
        String promptText = "剪辑区域为选中时间至声音末尾";
        canvas.drawText(promptText, mWidth/2, DimensionUtil.dipToPx(getContext(), 18), mAssistedTextPaint);

        // 刻度条
        float scaleY = DimensionUtil.dipToPx(80);
        canvas.drawLine(0, scaleY, mWidth, scaleY, mScalePaint);

        int scaleCount = 11;
        float unitWith = (mWidth-mPaddingLR*2) / (scaleCount - 1);
        for (int i = 0; i < scaleCount; i++) {
            float scaleLength;
            if (i % 5 == 0) {
                scaleLength = DimensionUtil.dipToPx(getContext(), 12);
            } else {
                scaleLength = DimensionUtil.dipToPx(getContext(), 4);
            }
            canvas.drawLine(mPaddingLR+i*unitWith, scaleY, mPaddingLR+i*unitWith, scaleY - scaleLength, mScalePaint);
        }

        // 拖动条
        float thumbY = scaleY - DimensionUtil.dipToPx(getContext(), 13);
        canvas.drawBitmap(mThumbBitmap, mThumbX, thumbY, null);

        // 可剪辑区域
        int left = (int) (mThumbX+getThumbOffset());
        int top = (int) (scaleY + DimensionUtil.dipToPx(getContext(), 1));
        if (mEditRect.right == 0)
            mEditRect.right = (int) mWidth;
        int bottom = (int) (mHeight-DimensionUtil.dipToPx(getContext(), 1));
        mEditRect.set(left, top, mEditRect.right, bottom);
        canvas.drawRect(mEditRect, mEditPaint);

        // 拖动条上面的时间
        float timeTextY = thumbY - DimensionUtil.dipToPx(getContext(), 7);
        canvas.drawText(mTimeText, mThumbX-DimensionUtil.dipToPx(getContext(), 6), timeTextY, mTimePaint);

        // 底部刻度条
        canvas.drawLine(0, mHeight-mScalePaint.getStrokeWidth(), mWidth, mHeight-mScalePaint.getStrokeWidth(), mScalePaint);
    }

    public interface RecordClipViewListener {

        void onClipViewThumbTouchDown();
        void onClipViewThumbTouchMove(float x, int ret);
        void onClipViewThumbTouchUp();
    }

    public void setClipViewListener(RecordClipViewListener listener){
        this.mListener = listener;
    }
}

