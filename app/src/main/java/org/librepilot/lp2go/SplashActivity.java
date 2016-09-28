/*
 *
 * Copyright
 *
 * @file   SplashActivity.java
 * @author The LibrePilot Project, http://www.librepilot.org Copyright (C) 2016.
 * @see    The GNU Public License (GPL) Version 3
 */

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

package org.librepilot.lp2go;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import org.librepilot.lp2go.helper.H;

public class SplashActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        DisplayMetrics displayMetrics = this.getApplicationContext().getResources().getDisplayMetrics();

        float dpWidth = (displayMetrics.widthPixels / displayMetrics.density) * 0.9f;

        VisualLog.d("Splash: " + dpWidth);

        ImageView imgSplash = (ImageView) findViewById(R.id.imgSplash);
        imgSplash.setImageBitmap(H.drawableToBitmap(R.drawable.splash,
                Math.round(dpWidth), Math.round(dpWidth * 0.3f), this.getApplicationContext()));
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        imgSplash.startAnimation(pulse);

        final int SPLASH_DISPLAY_LENGTH = 1200;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}