package com.zizi.qmusic.componets.waveComponent.draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Handler.Callback;
import android.view.SurfaceView;

import com.zizi.playlib.record.RecordClient;
import com.zizi.qmusic.componets.waveComponent.view.WaveSurfaceView;

import java.util.ArrayList;
import java.util.Date;

/**
 * 录音和写入文件使用了两个不同的线程，以免造成卡机现象
 * 录音波形绘制
 * @author cokus
 *
 */
public class WaveCanvas {
    private static final String TAG = "WaveCanvas";

    public boolean isRecording = false;// 录音线程控制标记
    private  ArrayList<String>filePathList = new ArrayList<>();
    private int line_off ;//上下边距的距离
    public int rateX = 100;//控制多少帧取一帧
    public int rateY = 1; //  Y轴缩小的比例 默认为1
    public int baseLine = 0;// Y轴基线
    private int marginRight=30;//波形图绘制距离右边的距离
    private int marginLeft=20;//波形图绘制距离左边的距离
    private int draw_time = 1000 / 200;//两次绘图间隔的时间
    private float divider = 0.2f;//为了节约绘画时间，每0.2个像素画一个数据
    long c_time;//当前时间戳
    private Paint circlePaint;
    private Paint center;
    private Paint paintLine;
    private Paint mPaint;

    private Paint paintText;
    private Paint paintRect;

    RecordClient mRecordClient;



    /**
     * 开始录音
     * @param sfv
     * @param audioName
     */
    public void Start(final SurfaceView sfv
            ,String audioName,String path,Callback callback) {
        isRecording = true;
        init();
        line_off = ((WaveSurfaceView)sfv).getLine_off();
        mRecordClient = new RecordClient(audioName, path);
        mRecordClient.setOnRecordNotifyListner(new RecordClient.OnRecordNotifyListner() {

            @Override
            public void onStart() {

            }

            @Override
            public void onData(final ArrayList<Short> data) {
                sfv.post(new Runnable() {
                    @Override
                    public void run() {
                        long time = new Date().getTime();
                        if(time - c_time >= draw_time){
                            ArrayList<Short> buf = new ArrayList<Short>();
                            synchronized (data) {
                                if (data.size() == 0)
                                    return;
                                while(data.size() > (sfv.getWidth()-marginRight) / divider){
                                    data.remove(0);
                                }
                                buf = (ArrayList<Short>) data.clone();// 保存
                            }
                            SimpleDraw(sfv,buf, sfv.getHeight()/2);// 把缓冲区数据画出来
                            c_time = new Date().getTime();
                        }
                    }
                });

            }

            @Override
            public void onStop() {
                isRecording = false;
            }
        });
        mRecordClient.start();
    }

    /**
     * 绘制指定区域
     *
     * @param buf
     *            缓冲区
     * @param baseLine
     *            Y轴基线
     */
    void SimpleDraw(SurfaceView sfv,ArrayList<Short> buf, int baseLine) {
        divider = (float) ((sfv.getWidth()-marginRight-marginLeft)/(16000/rateX*20.00));
        if (!isRecording)
            return;
        rateY = (65535 /2/ (sfv.getHeight()-line_off));

        for (int i = 0; i < buf.size(); i++) {
            byte bus[] = getBytes(buf.get(i));
            buf.set(i, (short)((0x0000 | bus[1]) << 8 | bus[0]));//高低位交换
        }
        Canvas canvas = sfv.getHolder().lockCanvas(
                new Rect(0, 0, sfv.getWidth(), sfv.getHeight()));// 关键:获取画布
        if(canvas==null)
            return;
        canvas.drawARGB(255, 239, 239, 239);
        int start =(int) ((buf.size())* divider);
        float py = baseLine;
        float y;

        if(sfv.getWidth() - start <= marginRight){//如果超过预留的右边距距离
            start = sfv.getWidth() -marginRight;//画的位置x坐标
        }
        canvas.drawLine(0, line_off/2, sfv.getWidth(), line_off/2, paintLine);//最上面的那根线
        canvas.drawLine(0, sfv.getHeight()-line_off/2-1, sfv.getWidth(), sfv.getHeight()-line_off/2-1, paintLine);//最下面的那根线
        canvas.drawCircle(start, line_off/2, line_off/10, circlePaint);// 上圆
        canvas.drawCircle(start, sfv.getHeight()-line_off/2-1, line_off/10, circlePaint);// 下圆
        canvas.drawLine(start, line_off/2, start, sfv.getHeight()-line_off/2, circlePaint);//垂直的线
        int height = sfv.getHeight()-line_off;
        canvas.drawLine(0, height*0.5f+line_off/2, sfv.getWidth() ,height*0.5f+line_off/2, center);//中心线

//	         canvas.drawLine(0, height*0.25f+20, sfv.getWidth(),height*0.25f+20, paintLine);//第二根线
//	         canvas.drawLine(0, height*0.75f+20, sfv.getWidth(),height*0.75f+20, paintLine);//第3根线
        for (int i = 0; i < buf.size(); i++) {
            y =buf.get(i)/rateY + baseLine;// 调节缩小比例，调节基准线
            float x=(i) * divider;
            if(sfv.getWidth() - (i-1) * divider <= marginRight){
                x = sfv.getWidth()-marginRight;
            }
            //画线的方式很多，你可以根据自己要求去画。这里只是为了简单
            float y1 = sfv.getHeight() - y;
            if(y<line_off/2){
                y = line_off / 2;
            }
            if(y>sfv.getHeight()-line_off/2-1){
                y = sfv.getHeight() - line_off / 2 - 1;

            }
            if(y1<line_off/2){
                y1 = line_off / 2;
            }
            if(y1>(sfv.getHeight()-line_off/2-1)){
                y1 = (sfv.getHeight() - line_off / 2 - 1);
            }
            canvas.drawLine(x, y,  x,y1, mPaint);//中间出波形
        }
        sfv.getHolder().unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像
    }

    public byte[] getBytes(short s)
    {
        byte[] buf = new byte[2];
        for (int i = 0; i < buf.length; i++)
        {
            buf[i] = (byte) (s & 0x00ff);
            s >>= 8;
        }
        return buf;
    }

    public  void init(){
        circlePaint = new Paint();//画圆
        circlePaint.setColor(Color.rgb(246, 131, 126));//设置上圆的颜色

        center = new Paint();//中心线
        center.setColor(Color.rgb(39, 199, 175));// 画笔为color
        center.setStrokeWidth(1);// 设置画笔粗细
        center.setAntiAlias(true);
        center.setFilterBitmap(true);
        center.setStyle(Style.FILL);

        paintLine =new Paint();
        paintLine.setColor(Color.rgb(221, 221, 221));
        paintText = new Paint();
        paintText.setColor(Color.rgb(255, 255, 255));
        paintText.setStrokeWidth(3);
        paintText.setTextSize(22);

        paintRect = new Paint();
        paintRect.setColor(Color.rgb(39, 199, 175));

        mPaint = new Paint();
        mPaint.setColor(Color.rgb(39, 199, 175));// 画笔为color
        mPaint.setStrokeWidth(1);// 设置画笔粗细
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        mPaint.setStyle(Paint.Style.FILL);
    }


    /**
     * 停止录音
     */
    public void Stop() {
        isRecording = false;
        mRecordClient.stop();
        //inBuf.clear();// 清除
    }

    /**
     * 清楚数据
     */
    public void clear(){
    }

}

