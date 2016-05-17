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

import android.content.Intent;
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
    }

    public boolean isEnabled() {
        return this.mEnabled;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public void checkForTTS() {
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        mActivity.startActivityForResult(checkIntent, MainActivity.CALLBACK_TTS);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.CALLBACK_TTS) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                mTts = new TextToSpeech(mActivity, this);
            } else {
                //Intent installIntent = new Intent();
                //installIntent.setAction(
                //        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                //mActivity.startActivity(installIntent);

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

    public void speakFlush(String text) {
        if (mEnabled && mTts != null && text != null) {
            mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
