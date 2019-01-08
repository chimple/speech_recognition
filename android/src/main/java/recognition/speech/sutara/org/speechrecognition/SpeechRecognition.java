package recognition.speech.sutara.org.speechrecognition;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.Locale;

import static android.content.ContentValues.TAG;

public class SpeechRecognition {

    static final int MAX_RESULT_COUNT = 3;
    private Locale locale;
    private Context context;
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;

    private OnSpeechRecognitionListener onSpeechRecognitionListener;
    private boolean enableOnlyOfflineRecognition = false;

    public SpeechRecognition(Context context) {
        this.context = context;
        initializeSpeechRecognitionParameters();
    }

    public void setContext(Context context) {
        this.context = context;
        initializeSpeechRecognitionParameters();
    }

    public void setSpeechRecognitionListener(OnSpeechRecognitionListener onSpeechRecognitionListener) {
        this.onSpeechRecognitionListener = onSpeechRecognitionListener;
    }

    public void setPreferredLanguage(Locale locale) {
        this.locale = locale;
    }

    public void useOnlyOfflineRecognition(boolean onlyOfflineRecognition) {
        this.enableOnlyOfflineRecognition = onlyOfflineRecognition;
    }

    /**
     * Checks if speech recognition is supported on the device.
     * If you try to use {@link SpeechRecognition} when your device does not support it,
     * {@link IllegalStateException} will be thrown
     */
    public boolean isSpeechRecognitionAvailable() {
        return SpeechRecognitionUtilities.isSpeechRecognitionEnabled(context);
    }

    public void startSpeechRecognition(Locale locale) {
        Log.d(TAG, "locale country in startSpeechRecognition:" + locale.getCountry());
        Log.d(TAG, "locale language in startSpeechRecognition:" + locale.getLanguage());
        this.setPreferredLanguage(locale);
        SpeechRecognitionListener speechRecognitionListener = new SpeechRecognitionListener(
                this.onSpeechRecognitionListener, context);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, this.locale);
        speechRecognizer.setRecognitionListener(speechRecognitionListener);
        speechRecognizer.startListening(recognizerIntent);
    }

    public void stopSpeechRecognition() {
        onSpeechRecognitionListener.OnSpeechRecognitionStopped();
        speechRecognizer.stopListening();
    }

    public void destroy() {
        if(speechRecognizer != null) {
            Log.d(TAG, "speechRecognizer destroyed");
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }


    public SpeechRecognizer getSpeechRecognizer() {
        return this.speechRecognizer;
    }

    public void initializeSpeechRecognitionParameters() {
        Log.d(TAG, "speechRecognizer initialized ...");
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MAX_RESULT_COUNT);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 100000);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 200000);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 250000);
        /*
         * Only offline recognition works from API level 23
         */
        if (enableOnlyOfflineRecognition) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d(TAG, "adding EXTRA_PREFER_OFFLINE....");
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
            }
        }
    }
}