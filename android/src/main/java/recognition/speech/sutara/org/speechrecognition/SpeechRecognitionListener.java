package recognition.speech.sutara.org.speechrecognition;

import android.content.Context;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class SpeechRecognitionListener implements RecognitionListener {

    private OnSpeechRecognitionListener onSpeechRecognitionListener;
    private Context context;

    SpeechRecognitionListener(OnSpeechRecognitionListener onSpeechRecognizerListener, Context context) {
        this.onSpeechRecognitionListener = onSpeechRecognizerListener;
        this.context = context;
    }

    OnSpeechRecognitionListener getOnSpeechRecognitionListener() {
        return onSpeechRecognitionListener;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.d(TAG, "onReadyForSpeech");
        onSpeechRecognitionListener.onReadyForSpeech(true);
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech");
        onSpeechRecognitionListener.OnSpeechRecognitionStarted();
    }

    @Override
    public void onRmsChanged(float rmsdB) {
//        Log.d(TAG, "onRmsChanged : " + rmsdB);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.d(TAG, "onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech");
    }

    @Override
    public void onError(int i) {
        String errorMessage = "";
        int errorCode = -1;

        switch (i) {
            case SpeechRecognizer.ERROR_AUDIO:
                errorCode = SpeechRecognizer.ERROR_AUDIO;
                errorMessage = "ERROR_AUDIO";
                break;

            case SpeechRecognizer.ERROR_CLIENT:
                errorCode = SpeechRecognizer.ERROR_CLIENT;
                errorMessage = "ERROR_CLIENT";
                break;

            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                errorCode = SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS;
                errorMessage = "ERROR_INSUFFICIENT_PERMISSIONS";
                break;

            case SpeechRecognizer.ERROR_NETWORK:
                errorCode = SpeechRecognizer.ERROR_NETWORK;
                errorMessage = "ERROR_NETWORK";
                break;

            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                errorCode = SpeechRecognizer.ERROR_NETWORK_TIMEOUT;
                errorMessage = "ERROR_NETWORK_TIMEOUT";
                break;

            case SpeechRecognizer.ERROR_NO_MATCH:
                errorCode = SpeechRecognizer.ERROR_NO_MATCH;
                errorMessage = "ERROR_NO_MATCH";
                break;

            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                errorCode = SpeechRecognizer.ERROR_RECOGNIZER_BUSY;
                errorMessage = "ERROR_RECOGNIZER_BUSY";
                break;

            case SpeechRecognizer.ERROR_SERVER:
                errorCode = SpeechRecognizer.ERROR_SERVER;
                errorMessage = "ERROR_SERVER";
                break;

            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                errorCode = SpeechRecognizer.ERROR_SPEECH_TIMEOUT;
                errorMessage = "ERROR_SPEECH_TIMEOUT";
                break;

            default:
                errorMessage = "ERROR";
                break;
        }
        Log.d(TAG, "onError:" + errorMessage + "," + errorCode);
        onSpeechRecognitionListener.OnSpeechRecognitionError(errorCode, errorMessage, false);
    }

    @Override
    public void onResults(Bundle bundle) {
        Log.d(TAG, "onResults...");
        ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if (matches != null && matches.size() > 0) {
            String sentence = matches.get(0);
            Log.d(TAG, sentence);
            onSpeechRecognitionListener.OnSpeechRecognitionFinalResult(sentence, false);

        }
    }

    @Override
    public void onPartialResults(Bundle bundle) {
        Log.d(TAG, "onPartialResults...");
        //sentence with highest confidence score is in position 0
        ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if (matches != null && matches.size() > 0) {
            String word = matches.get(0);
            Log.d(TAG, word);
            onSpeechRecognitionListener.OnSpeechRecognitionCurrentResult(word, false);
        }
    }

    @Override
    public void onEvent(int eventType, Bundle bundle) {
        Log.d(TAG, "onEvent : " + eventType);
    }
}
