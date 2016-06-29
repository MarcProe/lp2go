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

package org.librepilot.lp2go.helper;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.VisualLog;

import java.util.Locale;

public class TextToSpeechHelper implements TextToSpeech.OnInitListener {

    private MainActivity mActivity;
    private boolean mEnabled = true;
    private TextToSpeech mTts;

    public TextToSpeechHelper(MainActivity mActivity) {
        this.mActivity = mActivity;
        checkForTTS();
        setEnabled(SettingsHelper.mText2SpeechEnabled);
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public void checkForTTS() {
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        try {
            Intent installIntent = new Intent();
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            ResolveInfo resolveInfo = mActivity.getPackageManager()
                    .resolveActivity(installIntent, PackageManager.MATCH_DEFAULT_ONLY);

            if (resolveInfo == null) {
                VisualLog.d("TTS", "Activity not supported");
                mTts = null;
            } else {
                VisualLog.d("TTS", "Calling Activity");
                mActivity.startActivityForResult(checkIntent, MainActivity.CALLBACK_TTS);
            }
        } catch (ActivityNotFoundException e) {
            VisualLog.d("TTS", "TTS call threw ActivityNotFoundException");
            mTts = null;
        }
    }

    public void onActivityResult(int requestCode, int resultCode,
                                 @SuppressWarnings("UnusedParameters") Intent data) {
        if (requestCode == MainActivity.CALLBACK_TTS) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                mTts = new TextToSpeech(mActivity, this);
                VisualLog.d("TTS", "Call successful");
            } else {
                //Intent installIntent = new Intent();
                //installIntent.setAction(
                //        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                //mActivity.startActivity(installIntent);
                VisualLog.d("TTS", "Call successful, but bad result");
                mTts = null;
            }
        }
    }

    @Override
    public void onInit(int status) {
        if (mTts != null) {
            mTts.setLanguage(Locale.US);
            VisualLog.d("TTS", "Instanciated");
        } else {
            VisualLog.d("TTS", "not available");
        }
    }

    private void speak(String text, int mode) {
        if (mEnabled && mTts != null && text != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTts.speak(text, mode, null, null);
            } else {
                //noinspection deprecation
                mTts.speak(text, mode, null);
            }
        }
    }

    public void speakFlush(String text) {
        this.speak(text, TextToSpeech.QUEUE_FLUSH);
    }
}
