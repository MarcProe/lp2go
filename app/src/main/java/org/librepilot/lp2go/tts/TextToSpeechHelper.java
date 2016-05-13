package org.librepilot.lp2go.tts;

import android.content.Intent;
import android.speech.tts.TextToSpeech;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.VisualLog;

import java.util.Locale;

/**
 * Created by Marcus on 13.05.2016.
 */
public class TextToSpeechHelper implements TextToSpeech.OnInitListener {

    private MainActivity mActivity;
    private TextToSpeech mTts;

    public TextToSpeechHelper(MainActivity mActivity) {
        this.mActivity = mActivity;
    }

    public void checkForTTS() {
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        mActivity.startActivityForResult(checkIntent, MainActivity.CALLBACK_TTS);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.CALLBACK_TTS) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                mTts = new TextToSpeech(mActivity, this);
            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                mActivity.startActivity(installIntent);
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
        if (mTts != null && text != null) {
            mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
