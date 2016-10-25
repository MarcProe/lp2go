/*
 * @file   MultiTouchStickPainterView.java
 * @author The LibrePilot Project, http://www.librepilot.org Copyright (C) 2016.
 * @see    The GNU Public License (GPL) Version 3
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.librepilot.lp2go.ui.manualcontrol;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.helper.H;

public class MultiTouchStickPainterView extends View {

    private static final int DIAMETER = 120;

    private int mLeftPointerId = -1;
    private int mRightPointerId = -1;

    private PointF mLeftPointer = null;
    private PointF mRightPointer = null;

    private Paint mPaint;

    private Paint textPaint;
    private int w;
    private int h;
    private int llb;
    private int lrb;
    private int tb;
    private int bb;
    private int rlb;
    private int rrb;

    private float mLeftHoriPerc;
    private float mLeftVertPerc;

    private float mRightHoriPerc;
    private float mRightVertPerc;
    private StickListener mStickListener;

    public MultiTouchStickPainterView(Context context, AttributeSet attrs, @NonNull StickListener s) {
        super(context, attrs);
        setStickListener(s);
        initView();
    }

    private void initView() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // set painter color to a color you like
        mPaint.setStrokeWidth(20);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(20);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // get pointer index from the event object
        int pointerIndex = event.getActionIndex();

        // get pointer ID
        int pointerId = event.getPointerId(pointerIndex);

        // get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();

        switch (maskedAction) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                try {
                    //check if we are left or right. if there is already a point on the side, cancel the new one.
                    //THERE CAN ONLY BE ONE!
                    if (event.getX(pointerId) > llb && event.getX(pointerId) < lrb && event.getY(pointerId) > tb && event.getY(pointerId) < bb) {
                        if (mLeftPointerId != -1) {
                            return false;
                        }
                        mLeftPointerId = pointerId;
                        mLeftPointer = new PointF(event.getX(pointerId), event.getY(pointerId));
                        if (mLeftPointerId == 0 && mRightPointerId == 0 && event.getPointerCount() == 2) {
                            mRightPointerId = 1;
                        }
                    } else if (event.getX(pointerId) > rlb && event.getX(pointerId) < rrb && event.getY(pointerId) > tb && event.getY(pointerId) < bb) {
                        if (mRightPointerId != -1) {
                            return false;
                        }
                        mRightPointerId = pointerId;
                        mRightPointer = new PointF(event.getX(pointerId), event.getY(pointerId));
                        if (mLeftPointerId == 0 && mRightPointerId == 0 && event.getPointerCount() == 2) {
                            mLeftPointerId = 1;
                        }
                    }
                    VisualLog.d("PointerDown: L " + mLeftPointerId + " R " + mRightPointerId + " " + event.getPointerCount());
                } catch (IllegalArgumentException e) {
                    VisualLog.d("pointerrcrash DOWN");
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {

                if (mLeftPointerId != -1 && event.getX(mLeftPointerId) > llb && event.getX(mLeftPointerId) < lrb && event.getY(mLeftPointerId) > tb && event.getY(mLeftPointerId) < bb) {
                    mLeftPointer.x = event.getX(mLeftPointerId);
                    mLeftPointer.y = event.getY(mLeftPointerId);
                }

                if (mRightPointerId != -1 && event.getX(mRightPointerId) > rlb && event.getX(mRightPointerId) < rrb && event.getY(mRightPointerId) > tb && event.getY(mRightPointerId) < bb) {
                    mRightPointer.x = event.getX(mRightPointerId);
                    mRightPointer.y = event.getY(mRightPointerId);
                }

                calc();

                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {

                if (pointerId == mLeftPointerId) {
                    mLeftPointerId = -1;
                    mLeftPointer.x = w / 4;
                } else if (pointerId == mRightPointerId) {
                    mRightPointerId = -1;
                    mRightPointer.x = w / 4 * 3;
                    mRightPointer.y = h / 2;
                }

                if (mLeftPointerId == 1 && mRightPointerId == -1) {
                    mLeftPointerId = 0;
                }
                if (mLeftPointerId == -1 && mRightPointerId == 1) {
                    mRightPointerId = 0;
                }

                VisualLog.d("PointerUp: L " + mLeftPointerId + " R " + mRightPointerId + " " + event.getPointerCount());

                if (event.getPointerCount() == 1) { //the last one, which is "up'ed" in this event
                    mLeftPointerId = -1;
                    mRightPointerId = -1;
                }

                calc();

                break;

            }
            default: {
                VisualLog.w("Unknown Touch Event: " + maskedAction);
                break;
            }

        }
        invalidate();

        return true;
    }

    private void calc() {
        final float hqsl = ((lrb - llb) / 2);
        mLeftHoriPerc = ((mLeftPointer.x - llb) - hqsl) / hqsl * 100;//x
        final float vqsl = ((bb - tb) / 2);
        mLeftVertPerc = ((mLeftPointer.y - tb) - vqsl) / vqsl * 100;//y

        if (-1 == mLeftPointerId) {
            mLeftPointer.x = w / 4;
        }

        //mStickListener.onLeftChange(mLeftHoriPerc, mLeftVertPerc);

        final float hqsr = ((rrb - rlb) / 2);
        mRightHoriPerc = ((mRightPointer.x - rlb) - hqsr) / hqsr * 100;//x
        final float vqsr = ((bb - tb) / 2);
        mRightVertPerc = ((mRightPointer.y - tb) - vqsr) / vqsr * 100;//y

        if (-1 == mRightPointerId) {
            mRightPointer.x = w / 4 * 3;
            mRightPointer.y = h / 2;
        }

        mStickListener.onChange(mLeftHoriPerc, mLeftVertPerc, mRightHoriPerc, mRightVertPerc);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        canvas.drawText("" + mLeftPointerId + "- " + H.round(mLeftHoriPerc, 2) + " | " + H.round(mLeftVertPerc, 2), w / 4, 30, textPaint);
        mPaint.setColor(Color.RED);
        canvas.drawCircle(mLeftPointer.x, mLeftPointer.y, DIAMETER, mPaint);
        mPaint.setStrokeWidth(w / 100);
        canvas.drawLine(llb, mLeftPointer.y, lrb, mLeftPointer.y, mPaint);
        canvas.drawLine(mLeftPointer.x, tb, mLeftPointer.x, bb, mPaint);

        canvas.drawText("" + mRightPointerId + "- " + H.round(mRightHoriPerc, 2) + " | " + H.round(mRightVertPerc, 2), w / 4 * 3, 30, textPaint);
        mPaint.setColor(Color.BLUE);
        canvas.drawCircle(mRightPointer.x, mRightPointer.y, DIAMETER, mPaint);
        mPaint.setStrokeWidth(w / 100);
        canvas.drawLine(rlb, mRightPointer.y, rrb, mRightPointer.y, mPaint);
        canvas.drawLine(mRightPointer.x, tb, mRightPointer.x, bb, mPaint);

        mPaint.setColor(Color.GRAY);

        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(llb, tb, lrb, bb, mPaint);
        canvas.drawRect(rlb, tb, rrb, bb, mPaint);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
        this.llb = w / 40;
        this.lrb = (w / 40) * 19;

        this.rlb = (w / 40) * 21;
        this.rrb = (w / 40) * 39;

        this.tb = h / 20;
        this.bb = (h / 20) * 19;


        mPaint.setStrokeWidth(w / 100);

        mLeftPointer = new PointF(w / 4, bb);
        mRightPointer = new PointF((w / 4) * 3, h / 2);
        calc();
    }

    public void setStickListener(StickListener s) {
        this.mStickListener = s;
    }

    public interface StickListener {
        void onLeftChange(float lx, float ly);

        void onRightChange(float rx, float ry);

        void onChange(float lx, float ly, float rx, float ry);
    }
}


