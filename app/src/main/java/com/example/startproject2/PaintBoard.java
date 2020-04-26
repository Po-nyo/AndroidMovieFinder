package com.example.startproject2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

public class PaintBoard extends View {
    Canvas mCanvas; // 캔버스
    Bitmap mBitmap; // 비트맵
    Paint mPaint;   // 페인트
    float lastX, lastY; // 이전 x,y 좌표
    Path mPath = new Path();    // Path

    static final float TOUCH_TOLERANCE = 8;

    public PaintBoard(Context context) {
        super(context);
        init(); // Paint 설정
    }

    private void init() {
        // PaintBoard 객체 생성시 초기설정
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(3.0F);

        this.lastX = -1;
        this.lastY = -1;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(mBitmap == null) {   // 이전 서명파일이 있어서 mBitmap에 설정된 경우에는 실행하지 않음
            Bitmap img = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas();
            canvas.setBitmap(img);
            canvas.drawColor(Color.WHITE);
            mBitmap = img;
            mCanvas = canvas;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mBitmap != null)
            canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 터치이벤트가 발생했을 때
        getParent().requestDisallowInterceptTouchEvent(true);
        int action = event.getAction(); // 액션 정보 가져옴

        float x = event.getX(); // x좌표
        float y = event.getY(); // y좌표

        if(action == MotionEvent.ACTION_UP) // 손가락을 뗐을 때
            mPath.rewind();
        else if(action == MotionEvent.ACTION_DOWN) // 손가락을 댔을 때
            touchDown(event);
        else if(action == MotionEvent.ACTION_MOVE) // 손가락을 댄 채로 움직였을 때
            precessMove(event);

        invalidate(); // 화면 갱신
        return true;
    }

    private void touchDown(MotionEvent event) { // 손가락을 댔을 때 실행
        float x = event.getX();
        float y = event.getY();

        lastX = x;
        lastY = y;

        mPath.moveTo(x, y);
        mCanvas.drawPath(mPath, mPaint);
    }

    private void precessMove(MotionEvent event) {   // 손가락을 댄 채로 움직였을 때
        float x = event.getX();
        float y = event.getY();
        float dx = Math.abs(x - lastX);
        float dy = Math.abs(y - lastY);

        if( dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            float cX = (x + lastX) / 2;
            float cY = (y + lastY) / 2;

            mPath.quadTo(lastX, lastY, cX, cY);
            lastX = x;
            lastY = y;
            mCanvas.drawPath(mPath, mPaint);
        }
    }

    public void clear() {   // 서명란 clear (MyFragment에서 지우기 버튼 눌렀을 때)
        mBitmap.eraseColor(Color.WHITE);
        invalidate();
    }

    public void changeBitmap(Bitmap bitmap) {   // Bitmap 파일을 수정가능하도록 적용
        Canvas canvas = new Canvas();
        Bitmap copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        canvas.setBitmap(copyBitmap);

        mBitmap = copyBitmap;
        mCanvas = canvas;

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
