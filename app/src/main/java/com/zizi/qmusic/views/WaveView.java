package com.zizi.qmusic.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import com.zizi.qmusic.configs.RecordConfig;
import com.zizi.qmusic.qmusic.R;
import com.zizi.qmusic.utils.DimensionUtil;
import com.zizi.qmusic.utils.StringUtil;

public class WaveView extends View implements RecordAuditionView.RecordAuditionViewListener,
        RecordClipView.RecordClipViewListener{

    // 一个音频点代表多少毫秒 数据处理单位/采样率
    private static final float POINT_PER_MSEC = 2 * 2048.f / 44100.f * 1000.f;

    public interface EditWaveViewListener {
        void onEditWaveViewUpdateTimeListener(long currentTime, long sumTime);
    }

    public RecordAuditionView mAuditionView;
    private RecordClipView mClipView;

    private int mTotalLines;    // 总共有多少波形
    private int mDrawLines;     // 已画波形数
    private int mMaxScreenLines;// 界面上最多显示的波形数
    private int mCurrentIndex;  // 总共有多少个真实点
    private int mStartIndex;    // 开始下标
    private int mEditIndex;     // 裁剪开始下标

    private Paint mWavePaint;
    private Paint mEditPaint;

    private int mEidtWaveColor = Color.parseColor("#cecac4");
    private int mRecordWaveColor = getResources().getColor(R.color.color_ffc341);
    private int mPlayerWaveColor = getResources().getColor(R.color.color_ffc341);

    /* 宽高 */
    private int mHeight; // 总高度
    private int mWidth;  // 总宽度
    private int mPaintW;
    private int mPaintH;
    private int mPaddingLR = DimensionUtil.dipToPx(getContext(), 18); // 默认左右没有边框
    private float mWaveWidth; // 一个波柱的宽度;
    private float mStartY;// 中间的坐标 Y

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mClearPaint;
    private Matrix mMatrix;
    private int mBitmapW;
    private int mBitmapH;
    private Rect mSrcRect;
    private Rect mDstRect;
    private int mLastIndex;
    private boolean isFirstCreate;


    /* 监听 */
    private EditWaveViewListener mListener;

    /* 逻辑数据 */
    private boolean isFirst;
    private int mScreenShowSecond;
    //private boolean isEditPlaying;
    private boolean autoMove = false;
    private boolean isLeft = false;
    private int mAudioMoveOff = DimensionUtil.dipToPx(getContext(), 20);
    private float[] mPoints;
    private float[] mAuditionPoints;

    /* 时间 */
    private long mSumRecordTime;//总录音时间
    private long mCurrentEditTime;//剪辑的当前时间

    /* 滑动波形相关 */
    private GestureDetector mGestureDetector;
    private Scroller mScroller;
    private int mLastScrollX;


    public WaveView(Context context) {
        this(context, null);
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public void setAuditionView(RecordAuditionView auditionView) {
        this.mAuditionView = auditionView;
        mAuditionView.setRecordAuditionViewListener(this);
    }

    public void setClipView(RecordClipView clipView) {
        mClipView = clipView;
        mClipView.setClipViewListener(this);
    }

    public void setListener(EditWaveViewListener listener) {
        mListener = listener;
    }

    private void initView() {
        mWavePaint = new Paint();
        mWavePaint.setAntiAlias(true);
        mWavePaint.setStyle(Style.FILL);
        mWavePaint.setColor(mRecordWaveColor);

        mClearPaint = new Paint();
        mClearPaint.setAntiAlias(true);
        mClearPaint.setStyle(Style.FILL);
        mClearPaint.setColor(getResources().getColor(R.color.color_ffffff));

        mEditPaint = new Paint();
        mEditPaint.setAntiAlias(true);
        mEditPaint.setStyle(Style.FILL);
        mEditPaint.setColor(Color.parseColor("#4cf0354b"));

        mSrcRect = new Rect();
        mDstRect = new Rect();
        mMatrix = new Matrix();

        mGestureDetector = new GestureDetector(getContext(), mGestureListener);
        mGestureDetector.setIsLongpressEnabled(false);
        mScroller = new Scroller(getContext());

        isFirst = true;
    }

    private int getMaxShowPoints() {
        // 点数
        int pointCount = (int) (RecordConfig.WAVE_DISPLAY_MSEC / (POINT_PER_MSEC * 1));
        return pointCount;
    }

    private void setup(int w, int h) {

        this.mWidth = w;
        this.mHeight = h;

        this.mPaintW = w - mPaddingLR - mPaddingLR;
        this.mPaintH = h;
        this.mStartY = mHeight / 2.0f;

        if (isFirst) {
            mTotalLines = (int) (RecordConfig.MAX_RECORD_MILLISECOND / 46); // 一个小时的波形点数
            mPoints = new float[mTotalLines];
            mMaxScreenLines = (int) (getDisplayScreenSecond() / getMsecPerPoint()); // 20s的波形点数
            mEditIndex = mMaxScreenLines + 1;
            mEditPlayCurrentIndex = mEditIndex;
        }

        this.mWaveWidth = (float) mPaintW / (mMaxScreenLines * 1.0f);// 一个波柱的宽度;
        mMatrix.postTranslate(mWaveWidth, 0);

        destroy();
        reset();
    }


    //region 试听相关

    public void setAuditionMode(boolean listenMode, int sumRecordTime) {

        this.isAuditionMode = listenMode;

        destroy();
        mSumRecordTime = sumRecordTime;
        if (listenMode) {
            mStartPlayCurrentTime = 0;
            changeDisplayPoints(sumRecordTime);
            mDrawLines = mMaxScreenLines;

            mStartIndex = 0;
            mCurrentEditTime = 0;

            mAuditionView.setRecordTime(mSumRecordTime);
            mAuditionView.updateThumbForTimeUpdate(0);

            long time = (long) ((mStartIndex + mEditIndex) * getMsecPerPoint());

            if (null != mListener) {
                mListener.onEditWaveViewUpdateTimeListener(
                        isAuditionMode ? time : getStartRcordTime() + time,
                        mSumRecordTime);
            }

        } else {

            pauseEditPlay();


            changeDisplayPoints(RecordConfig.WAVE_DISPLAY_MSEC);
        }
        invalidate();
    }
    //endregion

    //region Wave渲染设置

    private void changeDisplayPoints(int screenShowSecond) {

        mScreenShowSecond = screenShowSecond;
        mSumRecordTime = screenShowSecond;
        mMaxScreenLines = (int) (getDisplayScreenSecond() / getMsecPerPoint()); // 10s的波形点数

        if (mMaxScreenLines > getMaxShowPoints()) {
            mMaxScreenLines = getMaxShowPoints();
        }

        // 一个波柱的宽度;
        mWaveWidth = (float) mPaintW / (mMaxScreenLines * 1.0f);

        mEditIndex = 0;
        mEditPlayCurrentIndex = mEditIndex;
        mMatrix.postTranslate(mWaveWidth, 0);

        reset();
    }

    private int getDisplayScreenSecond() {
        if (mScreenShowSecond > 0) {
            return mScreenShowSecond;
        }
        return RecordConfig.WAVE_DISPLAY_MSEC;
    }
    //endregion

    private long getStartRcordTime() {
        return 0L;
    }


    //region 重载 画图setStartRcordTime

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (null != mBitmap && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if ((mWidth != w || mHeight != h)) {

            setup(w, h);
            isFirst = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (null == mBitmap || mBitmap.isRecycled()) {
            reset();
        }

        if (isEditMode || isAuditionMode) {
            drawEdit(canvas);
        } else {
            drawRecord(canvas);
        }
    }

    private void drawRecord(Canvas canvas) {
        try {
            if (isFirstCreate) {
                isFirstCreate = false;
                canvas.drawBitmap(mBitmap, mPaddingLR, 0, mWavePaint);
                return;
            }

            int currrents = mCurrentIndex;
            int lastIndex = mLastIndex;
            float startX = 0;

            // 波形超过显示范围时
            if (currrents > mMaxScreenLines) {
                int offset = (int) ((currrents - lastIndex) * mWaveWidth + mWaveWidth * 0.2);
                // 清除最后一个波形？往左一个波形？
                mSrcRect.set(offset, 0, mBitmapW, mBitmapH);
                mDstRect.set(0, 0, mBitmapW - offset, mBitmapH);
                mCanvas.drawBitmap(mBitmap, mSrcRect, mDstRect, null);
                // 清除最后一个波形
                mDstRect.set(mBitmapW - offset, 0, mBitmapW, mBitmapH);
                mCanvas.drawRect(mDstRect, mClearPaint);

                currrents = currrents % lastIndex;
                lastIndex = 0;
                startX = mBitmapW - offset;
            }

            if (null != mPoints && currrents > 0) {
                for (; lastIndex < currrents; lastIndex++) {
                    float s = (startX + lastIndex * mWaveWidth);
                    float e = (s + mWaveWidth - getDrawWaveWidthOffset());
                    float h = mPoints[mLastIndex] * mPaintH;
                    mLastIndex++;
                    mWavePaint.setStrokeWidth(h);
                    mCanvas.drawLine(s, mStartY, e, mStartY, mWavePaint);
                }
            }

            canvas.drawBitmap(mBitmap, mPaddingLR, 0, mWavePaint);
        } catch (Exception e) {

        }
    }

    private void setAuditionViewThumbChanged(float thumbX) {
        mEditIndex = (int) (thumbX / mWaveWidth);

        if (mEditIndex < 0) {
            mEditIndex = 0;
        }

        mEditPlayCurrentIndex = mEditIndex;

        invalidate();
    }

    private void setAuditionThumbX(float x) {

        //Ln.e("mEditPlayCurrentTime[%d], mSumRecordTime[%d], thumbMoveDistance[%f]", mEditPlayCurrentTime,mSumRecordTime,thumbMoveDistance);
        mAuditionView.updateThumbForTimeUpdate(x);
    }

    private void drawEdit(Canvas canvas) {

        float[] points = isAuditionMode ? mAuditionPoints : mPoints;
        int editPlayIndex = mEditPlayCurrentIndex - mStartIndex;

        //Log.d("zht", "editPlayIndex = "+editPlayIndex+" EditPlayCurrentIndex="+mEditPlayCurrentIndex+" StartIndex="+mStartIndex);

        if (editPlayIndex < 0) {
            //Log.d("zht", "editPlayIndex = " + editPlayIndex);
        }

        boolean isPlaying = true;

        float startX, endX, lineH;
        int i, j;
        for (i = 0, j = mStartIndex; i < mDrawLines; i++, j++) {
            startX = mPaddingLR + mWaveWidth * i;
            endX = (startX + mWaveWidth - getDrawWaveWidthOffset());
            lineH = points[j] * mPaintH;

            if (isPlaying) {
                // 已播放的波形
                if (i >= mEditIndex && i < editPlayIndex) {
                    mWavePaint.setColor(mPlayerWaveColor);
                }
                // 未播放的波形
                if (i >= editPlayIndex) {
                    mWavePaint.setColor(mEidtWaveColor);
                }
            } else {
                if (i >= mEditIndex) {
                    mWavePaint.setColor(mEidtWaveColor);
                }
            }

            mWavePaint.setStrokeWidth(lineH);
            canvas.drawLine(startX, mStartY, endX, mStartY, mWavePaint);
        }


        mWavePaint.setColor(mRecordWaveColor);

        // 更新拖动条
        if (isPlaying) {
            if (isAuditionMode) {
                float thumbMoveDistance = (mEditPlayCurrentTime) * 1f / mSumRecordTime *
                        mPaintW;
                setAuditionThumbX(thumbMoveDistance);
            } else {
                if (editPlayIndex == mMaxScreenLines && (mSumRecordTime - getStartRcordTime()) >= mEditPlayCurrentTime) {

                    if (mEditPlayStartScrollTime == 0)
                        mEditPlayStartScrollTime = mEditPlayCurrentTime;

                    if (mEditPlayCurrentIndex <= mCurrentIndex) {
                        long t = mEditPlayCurrentTime - mEditPlayStartScrollTime;
                        float d = t / getMsecPerPoint() * mWaveWidth;

                        float offsetX = mTouchStartX - mClipView.getThumbOffset() - d;
                        mClipView.setThumbPositionX(offsetX);
                    }
                }
            }

        } else {

        }

        // 更新时间
        renderTimeWhenNotPlaying();
    }
    //endregion

    private void renderTimeWhenNotPlaying() {

        boolean isPlaying = true;

        if (isPlaying) {
            return;
        }

        long time = (long) ((mStartIndex + mEditIndex) * getMsecPerPoint());

        // 继续录制特殊情况
        if (isEditMode && getStartRcordTime() > 0) {
            time += getStartRcordTime();
        }

        // 容错
        if (time > mSumRecordTime) {
            time = mSumRecordTime - getStartRcordTime();
        }

        // 刷新时间
        if (time != mCurrentEditTime) {
            if (isEditMode) {
                mClipView.setTimeText(StringUtil.getMMSSString(time / 1000));
                mClipView.drawView();
            }

            if (null != mListener) {
                mListener.onEditWaveViewUpdateTimeListener(time, mSumRecordTime);
            }
        }

        mCurrentEditTime = time;

    }

    private void drawEdit2Record() {
        reset();
    }


    /**
     * 从time开始裁剪
     *
     * @param time
     */
    public void cutRecord(long time) {
        time -= getStartRcordTime();
        if (time <= 0) {
            mCurrentIndex = 0;
        } else {
            int index = (int) (time / getMsecPerPoint());
            if (index < mCurrentIndex) {
                mCurrentIndex = index + 1;
                if (mCurrentIndex > mMaxScreenLines) {
                    mDrawLines = mMaxScreenLines;
                    mStartIndex = mCurrentIndex - mMaxScreenLines;
                } else {
                    mStartIndex = 0;
                    mDrawLines = mCurrentIndex;
                }
            }
        }

        isEditMode = false;

        reset();
    }

    public void reset() {

        mBitmapW = mPaintW;
        mBitmapH = mPaintH;

        if (null == mBitmap || mBitmap.isRecycled()) {
            if(mBitmapW <= 0 || mBitmapH <= 0){
                mBitmapW =DimensionUtil.getDisplayWidth(getContext()) - mPaddingLR - mPaddingLR;
                mBitmapH = DimensionUtil.dipToPx(35);
            }
            mBitmap = Bitmap.createBitmap(mBitmapW, mBitmapH, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        mCanvas.drawColor(getResources().getColor(R.color.color_ffffff));

        // 初始化试听模式下的点
        if (isAuditionMode) {
            mAuditionPoints = new float[mTotalLines];
            mAuditionPoints = getAuditionPoints();
        }

        // 重绘可见的或最后的部分
        int currrents = getCurrentIndex() > mMaxScreenLines ? mMaxScreenLines : getCurrentIndex();
        mLastIndex = 0;
        // 如果是试听模式，cIndex横等于0
        int cIndex = getCurrentIndex() - currrents;
        if (isAuditionMode) {
            cIndex = 0;
        }
        if (null != mPoints && currrents > 0) {
            for (; mLastIndex < currrents; mLastIndex++, cIndex++) {
                float s = mLastIndex * mWaveWidth;
                float e = (s + mWaveWidth - getDrawWaveWidthOffset());
                float h = isAuditionMode ? mAuditionPoints[cIndex] * mPaintH : mPoints[cIndex] * mPaintH;
                mWavePaint.setStrokeWidth(h);
                mCanvas.drawLine(s, mStartY, e, mStartY, mWavePaint);
            }
        }
        mLastIndex = getCurrentIndex();
        isFirstCreate = true;

        invalidate();
    }

    private int getCurrentIndex() {
        if (isAuditionMode && isReducePointsMode()) {
            return getMaxShowPoints();
        }

        return mCurrentIndex;
    }


    private float getMsecPerPoint() {

        if (isAuditionMode && (mCurrentIndex > 0)) {

            // 增大的一帧的时间=总时间/压缩的帧数
            float secondPerPoint;

            if (mSumRecordTime > RecordConfig.WAVE_DISPLAY_MSEC) {
                secondPerPoint = mSumRecordTime / getMaxShowPoints();
            } else {
                if (getStartRcordTime() > 0) {
                    float beforeIndex = getStartRcordTime() / POINT_PER_MSEC;
                    secondPerPoint = mSumRecordTime / (mCurrentIndex + beforeIndex);
                } else {
                    secondPerPoint = mSumRecordTime / mCurrentIndex;
                }
            }

            return secondPerPoint;
        }
        return POINT_PER_MSEC;
    }

    //region 试听模式相关

    private boolean isReducePointsMode() {
        return mSumRecordTime > RecordConfig.WAVE_DISPLAY_MSEC;
    }

    private float[] getAuditionPoints() {

        float[] points = new float[mTotalLines];

        // 需要随机生成的波形数
        int randomLines = (int) (getStartRcordTime() / getMsecPerPoint());
        // 总共的波形数
        int totalLines = randomLines + mCurrentIndex;
        // 真实的波形数
        int trueLines = mCurrentIndex;
        // 压缩波形数的比例
        float compressRate = 0;

        if (totalLines > getMaxShowPoints()) {
            // 按比例获取需要生成的波形数
            compressRate = (float) getMaxShowPoints() / totalLines;
            randomLines = (int) (randomLines * compressRate);
        }

        double[] p = RecordConfig.virtualWavePoints();

        for (int i = 0, j = 0; i < randomLines; i++, j++) {

            if (j >= p.length)
                j = 0;

            points[i] = (float) p[j];
        }

        if (totalLines > getMaxShowPoints()) {
            // 需要抽样的波形数
            int sampleLines = getMaxShowPoints() - randomLines;

            float rate = (float) sampleLines / trueLines;

            // 按比例抽样获取波形
            for (int i = randomLines, j = 0; i < (sampleLines + randomLines); i++, j++) {
                int k = (int) (j / rate);
                if (k > trueLines) {
                    k = trueLines;
                }
                points[i] = mPoints[k];
            }
        } else {
            for (int i = randomLines, j = 0; i < (trueLines + randomLines); i++, j++) {
                points[i] = (mPoints[j]);
            }
        }

        return points;
    }

    // 在试听直接到剪辑模式下，获取当前的剪辑时间
    public long getEditTimeFromAudition() {

        if (mEditPlayCurrentTime > getStartRcordTime()) {
            return mEditPlayCurrentTime;
        }

        return getStartRcordTime();
    }

    public long getPlaybackTime() {
        return mEditPlayCurrentTime;
    }

    private float getDrawWaveWidthOffset() {

        float offset = mWaveWidth * 0.3f;
        float px1 = DimensionUtil.dipToPx(getContext(), 1);
        if (offset < px1 && mWaveWidth < px1) {
            offset = px1;
        }
        return offset;
    }


    //endregion
    public void destroy() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    //获取当前的剪辑时间
    public long getEditTime() {
        return mCurrentEditTime;
    }

    //添加点
    public void addPoint(float point) {
        // point值范围0-1
        float finalPoint = point * 2.f;
        // Log.d("zht", "p="+point);

        mPoints[mCurrentIndex] = finalPoint;
        mCurrentIndex++;

        if (mCurrentIndex > mMaxScreenLines) {
            mDrawLines = mMaxScreenLines;
            mStartIndex = mCurrentIndex - mMaxScreenLines;
        } else {
            mStartIndex = 0;
            mDrawLines = mCurrentIndex;
        }

        postInvalidate();
    }

    private void updateWave(float moveX) {

        if (moveX > 0) {
            // 波形向右画，也就是波形要向头部画
            if (mStartIndex <= 0) {
                return;
            }
            int l = (int) (moveX / mWaveWidth);
            mStartIndex -= l;
            if (mStartIndex <= 0) {
                mStartIndex = 0;
            }
        } else {
            // 波形向左画，也就是波形要向尾部画
            if (mStartIndex + mDrawLines >= mCurrentIndex) {
                return;
            }

            int l = (int) (moveX / mWaveWidth);
            mStartIndex -= l;
            if (mStartIndex + mDrawLines >= mCurrentIndex) {
                mDrawLines = mMaxScreenLines;
                mStartIndex = mCurrentIndex - mDrawLines;
            }
        }
        invalidate();
    }

    private boolean isEditMode;
    public boolean isAuditionMode;

    private float mTouchStartX;
    private float mMoveStartX;
    private boolean isTouchEidt; // 滑动裁剪
    private boolean isMoveEidt = false;
    private int mEditPlayCurrentIndex;// 裁剪的试听当前位置
    private long mEditPlayCurrentTime;// 裁剪播放的起始时间
    private long mEditPlayStartScrollTime;

    private float mStartPlayCurrentX;// 裁剪的开始试听位置
    private int mStartPlayCurrentIndex;// 裁剪的开始试听位置
    private long mStartPlayCurrentTime;// 裁剪播放的开始时间

    private int mStartIndexTemp;
    private int mEditIndexTemp;

    private boolean isCanPlayer;

    //region 剪辑模块
    public void startEditPlay() {

        pauseEditPlay();

        if (isAuditionMode == false && isEditMode == false)
            return;

        mEditPlayStartScrollTime = 0;

        mStartIndexTemp = mStartIndex;
        mEditIndexTemp = mEditIndex;
        mStartPlayCurrentIndex = mStartIndex + mEditIndex;
        mStartPlayCurrentTime = (long) (mStartPlayCurrentIndex * getMsecPerPoint());
        mStartPlayCurrentX = mPaddingLR + mStartPlayCurrentIndex * mWaveWidth;
        mEditPlayCurrentIndex = mStartPlayCurrentIndex;


    }


    public void pauseEditPlay() {

    }

    public void finishEditPlay() {
        // 完成播放后，播放进度重置到最开始
        if (isAuditionMode) {
            mStartIndex = 0;
            mEditIndex = 0;
            mEditPlayCurrentIndex = mEditIndex;
            mAuditionView.updateThumbForTimeUpdate(0);

            invalidate();
        }
    }


    public void editPlayStopUpdate() {

        // isEditPlaying = false;

        if (isAuditionMode == false) {
            // 还原刚开始播放的点
            mStartIndex = mStartIndexTemp;
            mEditIndex = mEditIndexTemp;
            mEditPlayCurrentIndex = mEditIndex;

            // 还原拖动条
            // float offsetX = mEditIndex*mWaveWidth - mClipView.getThumbOffset();
            float offsetX = mTouchStartX - mClipView.getThumbOffset();
            mClipView.setThumbPositionX(offsetX);

            postInvalidate();
        } else {
            mEditIndex = mEditPlayCurrentIndex - mStartIndex;
        }
    }

    //剪辑播放更新当前播放时间

    public void updatePlay(long playTime) {

        if (isAuditionMode) {

        } else if (!isEditMode) {
            return;
        }
        boolean isPlaying = true;
//        boolean isPlaying = RecordManager.getInstance().getAudioReplayIsPlaying();

        if (isPlaying == false)
            return;

        if (isAuditionMode == false) {
            playTime -= getStartRcordTime();
        }

        // 容错
        if (playTime < mStartPlayCurrentTime) {
            playTime = mStartPlayCurrentTime;
        }

        if (playTime > mSumRecordTime) {
            playTime = mSumRecordTime;
        }

        mEditPlayCurrentIndex = (int) (playTime / getMsecPerPoint());

        if (isAuditionMode == false) {
            if (mEditPlayCurrentIndex > getCurrentIndex()) {
                mEditPlayCurrentIndex = getCurrentIndex();
            }
        }


        if (mEditPlayCurrentIndex - mStartIndex >= mMaxScreenLines) {
            mStartIndex = mEditPlayCurrentIndex - mMaxScreenLines;
        }

        mEditPlayCurrentTime = playTime;

        postInvalidate();
    }


    public void resetFrame() {

        int paddingLR;
        if (isEditMode) {
            paddingLR = 0;
        } else {
            paddingLR = DimensionUtil.dipToPx(getContext(), 18);
        }
        if (paddingLR != mPaddingLR) {
            mPaddingLR = paddingLR;
            setup(mWidth, mHeight);
        }
    }

    //设置是否是剪辑模式
    public void setEditMode(boolean editMode,
                            long sumRecordTime,
                            long startEditTime) {

        setEditMode(editMode, sumRecordTime, startEditTime, false);
    }

    public float getThumbViewMaxX() {
        float emptyWidth = 0;

        if (mCurrentIndex < getMaxShowPoints()) {
            emptyWidth = mWidth * (getMaxShowPoints() - mCurrentIndex) / getMaxShowPoints();
        }
        float maxOriginX = mPaintW - mPaddingLR - emptyWidth;
        return maxOriginX;
    }

    public void setEditMode(boolean editMode,
                            long sumRecordTime,
                            long startEditTime,
                            boolean isFromReplay) {

        this.isEditMode = editMode;

        resetFrame();

        if (isEditMode) {
            float maxOriginX = getThumbViewMaxX();
            float thumbPositionDuration = 0;
            float maxRLocationX;
            long curDuration = startEditTime;
            long clipDuration = sumRecordTime - getStartRcordTime();

            // 当前剪辑时间（剔除开始录音的时间）
            if (getStartRcordTime() > 0) {
                curDuration -= getStartRcordTime();
            }

            if (isFromReplay) {
                // 点数小于一屏幕点数
                if (clipDuration <= RecordConfig.WAVE_DISPLAY_MSEC) {
                    thumbPositionDuration = clipDuration - curDuration;
                    mStartIndex = 0;
                }
                // 点数大于一屏幕点数
                else {
                    // 右边点数够一半
                    if (clipDuration - curDuration >= RecordConfig.WAVE_DISPLAY_MSEC / 2) {
                        // 从一半开始
                        thumbPositionDuration = RecordConfig.WAVE_DISPLAY_MSEC / 2;

                        float clipRate = curDuration * 1.0f / clipDuration * 1.0f;
                        float startEditIndex = mCurrentIndex * clipRate;
                        mStartIndex = (int) (startEditIndex - mMaxScreenLines / 2);
                        // 左边点数不够
                        if (mStartIndex < 0) {
                            thumbPositionDuration = RecordConfig.WAVE_DISPLAY_MSEC - curDuration;
                            mStartIndex = 0;
                        }
                    }
                    // 右边点数不够5s
                    else {
                        thumbPositionDuration = clipDuration - curDuration;
                        mStartIndex = mCurrentIndex - mMaxScreenLines;
                    }
                }
            } else {
                thumbPositionDuration = sumRecordTime - startEditTime;
                if (mCurrentIndex > mMaxScreenLines) {
                    mStartIndex = mCurrentIndex - mMaxScreenLines;
                } else {
                    mStartIndex = 0;
                }
            }

            // 长度/总可见点数*要显示的秒 = thumb的位置
            float offset = distanceWithSecond(thumbPositionDuration);
            maxOriginX -= offset;
            // MaxRight
            float emptyWidth = 0;
            if (mCurrentIndex < getMaxShowPoints()) {
                emptyWidth = (mPaintW * (getMaxShowPoints() - mCurrentIndex) / getMaxShowPoints());
            }
            float w = mPaintW - maxOriginX - emptyWidth;
            maxRLocationX = w + maxOriginX;


            // 告诉剪辑视图，渲染拖动条和可剪辑区域
            float thumbX = maxOriginX - mClipView.getThumbOffset();
            mClipView.setEditRectRight(maxRLocationX);
            mClipView.setTimeText(StringUtil.getMMSSString(getEditTime() / 1000));
            mClipView.setThumbPositionX(thumbX);

            mTouchStartX = maxOriginX;


            // 设置开始播放的时间
            mCurrentEditTime = startEditTime;
            if (mCurrentEditTime < 0) {
                mCurrentEditTime = 0;
            }
            // 设置总时长
            mSumRecordTime = sumRecordTime;

            // 设置从左边可见范围，开始编辑和播放的索引
            mEditIndex = (int) ((maxOriginX) / mWaveWidth);
            if (mEditIndex < 0) {
                mEditIndex = 0;
            }
            mEditPlayCurrentIndex = mEditIndex;


            // 设置总共要画多少条线
            if (mCurrentIndex > mMaxScreenLines) {
                mDrawLines = mMaxScreenLines;
            } else {
                mDrawLines = mCurrentIndex;
            }

            // 调用重绘
            invalidate();
        } else {

            pauseEditPlay();
//            RecordManager.getInstance().destoryAudioRecordReplay();

            mEditIndex = mMaxScreenLines + 1;
            mEditPlayCurrentIndex = mEditIndex;
            drawEdit2Record();
        }
    }


    private float distanceWithSecond(float second) {

        float rate = (float) mPaintW / (float) RecordConfig.WAVE_DISPLAY_MSEC;
        float distance = rate * second;
        return distance;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    //endregion

    //region Wave滚动相关

    private void startAutoMove(boolean left) {
        if (!autoMove) {
            autoMove = true;
            isLeft = left;
            post(mAutoRunnable);
        }
    }

    private void stopAutoMove() {
        if (autoMove) {
            autoMove = false;
            removeCallbacks(mAutoRunnable);
        }
    }

    private Runnable mAutoRunnable = new Runnable() {

        @Override
        public void run() {
            if (autoMove) {
                updateWave(isLeft ? mAudioMoveOff : -mAudioMoveOff);
                postDelayed(mAutoRunnable, 20);
            }
        }
    };
    //endregion


    // gesture listener
    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return true;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            if (isTouchEidt)
                return false;

            mLastScrollX = 0;
            isCanPlayer = true;
            final int maxX = 0x7FFFFFFF;
            final int minX = -maxX;
            mScroller.fling(mLastScrollX, 0, (int) -velocityX, 0, minX, maxX, 0, 0);
            setNextMessage(MESSAGE_SCROLL);

            return true;
        }
    };

    private void setNextMessage(int message) {
        clearMessages();
        animationHandler.sendEmptyMessage(message);
    }

    /**
     * Clears messages from queue
     */
    private void clearMessages() {
        animationHandler.removeMessages(MESSAGE_SCROLL);
        animationHandler.removeMessages(MESSAGE_JUSTIFY);
    }

    // animation handler
    private Handler animationHandler = new Handler() {

        public void handleMessage(Message msg) {

            if (!isCanPlayer || isAuditionMode) {
                return;
            }

            if (isTouchEidt) {
                return;
            }

            mScroller.computeScrollOffset();
            int currX = mScroller.getCurrX();
            int delta = mLastScrollX - currX;
            mLastScrollX = currX;
            if (delta != 0) {
                updateWave(delta);
            }

            if (Math.abs(currX - mScroller.getFinalY()) < MIN_DELTA_FOR_SCROLLING) {
                mScroller.forceFinished(true);
            }
            if (!mScroller.isFinished()) {
                animationHandler.sendEmptyMessage(msg.what);
            } else if (msg.what == MESSAGE_SCROLL) {
                setNextMessage(MESSAGE_JUSTIFY);
            } else {
                //滚动结束
                post(new Runnable() {
                    @Override
                    public void run() {
                        startEditPlay();
                        // 打点
                    }
                });
            }
        }
    };

    //region 拖动"Thumb"相关方法

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isTouchEidt == false && (isAuditionMode || isEditMode)) {
            boolean ges = mGestureDetector.onTouchEvent(event);

            final float x = event.getX();
            float offsetX = 0;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    clearMessages();
                    isCanPlayer = false;
                    mScroller.forceFinished(true);
                    pauseEditPlay();

                    offsetX = x + mClipView.getThumbOffset();
                    mMoveStartX = offsetX;

                    break;

                case MotionEvent.ACTION_MOVE:

                    offsetX = x + mClipView.getThumbOffset();
                    if (offsetX == mMoveStartX) // 没有移动
                        return true;

                    if (isAuditionMode == false && x - mTouchStartX != 0) {
                        isMoveEidt = true;
                        updateWave(offsetX - mMoveStartX);
                        Log.d("zht", "x-mTouchStartX=" + (offsetX - mMoveStartX) + " offsetX=" + offsetX);
                        mMoveStartX = offsetX;
                    }

                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:

                    stopAutoMove();

                    //Ln.d("zht", "startIndex=" + mStartIndex + " editIndex=" + mEditIndex + " time=" + ((mStartIndex + mEditIndex) * getMsecPerPoint()) + " st=" + getStartRcordTime());

                    if (isAuditionMode) {
                        setAuditionThumbX(x);
                        setAuditionViewThumbChanged(x);
                        // 延迟播放
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                startEditPlay();
                            }
                        }, 500);
                    }

                    // 强制刷新也没有更新
                    invalidate();
                    // 因为上一句的原因
                    renderTimeWhenNotPlaying();

                    if (isEditMode && isMoveEidt) {
                        if (ges == false) {
                            startEditPlay();
                        }
                    }

                    isMoveEidt = false;

                    break;
                default:
                    break;
            }
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }
    //endregion

    @Override
    public void onClipViewThumbTouchDown() {

        mScroller.forceFinished(true);

        isTouchEidt = true;

        pauseEditPlay();
    }

    @Override
    public void onClipViewThumbTouchMove(float x, int ret) {

        //if (mTouchStartX != x)
        {
            mTouchStartX = x;

            //Log.d("zht", " touch x=" + x);

            pauseEditPlay();

            mEditIndex = (int) (x / mWaveWidth);

            if (mEditIndex < 0) {
                mEditIndex = 0;
            }
            mEditPlayCurrentIndex = mEditIndex;

            invalidate();

            if (ret == 0) {
                if (autoMove) {
                    stopAutoMove();
                }

            } else {

                if (autoMove == false && isAuditionMode == false) {
                    startAutoMove(ret == 1 ? true : false);
                }
            }

        }
    }

    @Override
    public void onClipViewThumbTouchUp() {

        stopAutoMove();

        post(new Runnable() {
            @Override
            public void run() {
                startEditPlay();
                isTouchEidt = false;
            }
        });
    }

    @Override
    public void onAuditionViewThumbTouchDown() {

        pauseEditPlay();
    }

    @Override
    public void onAuditionViewThumbTouchMove(float moveX) {

        pauseEditPlay();
        setAuditionViewThumbChanged(moveX);
    }

    @Override
    public void onAuditionViewThumbTouchUp() {
        startEditPlay();
    }


    /**
     * Minimum delta for scrolling
     */
    public static final int MIN_DELTA_FOR_SCROLLING = 1;
    private final int MESSAGE_SCROLL = 0;
    private final int MESSAGE_JUSTIFY = 1;

}

