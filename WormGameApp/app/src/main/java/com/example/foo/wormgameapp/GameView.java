package com.example.foo.wormgameapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.example.foo.wormgameapp.grid.Grid;
import com.example.foo.wormgameapp.grid.HorizontalGrid;
import com.example.foo.wormgameapp.grid.VerticalGrid;

import java.util.Random;

/**
 * Created by foo on 3/24/18.
 */

public class GameView extends SurfaceView implements Runnable, View.OnTouchListener {

    protected static final String TAG = GameView.class.getSimpleName();

    protected SurfaceHolder mHolder;
    protected Context mContext;
    protected Thread mTask;
    protected boolean mRunning;
    protected float mDeviceWidth;
    protected float mDeviceHeight;
    protected int mBlockSize;
    protected Canvas mCanvas;
    protected int mNumHorizontalBlocks;
    protected int mNumVerticalBlocks;
    protected Paint mPaintGrid = new Paint();
    protected int[] mSnakeX = new int[100];
    protected int[] mSnakeY = new int[100];
    protected PointF mPointSnake = new PointF();
    protected Paint mPaintSnake = new Paint();
    protected PointF mPointApple = new PointF();
    protected Paint mPaintApple = new Paint();
    protected Random mRandom = new Random();
    protected VerticalGrid[] mGridV;
    protected HorizontalGrid[] mGridH;
    protected int mIdxAppleX;
    protected int mIdxAppleY;
    protected int mIdxSnakeX;
    protected int mIdxSnakeY;
    protected float mAppleBottom, mAppleLeft, mAppleRight, mAppleTop;
    protected float mSnakeBottom, mSnakeLeft, mSnakeRight, mSnakeTop;
    protected PointF mPointTouchDown = new PointF();
    protected boolean mMoveBottom, mMoveLeft, mMoveRight, mMoveTop;
    protected long mPrevMillis;
//    protected float mGridHstartX, mGridHstartY, mGridHstopX, mGridHstopY;
//    protected float mGridVstartX, mGridVstartY, mGridVstopX, mGridVstopY;





    public GameView(Context context) {
        super(context);
        mContext = context;
        this.setOnTouchListener(this);
        this.init();
    }

    @Override
    public void run() {
        while (mRunning) {
            this.updateView();
            this.drawView();
            this.controlFPS();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mPointTouchDown.set(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = (x - mPointTouchDown.x);
                float dy = (y - mPointTouchDown.y);
                float absDx = Math.abs(dx);
                float absDy = Math.abs(dy);
                if (absDx > absDy) { // move horizontally
                    if (dx < 0) { // move to left
                        Log.d(TAG, "LEFT");
                        this.resetMove();
                        mMoveLeft = true;
                    } else if ((0 < dx) && !mMoveRight) { // move to right
                        Log.d(TAG, "RIGHT");
                        this.resetMove();
                        mMoveRight = true;
                    }
                } else if (absDx < absDy) { // move vertically
                    if (dy < 0) { // move to top
                        Log.d(TAG, "TOP");
                        this.resetMove();
                        mMoveTop = true;
                    } else if (0 < dy) { // move to bottom
                        Log.d(TAG, "BOTTOM");
                        this.resetMove();
                        mMoveBottom = true;
                    }
                }
                break;
        }

        return true;
    }

    protected void init() {
//        this.hideNavigationBar();
        this.setDeviceSize();

        mNumVerticalBlocks = 9;
        mBlockSize = (int) (mDeviceWidth / mNumVerticalBlocks);
        mNumHorizontalBlocks = (int) (mDeviceHeight) / mBlockSize;

        mPaintGrid.setColor(Color.WHITE);
        mPaintSnake.setColor(Color.GREEN);
        mPaintApple.setColor(Color.RED);

        mGridV = new VerticalGrid[mNumVerticalBlocks];
        mGridH = new HorizontalGrid[mNumHorizontalBlocks];
        mIdxAppleX = mRandom.nextInt(mNumVerticalBlocks);
        mIdxAppleY = mRandom.nextInt(mNumHorizontalBlocks);

        mIdxSnakeX = 1;
        mIdxSnakeY = 1;

        this.setGrid();


        boolean debug = false;
    }

    public void startGame() {
        mRunning = true;
        mTask = new Thread(this);
        mTask.start();
    }

    public void pauseGmae() {
        mRunning = false;
    }

    public void resumeGame() {
        mRunning = true;
    }

    public void stopGame() {
        if (mTask != null) {
            try {
                mTask.join();
            } catch (InterruptedException ie) {
                Log.e(TAG, ie.getMessage(), ie);
            }
        }
    }

    protected void updateView() {
        // set apple left, top, right, bottom
        mAppleLeft = mGridV[mIdxAppleX].getStart().x;
        mAppleTop = mGridH[mIdxAppleY].getStart().y;
        mAppleRight = (mAppleLeft + mBlockSize);
        mAppleBottom = (mAppleTop + mBlockSize);

        // set snake left, top, right, bottom
        mSnakeLeft = mGridV[mIdxSnakeX].getStart().x;
        mSnakeTop = mGridH[mIdxSnakeY].getStart().y;
        mSnakeRight = (mSnakeLeft + mBlockSize);
        mSnakeBottom = (mSnakeTop + mBlockSize);

        if (mMoveBottom) {
            if (mIdxSnakeY < (mGridH.length - 1)){
                mIdxSnakeY++;
            }
        } else if (mMoveLeft) {
            if (mIdxSnakeX > 0) {
                mIdxSnakeX--;
            }
        } else if (mMoveRight) {
            if (mIdxSnakeX < (mGridV.length - 1)) {
                mIdxSnakeX++;
            }
        } else if (mMoveTop) {
            if (mIdxSnakeY > 0) {
                mIdxSnakeY--;
            }
        }



    }

    protected void drawView() {
        mHolder = this.getHolder();
        if (mHolder == null) {
            return;
        }
        if (!mHolder.getSurface().isValid()) {
            return;
        }
        mCanvas = mHolder.lockCanvas();
        if (mCanvas == null) {
            return;
        }
        mCanvas.drawColor(Color.BLACK);

        this.drawVerticalGrid();
        this.drawHorizontalGrid();

        // draw apple
        mCanvas.drawRect(mAppleLeft, mAppleTop, mAppleRight, mAppleBottom, mPaintApple);

        // draw snake
        mCanvas.drawRect(mSnakeLeft, mSnakeTop, mSnakeRight, mSnakeBottom, mPaintSnake);

        mHolder.unlockCanvasAndPost(mCanvas);
    }

    protected void controlFPS() {
        long sleepMillis = 500;
        long elapsedMillis = (System.currentTimeMillis() - mPrevMillis);
        sleepMillis -= elapsedMillis;
        if (sleepMillis > 0) {
            try {
                Thread.sleep(sleepMillis);
            } catch (InterruptedException ie) {
                Log.e(TAG, ie.getMessage(), ie);
            }
        }
        mPrevMillis = System.currentTimeMillis();
    }

    private void hideNavigationBar() {
        View decorView = ((AppCompatActivity) mContext).getWindow().getDecorView();
        int visivility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
        decorView.setSystemUiVisibility(visivility);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected void setDeviceSize() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            wm.getDefaultDisplay().getSize(point);
        } else {
            wm.getDefaultDisplay().getRealSize(point);
        }
        mDeviceWidth = point.x;
        mDeviceHeight = point.y;
        Log.d(TAG, "device W: " + mDeviceWidth + ", device H: " + mDeviceHeight);
    }

    protected void setGrid() {
        // draw vertical lines..
        for (int i = 0; i < mNumVerticalBlocks; i++) {
            float startX = (mBlockSize * i);
            float startY = 0.0f;
            PointF startP = new PointF(startX, startY);
            float stopX = startX;
            float stopY = mDeviceHeight;
            PointF stopP = new PointF(stopX, stopY);
            VerticalGrid vg = new VerticalGrid(startP, stopP);
            mGridV[i] = vg;
        }
        // draw horizontal lines..
        for (int i = 0; i < mNumHorizontalBlocks; i++) {
            float startX = 0.0f;
            float startY = (mBlockSize * i);
            PointF startP = new PointF(startX, startY);
            float stopX = mDeviceWidth;
            float stopY = startY;
            PointF stopP = new PointF(stopX, stopY);
            HorizontalGrid hg = new HorizontalGrid(startP, stopP);
            mGridH[i] = hg;
        }
    }

    protected void resetMove() {
        mMoveBottom = mMoveLeft = mMoveRight = mMoveTop = false;
    }

    protected void drawVerticalGrid() {
        this.drawGrid(mGridV);
    }

    protected void drawHorizontalGrid() {
        this.drawGrid(mGridH);
    }

    protected void drawGrid(Grid[] grid) {
        int numGrids = grid.length;
        for (int i = 0; i < numGrids; i++) {
            PointF start = grid[i].getStart();
            PointF stop = grid[i].getStop();
            mCanvas.drawLine(start.x, start.y, stop.x, stop.y, mPaintGrid);
        }
    }
}
