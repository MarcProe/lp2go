package org.librepilot.lp2go.ui.pfd;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


/**
 * Created by Marcus on 01.06.2016.
 */
public class PfdRollPitchView extends View {

    private boolean isInitialized;
    private long mDiag;
    private Paint mEarthPaint;
    private Rect mEarthRect;
    private int mHalfDiag;
    private int mHorizon;
    private Paint mHorizonPaint;
    private Rect mHorizonRect;
    private int mMiddle;
    private int mPitch;
    private float mRoll;
    private Path mRollScaleBottomTrianglePath;
    private Paint mRollScaleCrescentPaint;
    private RectF mRollScaleOval;
    //private Path mRollScaleCrescentPath;
    private Paint mRollScalePaint;
    private PointF mRollScaleTick225EndPoint;
    private PointF mRollScaleTick225StartPoint;
    private PointF mRollScaleTick240EndPoint;
    private PointF mRollScaleTick240StartPoint;
    private PointF mRollScaleTick245EndPoint;
    private PointF mRollScaleTick245StartPoint;
    private PointF mRollScaleTick250EndPoint;
    private PointF mRollScaleTick250StartPoint;
    private PointF mRollScaleTick255EndPoint;
    private PointF mRollScaleTick255StartPoint;
    private PointF mRollScaleTick260EndPoint;
    private PointF mRollScaleTick260StartPoint;
    private PointF mRollScaleTick265EndPoint;
    private PointF mRollScaleTick265StartPoint;
    private PointF mRollScaleTick275EndPoint;
    private PointF mRollScaleTick275StartPoint;
    private PointF mRollScaleTick280EndPoint;
    private PointF mRollScaleTick280StartPoint;
    private PointF mRollScaleTick285EndPoint;
    private PointF mRollScaleTick285StartPoint;
    private PointF mRollScaleTick290EndPoint;
    private PointF mRollScaleTick290StartPoint;
    private PointF mRollScaleTick295EndPoint;
    private PointF mRollScaleTick295StartPoint;
    private PointF mRollScaleTick300EndPoint;
    private PointF mRollScaleTick300StartPoint;
    private PointF mRollScaleTick315EndPoint;
    private PointF mRollScaleTick315StartPoint;
    private Path mRollScaleTopTrianglePath;
    private Paint mSkyPaint;
    private Rect mSkyRect;
    private Paint mUiPaint;


    public PfdRollPitchView(Context context) {
        super(context);
    }

    public PfdRollPitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PfdRollPitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPitch(float pitch) {
        this.mPitch = Math.round(pitch * (mHorizon / 90));
    }

    public void setRoll(float roll) {
        this.mRoll = -roll;
    }

    private void init() {

        mRoll = 0;
        mPitch = 0;
        //the length are just lazyness to do the real math, will calculate this stuff correctly later.

        mHorizon = Math.round(this.getHeight() / 2f + .5f);
        mMiddle = Math.round(this.getWidth() / 2f + .5f);
        mDiag = Math.round(Math.sqrt(Math.pow(mHorizon, 2) + Math.pow(mMiddle, 2)));
        mHalfDiag = Math.round(mDiag / 2);

        //draw earth
        mEarthRect = new Rect(-mHalfDiag, mHorizon + mPitch, mHalfDiag * 4, mHalfDiag * 4);
        mEarthPaint = new Paint();
        mEarthPaint.setColor(Color.argb(0xff, 0x65, 0x33, 0x00));

        //draw sky
        mSkyRect = new Rect(-mHalfDiag, -mHalfDiag, mHalfDiag * 4, mHorizon + mPitch);
        mSkyPaint = new Paint();
        mSkyPaint.setColor(Color.argb(0xff, 0x01, 0x64, 0xcc));

        //draw horizon
        mHorizonRect = new Rect(-mHalfDiag, (mHorizon - 2) + mPitch, mHalfDiag * 4,
                (mHorizon + 2) + mPitch);
        mHorizonPaint = new Paint();
        mHorizonPaint.setColor(Color.argb(0xff, 0xff, 0xff, 0xff));

        mUiPaint = new Paint();
        mUiPaint.setColor(Color.argb(0xff, 0xff, 0xff, 0xff));
        mUiPaint.setStrokeWidth(5);


        //draw RollScale
        int radius = Math.round(mMiddle / 2);

        mRollScaleCrescentPaint = new Paint();
        mRollScaleCrescentPaint.setColor(Color.WHITE);
        mRollScaleCrescentPaint.setStrokeWidth(4);
        mRollScaleCrescentPaint.setStyle(Paint.Style.STROKE);

        //rollscale half circle
        mRollScaleOval = new RectF();
        mRollScaleOval
                .set(mMiddle - radius, mHorizon - radius, mMiddle + radius, mHorizon + radius);

        //rollscale center triangle
        mRollScalePaint = new Paint();
        mRollScalePaint.setColor(Color.WHITE);
        mRollScalePaint.setStrokeWidth(4);
        mRollScalePaint.setStyle(Paint.Style.FILL);

        mRollScaleTopTrianglePath = new Path();
        mRollScaleTopTrianglePath.moveTo(mMiddle, mHorizon - radius);
        mRollScaleTopTrianglePath
                .lineTo(mMiddle - (mMiddle * .04f), mHorizon - radius - (mMiddle * .06f));
        mRollScaleTopTrianglePath
                .lineTo(mMiddle + (mMiddle * .04f), mHorizon - radius - (mMiddle * .06f));
        mRollScaleTopTrianglePath.lineTo(mMiddle, mHorizon - radius);

        //ticks
        float bigTickHeight = mMiddle * 0.06f;
        float smallTickHeight = mMiddle * 0.03f;

        mRollScaleTick225StartPoint = getPosition(mHorizon, mMiddle, radius, 225);
        mRollScaleTick225EndPoint = getPosition(mHorizon, mMiddle, radius + bigTickHeight, 225);

        mRollScaleTick240StartPoint = getPosition(mHorizon, mMiddle, radius, 240);
        mRollScaleTick240EndPoint = getPosition(mHorizon, mMiddle, radius + bigTickHeight, 240);

        mRollScaleTick245StartPoint = getPosition(mHorizon, mMiddle, radius, 245);
        mRollScaleTick245EndPoint = getPosition(mHorizon, mMiddle, radius + smallTickHeight, 245);

        mRollScaleTick250StartPoint = getPosition(mHorizon, mMiddle, radius, 250);
        mRollScaleTick250EndPoint = getPosition(mHorizon, mMiddle, radius + smallTickHeight, 250);

        mRollScaleTick255StartPoint = getPosition(mHorizon, mMiddle, radius, 255);
        mRollScaleTick255EndPoint = getPosition(mHorizon, mMiddle, radius + bigTickHeight, 255);

        mRollScaleTick260StartPoint = getPosition(mHorizon, mMiddle, radius, 260);
        mRollScaleTick260EndPoint = getPosition(mHorizon, mMiddle, radius + smallTickHeight, 260);

        mRollScaleTick265StartPoint = getPosition(mHorizon, mMiddle, radius, 265);
        mRollScaleTick265EndPoint = getPosition(mHorizon, mMiddle, radius + smallTickHeight, 265);

        //on 270 is the triangle

        mRollScaleTick275StartPoint = getPosition(mHorizon, mMiddle, radius, 275);
        mRollScaleTick275EndPoint = getPosition(mHorizon, mMiddle, radius + smallTickHeight, 275);

        mRollScaleTick280StartPoint = getPosition(mHorizon, mMiddle, radius, 280);
        mRollScaleTick280EndPoint = getPosition(mHorizon, mMiddle, radius + smallTickHeight, 280);

        mRollScaleTick285StartPoint = getPosition(mHorizon, mMiddle, radius, 285);
        mRollScaleTick285EndPoint = getPosition(mHorizon, mMiddle, radius + bigTickHeight, 285);

        mRollScaleTick290StartPoint = getPosition(mHorizon, mMiddle, radius, 290);
        mRollScaleTick290EndPoint = getPosition(mHorizon, mMiddle, radius + smallTickHeight, 290);

        mRollScaleTick295StartPoint = getPosition(mHorizon, mMiddle, radius, 295);
        mRollScaleTick295EndPoint = getPosition(mHorizon, mMiddle, radius + smallTickHeight, 295);

        mRollScaleTick300StartPoint = getPosition(mHorizon, mMiddle, radius, 300);
        mRollScaleTick300EndPoint = getPosition(mHorizon, mMiddle, radius + bigTickHeight, 300);

        mRollScaleTick315StartPoint = getPosition(mHorizon, mMiddle, radius, 315);
        mRollScaleTick315EndPoint = getPosition(mHorizon, mMiddle, radius + bigTickHeight, 315);

        //rollscale bottom triangle
        mRollScaleBottomTrianglePath = new Path();
        mRollScaleBottomTrianglePath.moveTo(mMiddle, mHorizon - radius);
        mRollScaleBottomTrianglePath
                .lineTo(mMiddle - (mMiddle * .02f), mHorizon - radius + (mMiddle * .04f));
        mRollScaleBottomTrianglePath
                .lineTo(mMiddle + (mMiddle * .02f), mHorizon - radius + (mMiddle * .04f));
        mRollScaleBottomTrianglePath.lineTo(mMiddle, mHorizon - radius);

        isInitialized = true;

    }

    private PointF getPosition(int cy, int cx, float rad, float angle) {

        PointF p = new PointF((float) (cx + rad * Math.cos(Math.toRadians(angle))),
                (float) (cy + rad * Math.sin(Math.toRadians(angle))));

        return p;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isInitialized) {
            init();
        }

        canvas.save();
        canvas.rotate(mRoll, mMiddle, mHorizon);

        //everything that pitches and rolls
        mSkyRect.set(-mHalfDiag, -mHalfDiag, mHalfDiag * 4, mHorizon + mPitch);
        canvas.drawRect(mSkyRect, mSkyPaint);

        mEarthRect.set(-mHalfDiag, mHorizon + mPitch, mHalfDiag * 4, mHalfDiag * 4);
        canvas.drawRect(mEarthRect, mEarthPaint);

        mHorizonRect
                .set(-mHalfDiag, (mHorizon - 2) + mPitch, mHalfDiag * 4, (mHorizon + 2) + mPitch);
        canvas.drawRect(mHorizonRect, mHorizonPaint);

        //everything that just rolls
        canvas.drawArc(mRollScaleOval, 225, 90, false, mRollScaleCrescentPaint);

        canvas.drawPath(mRollScaleTopTrianglePath, mRollScalePaint);

        canvas.drawLine(mRollScaleTick225StartPoint.x, mRollScaleTick225StartPoint.y,
                mRollScaleTick225EndPoint.x, mRollScaleTick225EndPoint.y, mRollScalePaint);

        canvas.drawLine(mRollScaleTick240StartPoint.x, mRollScaleTick240StartPoint.y,
                mRollScaleTick240EndPoint.x, mRollScaleTick240EndPoint.y, mRollScalePaint);

        canvas.drawLine(mRollScaleTick245StartPoint.x, mRollScaleTick245StartPoint.y,
                mRollScaleTick245EndPoint.x, mRollScaleTick245EndPoint.y, mRollScalePaint);
        canvas.drawLine(mRollScaleTick250StartPoint.x, mRollScaleTick250StartPoint.y,
                mRollScaleTick250EndPoint.x, mRollScaleTick250EndPoint.y, mRollScalePaint);

        canvas.drawLine(mRollScaleTick255StartPoint.x, mRollScaleTick255StartPoint.y,
                mRollScaleTick255EndPoint.x, mRollScaleTick255EndPoint.y, mRollScalePaint);

        canvas.drawLine(mRollScaleTick260StartPoint.x, mRollScaleTick260StartPoint.y,
                mRollScaleTick260EndPoint.x, mRollScaleTick260EndPoint.y, mRollScalePaint);
        canvas.drawLine(mRollScaleTick265StartPoint.x, mRollScaleTick265StartPoint.y,
                mRollScaleTick265EndPoint.x, mRollScaleTick265EndPoint.y, mRollScalePaint);

        //on 270 is the triangle

        canvas.drawLine(mRollScaleTick275StartPoint.x, mRollScaleTick275StartPoint.y,
                mRollScaleTick275EndPoint.x, mRollScaleTick275EndPoint.y, mRollScalePaint);
        canvas.drawLine(mRollScaleTick280StartPoint.x, mRollScaleTick280StartPoint.y,
                mRollScaleTick280EndPoint.x, mRollScaleTick280EndPoint.y, mRollScalePaint);

        canvas.drawLine(mRollScaleTick285StartPoint.x, mRollScaleTick285StartPoint.y,
                mRollScaleTick285EndPoint.x, mRollScaleTick285EndPoint.y, mRollScalePaint);

        canvas.drawLine(mRollScaleTick290StartPoint.x, mRollScaleTick290StartPoint.y,
                mRollScaleTick290EndPoint.x, mRollScaleTick290EndPoint.y, mRollScalePaint);
        canvas.drawLine(mRollScaleTick295StartPoint.x, mRollScaleTick295StartPoint.y,
                mRollScaleTick295EndPoint.x, mRollScaleTick295EndPoint.y, mRollScalePaint);

        canvas.drawLine(mRollScaleTick300StartPoint.x, mRollScaleTick300StartPoint.y,
                mRollScaleTick300EndPoint.x, mRollScaleTick300EndPoint.y, mRollScalePaint);

        canvas.drawLine(mRollScaleTick315StartPoint.x, mRollScaleTick315StartPoint.y,
                mRollScaleTick315EndPoint.x, mRollScaleTick315EndPoint.y, mRollScalePaint);

        canvas.restore();

        //now everything that does not move
        canvas.drawPath(mRollScaleBottomTrianglePath, mRollScalePaint);

    }
}
