package com.example.foo.wormgameapp.grid;

import android.graphics.PointF;

/**
 * Created by foo on 3/26/18.
 */

public class Grid {
    protected PointF mStart;
    protected PointF mStop;

    public Grid(PointF start, PointF stop) {
        mStart = start;
        mStop = stop;
    }

    public PointF getStart() {
        return mStart;
    }

    public void setStart(PointF start) {
        mStart = start;
    }

    public PointF getStop() {
        return mStop;
    }

    public void setStop(PointF stop) {
        mStop = stop;
    }
}
