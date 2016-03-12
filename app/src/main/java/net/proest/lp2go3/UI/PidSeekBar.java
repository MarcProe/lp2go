package net.proest.lp2go3.UI;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DecimalFormat;

/**
 * Created by Marcus on 11.03.2016.
 */
public class PidSeekBar extends SeekBar implements SeekBar.OnSeekBarChangeListener, SeekBar.OnTouchListener, View.OnClickListener {

    private TextView mSeekBarProgress;
    private int mDenominator;
    private int mStep;
    private String mDecimalFormatString;
    private ImageView mLock;
    private boolean mLockOpen;
    private ImageView mPlus;
    private ImageView mMinus;

    public PidSeekBar(Context context) {
        super(context);
    }

    public PidSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PidSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mLockOpen) {
            int p = seekBar.getProgress();

            if (p % mStep != 0) {
                p = p - p % mStep;
            }

            double v = (double) p / mDenominator;

            mSeekBarProgress.setText(getDecimalString(v));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void init(TextView textView, ImageView lock, ImageView plus, ImageView minus, int denominator, int max, int step, String dfs) {
        this.mDenominator = denominator;
        this.mStep = step;
        this.mSeekBarProgress = textView;
        this.mDecimalFormatString = dfs;

        this.setProgress(0);
        this.setMax(max);
        this.setOnSeekBarChangeListener(this);

        this.mLock = lock;
        this.mPlus = plus;
        this.mMinus = minus;

        mLock.setOnClickListener(this);
        mPlus.setOnClickListener(this);
        mMinus.setOnClickListener(this);

        mLockOpen = false;

        onProgressChanged(this, 0, false);
        //this.setProgress(0d);

        mPlus.setColorFilter(Color.argb(0xff, 0xaa, 0xaa, 0xaa));
        mMinus.setColorFilter(Color.argb(0xff, 0xaa, 0xaa, 0xaa));

        this.setOnTouchListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mLock)) {
            mLockOpen = !mLockOpen;
            if (mLockOpen) {
                mLock.setColorFilter(Color.argb(0xff, 0x00, 0x80, 0x00));
                mPlus.setColorFilter(Color.argb(0xff, 0x00, 0x00, 0x00));
                mMinus.setColorFilter(Color.argb(0xff, 0x00, 0x00, 0x00));
            } else {
                mLock.setColorFilter(Color.argb(0xff, 0xd4, 0x00, 0x00));
                mPlus.setColorFilter(Color.argb(0xff, 0xaa, 0xaa, 0xaa));
                mMinus.setColorFilter(Color.argb(0xff, 0xaa, 0xaa, 0xaa));
            }
        } else if (mLockOpen && v.equals(mPlus)) {
            int p = this.getProgress() + mStep;
            if (p <= this.getMax()) {
                this.setProgress(p);
            }
        } else if (mLockOpen && v.equals(mMinus)) {
            int p = this.getProgress() - mStep;
            if (p >= 0) {
                this.setProgress(p);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return !mLockOpen;
    }

    private String getDecimalString(double v) {
        DecimalFormat df = new DecimalFormat(mDecimalFormatString);
        return df.format(v);
    }

    /*
    * Set Progress from PollThread, overriding the lock.
     */
    public void setProgress(double p) {
        this.setProgress(Math.round(p * this.mDenominator));
    }
}
