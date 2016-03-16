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

    private TextView mTxtSeekBarProgress;
    private int mDenominator;
    private int mStep;
    private String mDecimalFormatString;
    private ImageView mLock;
    private boolean mLockOpen;
    private int mProgress;

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
        if (fromUser && mLockOpen) {
            if (progress < mProgress) {
                this.setProgress(mProgress - mStep);
            } else if (progress > mProgress) {
                this.setProgress(mProgress + mStep);
            }

            int p = seekBar.getProgress();

            if (p % mStep != 0) {
                p = p - p % mStep;
            }

            double v = (double) p / mDenominator;
            mTxtSeekBarProgress.setText(getDecimalString(v));
            mProgress = seekBar.getProgress();

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void init(TextView textView, ImageView lock, int denominator, int max, int step, String dfs) {
        this.mDenominator = denominator;
        this.mStep = step;
        this.mTxtSeekBarProgress = textView;
        this.mDecimalFormatString = dfs;

        this.setProgress(0);
        this.setMax(max);
        this.setOnSeekBarChangeListener(this);

        this.mLock = lock;

        mLock.setOnClickListener(this);

        mLockOpen = false;

        onProgressChanged(this, 0, false);
        //this.setProgress(0d);

        this.setOnTouchListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mLock)) {
            mLockOpen = !mLockOpen;
            if (mLockOpen) {
                mLock.setColorFilter(Color.argb(0xff, 0x00, 0x80, 0x00));
            } else {
                mLock.setColorFilter(Color.argb(0xff, 0xd4, 0x00, 0x00));
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
