/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.librepilot.lp2go.ui.pfd;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class PfdRollPitchView extends View {

    final int VIEWPORT_HEIGHT_DIVISOR = 45;
    private final Rect textBounds = new Rect();
    private boolean isInitialized;
    private Path mDashedPitchPath;
    private Paint mEarthPaint;
    private Rect mEarthRect;
    private Paint mFatHorizonPaint;
    private int mHalfDiag;
    private int mHorizon;
    private float mHorizonDivisor;
    private Paint mHorizonPaint;
    private Rect mHorizonRect;
    private int mMiddle;
    private int mPitch;
    private int mRadius;
    private float mRawPitch;
    private float mRoll;
    private Path mRollScaleBottomTrianglePath;
    private Paint mRollScaleCrescentPaint;
    private RectF mRollScaleOval;
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
    private Paint mUiStrokedPaint;
    private Path mStaticTriangleBrightPath;
    private Paint mStaticHorizonBrightPaint;
    private Paint mStaticHorizonDarkPaint;
    private Path mStaticTriangleDarkPath;
    private Path mStaticLeftBarBrightPath;
    private Path mStaticLeftBarDarkPath;
    private Path mStaticRightBarBrightPath;
    private Path mStaticRightBarDarkPath;

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
        this.mRawPitch = pitch;
        this.mPitch = Math.round(pitch * (mHorizon / VIEWPORT_HEIGHT_DIVISOR));
    }

    public void setRoll(float roll) {
        this.mRoll = -roll;
    }

    private void init() {
        //the length are just lazyness to do the real math, will calculate this stuff correctly later.
        //maybe.

        mHorizon = Math.round(this.getHeight() / 2f + .5f);
        mMiddle = Math.round(this.getWidth() / 2f + .5f);

        final long diag = Math.round(Math.sqrt(Math.pow(mHorizon, 2) + Math.pow(mMiddle, 2)));

        mHalfDiag = Math.round(diag / 2);
        mHorizonDivisor = mHorizon / VIEWPORT_HEIGHT_DIVISOR;

        setPitch(0);
        setRoll(0);

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
        mUiPaint.setColor(Color.WHITE);
        mUiPaint.setStrokeWidth(5);
        mUiPaint.setTextSize(mHorizon / 17);

        mUiStrokedPaint = new Paint();
        mUiStrokedPaint.setColor(Color.WHITE);
        mUiStrokedPaint.setStrokeWidth(5);
        mUiStrokedPaint.setStyle(Paint.Style.STROKE);
        mUiStrokedPaint.setPathEffect(new DashPathEffect(new float[]{10, 2}, 0));

        //draw RollScale
        mRadius = Math.round(mHorizon / 2);

        mRollScaleCrescentPaint = new Paint();
        mRollScaleCrescentPaint.setColor(Color.WHITE);
        mRollScaleCrescentPaint.setStrokeWidth(4);
        mRollScaleCrescentPaint.setStyle(Paint.Style.STROKE);

        //rollscale half circle
        mRollScaleOval = new RectF();
        mRollScaleOval.set(mMiddle - mRadius, mHorizon - mRadius,
                mMiddle + mRadius, mHorizon + mRadius);

        //rollscale center triangle
        mRollScalePaint = new Paint();
        mRollScalePaint.setColor(Color.WHITE);
        mRollScalePaint.setStrokeWidth(4);
        mRollScalePaint.setStyle(Paint.Style.FILL);

        mRollScaleTopTrianglePath = new Path();
        mRollScaleTopTrianglePath.moveTo(mMiddle, mHorizon - mRadius);
        mRollScaleTopTrianglePath
                .lineTo(mMiddle - (mMiddle * .04f), mHorizon - mRadius - (mMiddle * .06f));
        mRollScaleTopTrianglePath
                .lineTo(mMiddle + (mMiddle * .04f), mHorizon - mRadius - (mMiddle * .06f));
        mRollScaleTopTrianglePath.lineTo(mMiddle, mHorizon - mRadius);

        //ticks
        float bigTickHeight = mMiddle * 0.06f;
        float smallTickHeight = mMiddle * 0.03f;

        mRollScaleTick225StartPoint = getPosition(mHorizon, mMiddle, mRadius, 225);
        mRollScaleTick225EndPoint = getPosition(mHorizon, mMiddle, mRadius + bigTickHeight, 225);

        mRollScaleTick240StartPoint = getPosition(mHorizon, mMiddle, mRadius, 240);
        mRollScaleTick240EndPoint = getPosition(mHorizon, mMiddle, mRadius + bigTickHeight, 240);

        mRollScaleTick245StartPoint = getPosition(mHorizon, mMiddle, mRadius, 245);
        mRollScaleTick245EndPoint = getPosition(mHorizon, mMiddle, mRadius + smallTickHeight, 245);

        mRollScaleTick250StartPoint = getPosition(mHorizon, mMiddle, mRadius, 250);
        mRollScaleTick250EndPoint = getPosition(mHorizon, mMiddle, mRadius + smallTickHeight, 250);

        mRollScaleTick255StartPoint = getPosition(mHorizon, mMiddle, mRadius, 255);
        mRollScaleTick255EndPoint = getPosition(mHorizon, mMiddle, mRadius + bigTickHeight, 255);

        mRollScaleTick260StartPoint = getPosition(mHorizon, mMiddle, mRadius, 260);
        mRollScaleTick260EndPoint = getPosition(mHorizon, mMiddle, mRadius + smallTickHeight, 260);

        mRollScaleTick265StartPoint = getPosition(mHorizon, mMiddle, mRadius, 265);
        mRollScaleTick265EndPoint = getPosition(mHorizon, mMiddle, mRadius + smallTickHeight, 265);

        //on 270 is the triangle

        mRollScaleTick275StartPoint = getPosition(mHorizon, mMiddle, mRadius, 275);
        mRollScaleTick275EndPoint = getPosition(mHorizon, mMiddle, mRadius + smallTickHeight, 275);

        mRollScaleTick280StartPoint = getPosition(mHorizon, mMiddle, mRadius, 280);
        mRollScaleTick280EndPoint = getPosition(mHorizon, mMiddle, mRadius + smallTickHeight, 280);

        mRollScaleTick285StartPoint = getPosition(mHorizon, mMiddle, mRadius, 285);
        mRollScaleTick285EndPoint = getPosition(mHorizon, mMiddle, mRadius + bigTickHeight, 285);

        mRollScaleTick290StartPoint = getPosition(mHorizon, mMiddle, mRadius, 290);
        mRollScaleTick290EndPoint = getPosition(mHorizon, mMiddle, mRadius + smallTickHeight, 290);

        mRollScaleTick295StartPoint = getPosition(mHorizon, mMiddle, mRadius, 295);
        mRollScaleTick295EndPoint = getPosition(mHorizon, mMiddle, mRadius + smallTickHeight, 295);

        mRollScaleTick300StartPoint = getPosition(mHorizon, mMiddle, mRadius, 300);
        mRollScaleTick300EndPoint = getPosition(mHorizon, mMiddle, mRadius + bigTickHeight, 300);

        mRollScaleTick315StartPoint = getPosition(mHorizon, mMiddle, mRadius, 315);
        mRollScaleTick315EndPoint = getPosition(mHorizon, mMiddle, mRadius + bigTickHeight, 315);

        //rollscale bottom triangle
        mRollScaleBottomTrianglePath = new Path();
        mRollScaleBottomTrianglePath.moveTo(mMiddle, mHorizon - mRadius);
        mRollScaleBottomTrianglePath
                .lineTo(mMiddle - (mMiddle * .02f), mHorizon - mRadius + (mMiddle * .04f));
        mRollScaleBottomTrianglePath
                .lineTo(mMiddle + (mMiddle * .02f), mHorizon - mRadius + (mMiddle * .04f));
        mRollScaleBottomTrianglePath.lineTo(mMiddle, mHorizon - mRadius);

        //fat horizon line
        mFatHorizonPaint = new Paint();
        mFatHorizonPaint.setColor(Color.argb(0xff, 0xff, 0xff, 0xff));
        mFatHorizonPaint.setStrokeWidth(8);

        mDashedPitchPath = new Path();

        //bright yellow triangle
        mStaticHorizonBrightPaint = new Paint();
        mStaticHorizonBrightPaint.setColor(Color.argb(0xff, 0xff, 0xff, 0x0));
        mStaticHorizonBrightPaint.setStrokeWidth(8);

        mStaticTriangleBrightPath = new Path();
        mStaticTriangleBrightPath.moveTo(mMiddle, mHorizon);
        mStaticTriangleBrightPath.lineTo(mMiddle - (mMiddle * .15f), mHorizon + (mMiddle * .1f));
        mStaticTriangleBrightPath.lineTo(mMiddle, mHorizon + (mMiddle * .04f));
        mStaticTriangleBrightPath.lineTo(mMiddle + (mMiddle * .15f), mHorizon + (mMiddle * .1f));
        mStaticTriangleBrightPath.lineTo(mMiddle, mHorizon);

        mStaticLeftBarBrightPath = new Path();
        mStaticLeftBarBrightPath.moveTo(mMiddle - mRadius * .55f, mHorizon);
        mStaticLeftBarBrightPath.lineTo(mMiddle - mRadius * .6f, mHorizon * 0.98f);
        mStaticLeftBarBrightPath.lineTo(mMiddle - mRadius, mHorizon * 0.98f);
        mStaticLeftBarBrightPath.lineTo(mMiddle - mRadius, mHorizon);
        mStaticLeftBarBrightPath.lineTo(mMiddle - mRadius * .55f, mHorizon);

        mStaticLeftBarDarkPath = new Path();
        mStaticLeftBarDarkPath.moveTo(mMiddle - mRadius * .55f, mHorizon);
        mStaticLeftBarDarkPath.lineTo(mMiddle - mRadius * .6f, mHorizon * 1.02f);
        mStaticLeftBarDarkPath.lineTo(mMiddle - mRadius, mHorizon * 1.02f);
        mStaticLeftBarDarkPath.lineTo(mMiddle - mRadius, mHorizon);
        mStaticLeftBarDarkPath.lineTo(mMiddle - mRadius * .55f, mHorizon);

        //dark yellow triangle
        mStaticHorizonDarkPaint = new Paint();
        mStaticHorizonDarkPaint.setColor(Color.argb(0xff, 0x80, 0x80, 0x0));
        mStaticHorizonDarkPaint.setStrokeWidth(8);

        mStaticTriangleDarkPath = new Path();
        mStaticTriangleDarkPath.moveTo(mMiddle, mHorizon + (mMiddle * .04f));
        mStaticTriangleDarkPath.lineTo(mMiddle - (mMiddle * .15f), mHorizon + (mMiddle * .1f));
        mStaticTriangleDarkPath.lineTo(mMiddle, mHorizon + (mMiddle * .06f));
        mStaticTriangleDarkPath.lineTo(mMiddle + (mMiddle * .15f), mHorizon + (mMiddle * .1f));
        mStaticTriangleDarkPath.lineTo(mMiddle, mHorizon + (mMiddle * .04f));

        mStaticRightBarBrightPath = new Path();
        mStaticRightBarBrightPath.moveTo(mMiddle + mRadius * .55f, mHorizon);
        mStaticRightBarBrightPath.lineTo(mMiddle + mRadius * .6f, mHorizon * 0.98f);
        mStaticRightBarBrightPath.lineTo(mMiddle + mRadius, mHorizon * 0.98f);
        mStaticRightBarBrightPath.lineTo(mMiddle + mRadius, mHorizon);
        mStaticRightBarBrightPath.lineTo(mMiddle + mRadius * .55f, mHorizon);

        mStaticRightBarDarkPath = new Path();
        mStaticRightBarDarkPath.moveTo(mMiddle + mRadius * .55f, mHorizon);
        mStaticRightBarDarkPath.lineTo(mMiddle + mRadius * .6f, mHorizon * 1.02f);
        mStaticRightBarDarkPath.lineTo(mMiddle + mRadius, mHorizon * 1.02f);
        mStaticRightBarDarkPath.lineTo(mMiddle + mRadius, mHorizon);
        mStaticRightBarDarkPath.lineTo(mMiddle + mRadius * .55f, mHorizon);

        isInitialized = true;
    }

    private PointF getPosition(int cy, int cx, float rad, float angle) {
        return new PointF((float) (cx + rad * Math.cos(Math.toRadians(angle))),
                (float) (cy + rad * Math.sin(Math.toRadians(angle))));
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

        //fatHorizon
        canvas.drawLine(mMiddle - mRadius * .55f, mHorizon + mPitch,
                mMiddle + mRadius * .55f, mHorizon + mPitch, mFatHorizonPaint);

        //pitch lines
        final float mPitchScaleOffset = mRawPitch % 10;
        final float radLineOffset = mRadius - mRadius * 0.1f;

        //only draw top 3 lines if in the circle
        if (radLineOffset > ((30 - mPitchScaleOffset) * (mHorizonDivisor))) {
            drawLine(.4f, -30, canvas);
        }

        if (radLineOffset > ((25 - mPitchScaleOffset) * (mHorizonDivisor))) {
            drawLine(.2f, -25, canvas);
        }
        if (radLineOffset > ((20 - mPitchScaleOffset) * (mHorizonDivisor))) {
            drawLine(.4f, -20, canvas);
        }

        drawLine(.2f, -15, canvas);
        drawLine(.4f, -10, canvas);
        drawLine(.2f, -5, canvas);
        drawLine(.4f, 0, canvas);
        drawLine(.2f, 5, canvas);
        drawLine(.4f, 10, canvas);
        drawLine(.2f, 15, canvas);

        //only draw the bottom 3 lines if in the circle (which is invisible on the bottom)
        if (radLineOffset > ((20 + mPitchScaleOffset) * (mHorizonDivisor))) {
            drawLine(.4f, 20, canvas);
        }

        if (radLineOffset > ((25 + mPitchScaleOffset) * (mHorizonDivisor))) {
            drawLine(.2f, 25, canvas);
        }

        if (radLineOffset > ((30 + mPitchScaleOffset) * (mHorizonDivisor))) {
            drawLine(.4f, 30, canvas);
        }

        //all dashed lines are drawn here; see this.drawLine() for details
        canvas.drawPath(mDashedPitchPath, mUiStrokedPaint);
        mDashedPitchPath.reset();

        canvas.restore();

        //draw everything that does not move
        canvas.drawPath(mRollScaleBottomTrianglePath, mRollScalePaint);

        canvas.drawPath(mStaticTriangleBrightPath, mStaticHorizonBrightPaint);
        canvas.drawPath(mStaticTriangleDarkPath, mStaticHorizonDarkPaint);

        canvas.drawPath(mStaticLeftBarBrightPath, mStaticHorizonBrightPaint);
        canvas.drawPath(mStaticLeftBarDarkPath, mStaticHorizonDarkPaint);

        canvas.drawPath(mStaticRightBarBrightPath, mStaticHorizonBrightPaint);
        canvas.drawPath(mStaticRightBarDarkPath, mStaticHorizonDarkPaint);

    }

    private void drawLine(float lenFactor, int offset, Canvas canvas) {
        final float pitchScaleOffset = mRawPitch % 10;
        final float fromX = mMiddle - mRadius * lenFactor;
        final float y = mHorizon + ((offset + pitchScaleOffset) * (mHorizonDivisor));
        final float toX = mMiddle + mRadius * lenFactor;

        if (y < mHorizon + mPitch) {
            canvas.drawLine(fromX, y, toX, y, mUiPaint);
        } else {
            //we want a dashed line here, drawLine does not
            //support this with GPU support on some versions. so we'll use a path.
            //We'll put all lines in one path and draw it in onDraw();

            //TODO: maybe better use drawLines() (with "s") and draw a line for each dash. drawLines() is running on the GPU, drawPath on the CPU
            mDashedPitchPath.moveTo(fromX, y);
            mDashedPitchPath.lineTo(toX, y);
        }

        final double d = (Math.abs(Math.round(
                (((offset + pitchScaleOffset) * (mHorizonDivisor)) - mPitch) / mHorizonDivisor)));
        final String label = String.format("%s", (int) d);

        if (label != null && d != 0 && d <= 90 && offset % 10 == 0) {
            mUiPaint.getTextBounds(label, 0, label.length(), textBounds);
            canvas.drawText(label, fromX - textBounds.width() - 4, y - textBounds.exactCenterY(),
                    mUiPaint);
            canvas.drawText(label, toX + 2, y - textBounds.exactCenterY(), mUiPaint);
        }
    }
}
