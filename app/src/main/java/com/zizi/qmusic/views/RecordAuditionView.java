package com.zizi.qmusic.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.zizi.qmusic.qmusic.R;
import com.zizi.qmusic.utils.DimensionUtil;
import com.zizi.qmusic.utils.StringUtil;


public class RecordAuditionView extends View implements  View.OnTouchListener {

    public interface RecordAuditionViewListener {
        void onAuditionViewThumbTouchDown();
        void onAuditionViewThumbTouchMove(float moveX);
        void onAuditionViewThumbTouchUp();
    }

    RecordAuditionViewListener mListener;

    // 拖动条
    private Paint mThumbPaint = new Paint();
    private float mThumbMinLeft;
    private float mThumbMaxRight;
    private float mThumbWidth = DimensionUtil.dipToPx(getContext(), 1);
    private float mThumbX;

    // 刻度
    private Paint mScalePaint = new Paint();
    private Paint mTimePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private String mStartTimeText;
    private String mMiddleTimeText;
    private String mEndTimeText;

    // 数据
    private boolean mIsTouchingThumb;

    private int mPaddingLR = DimensionUtil.dipToPx(getContext(), 18);

    private float mWidth;  //总高度
    private float mHeight; //总宽度

    public RecordAuditionView(Context context, AttributeSet attrs){
        super(context, attrs);
        inflate(context, R.layout.view_record_audition_mode, null);
        setOnTouchListener(this);

        initView();
    }

    public void setRecordAuditionViewListener(RecordAuditionViewListener listener){
        this.mListener = listener;
    }

    private void initView(){

        // 拖动条
        initThumbView();

        // 刻度条
        initScaleView();

        // 刻度条时间
        initTimeView();
    }

    private void initThumbView() {

        mThumbPaint.setColor(Color.parseColor("#fe5353"));
        mThumbPaint.setStrokeWidth(mThumbWidth);
        // mThumbBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.rectangle_1577)).getBitmap();
    }

    private void initScaleView(){
        mScalePaint.setStyle(Paint.Style.FILL);
        mScalePaint.setColor(Color.parseColor("#e0ddd6"));
        mScalePaint.setStrokeWidth(DimensionUtil.dipToPx(getContext(), 1));
    }

    private void initTimeView(){
        mTimePaint.setColor(Color.parseColor("#e0ddd6"));
        mTimePaint.setTextSize(DimensionUtil.dipToPx(getContext(), 10));
    }

    private int updateThumbForTouchMove(float x) {

        int ret = 0;
        if (x < mThumbMinLeft) { // 往左过界了
            x = mThumbMinLeft;
            ret = 1;
        } else if (x > mThumbMaxRight) { // 往右过界了
            x = mThumbMaxRight;
            ret = 2;
        } else {
            ret = 0;
        }

        mThumbX = x;
        invalidate();

        return ret;
    }

    public void setRecordTime(long recordTime){
        mStartTimeText = StringUtil.getMMSSString(0);
        mMiddleTimeText = StringUtil.getMMSSString(recordTime/2/1000);
        mEndTimeText = StringUtil.getMMSSString(recordTime/1000);
    }

    public void updateThumbForTimeUpdate(float newLocationX){
        if(mIsTouchingThumb)
            return;

        newLocationX += mPaddingLR;
        updateThumbForTouchMove(newLocationX);
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
                        post(new Runnable() {
                            @Override
                            public void run() {
                                mListener.onAuditionViewThumbTouchDown();
                            }
                        });
                    }

                } else {

                }

                break;
            case MotionEvent.ACTION_MOVE:

                if (mIsTouchingThumb)
                {
                    updateThumbForTouchMove(x);
                    // float position = mThumbX / mWidth;
                    // Ln.d("zht  left=%f pos=%f" , mThumbView.getLeft(), mThumbX);

                    if (mListener != null) {
                        float offsetX = mThumbX - mPaddingLR + getThumbOffset();
                        mListener.onAuditionViewThumbTouchMove(offsetX);
                    }
                }

                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                if (mIsTouchingThumb)
                {
//                    if(event.getAction() == MotionEvent.ACTION_UP){
//                        UmsAgent.onEvent(getContext(), CobubConfig.EVENT_RECORD_CUT_MOVE_WAVEFORMS);
//                    }

                    if (mListener != null) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                mListener.onAuditionViewThumbTouchUp();
                            }
                        });
                    }
                }

                mIsTouchingThumb = false;

                break;
            default:
                break;
        }

        return mIsTouchingThumb;
    }

    private boolean isContainTouch(float x, float y) {

        float l = mThumbX - DimensionUtil.dipToPx(getContext(), 20);
        float r = mThumbX + mThumbWidth + DimensionUtil.dipToPx(getContext(), 20);

        return x >= l && x <= r;
    }

    public float getThumbOffset() {
        float offset = 0.f;
        return offset;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // 刻度条
        float scaleY = DimensionUtil.dipToPx(80);
        canvas.drawLine(0, scaleY, mWidth, scaleY, mScalePaint);
        canvas.drawLine(0, mHeight-mScalePaint.getStrokeWidth(), mWidth, mHeight-mScalePaint.getStrokeWidth(), mScalePaint);

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
        canvas.drawLine(mThumbX, scaleY, mThumbX, mHeight-DimensionUtil.dipToPx(getContext(), 1), mThumbPaint);
        // 拖动条上面的时间
        float timeOffsetLR = DimensionUtil.dipToPx(getContext(), 6);
        float timeWidth = DimensionUtil.dipToPx(getContext(), 24);
        float timeY = scaleY - DimensionUtil.dipToPx(getContext(), 15);
        canvas.drawText(mStartTimeText, timeOffsetLR, timeY, mTimePaint);
        canvas.drawText(mMiddleTimeText, mWidth/2-timeWidth/2, timeY, mTimePaint);
        canvas.drawText(mEndTimeText, mWidth-timeWidth-timeOffsetLR, timeY, mTimePaint);

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if ((mWidth != w || mHeight != h))
        {
            mWidth = w;
            mHeight = h;

            mThumbMinLeft = 0;
            mThumbMaxRight = mWidth - mThumbWidth - mPaddingLR;
        }
    }

}





