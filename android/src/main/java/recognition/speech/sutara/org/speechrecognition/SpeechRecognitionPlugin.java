package recognition.speech.sutara.org.speechrecognition;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;



import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import java.util.Locale;


/**
 * SpeechRecognitionPlugin
 */
public class SpeechRecognitionPlugin implements MethodCallHandler, OnSpeechRecognitionListener {

    private static final String TAG = "SpeechRecognitionPlugin";

    private SpeechRecognition speech;
    private MethodChannel speechChannel;
    String speechText = "";
    private boolean cancelled = false;
    private Intent recognizerIntent;
    private Activity activity;

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "speech_recognition");
        channel.setMethodCallHandler(new SpeechRecognitionPlugin(registrar.activity(), channel));
    }

    private SpeechRecognitionPlugin(Activity activity, MethodChannel channel) {
        Log.d(TAG, "SpeechRecognitionPlugin initialized.....");
        this.speechChannel = channel;
        this.speechChannel.setMethodCallHandler(this);
        this.activity = activity;

        speech = new SpeechRecognition(activity.getApplicationContext());
        speech.setSpeechRecognitionListener(this);
        speech.useOnlyOfflineRecognition(true);
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("SpeechRecognizer.initialize")) {
            result.success(true);
            Locale locale = activity.getResources().getConfiguration().locale;
            speech.setPreferredLanguage(locale);
            Log.d(TAG, "Current Locale : " + locale.toString());
            speechChannel.invokeMethod("SpeechRecognizer.onCurrentLocale", locale.toString());
        } else if (call.method.equals("SpeechRecognizer.listen")) {
            cancelled = false;
            speech.startSpeechRecognition(getLocale(call.arguments.toString()));
            result.success(true);
        } else if (call.method.equals("SpeechRecognizer.cancel")) {
            Log.d(TAG, "SpeechRecognizer.cancel invoked");
            speech.stopSpeechRecognition();
            cancelled = true;
            result.success(true);
        } else if (call.method.equals("SpeechRecognizer.stop")) {
            Log.d(TAG, "SpeechRecognizer.stop invoked");
            speech.stopSpeechRecognition();
            cancelled = true;
            result.success(true);
        } else {
            result.notImplemented();
        }
    }

    private Locale getLocale(String code) {
        String[] localeParts = code.split("_");
        return new Locale(localeParts[0], localeParts[1]);
    }

    private void sendSpeechText(boolean isFinal) {
        Log.d(TAG, "sendSpeechText -> " + speechText + " isFinal ->" + isFinal);
        speechChannel.invokeMethod(isFinal ? "SpeechRecognizer.onSpeechRecognitionEnded" : "SpeechRecognizer.onSpeech", speechText);
    }

    @Override
    public void OnSpeechRecognitionStarted() {
        speechText = "";
        speechChannel.invokeMethod("SpeechRecognizer.onSpeechRecognitionBegin", null);
    }

    @Override
    public void OnSpeechRecognitionStopped() {
        Log.d(TAG, "listener -> OnSpeechRecognitionStopped");
        speechChannel.invokeMethod("SpeechRecognizer.onSpeechRecognitionEnded", speechText);
    }

    @Override
    public void OnSpeechRecognitionFinalResult(String finalSentence, boolean isFinal) {
        speechText = finalSentence;
        Log.d(TAG, "onResults -> " + speechText);
        sendSpeechText(isFinal);
    }

    @Override
    public void OnSpeechRecognitionCurrentResult(String currentWord, boolean isFinal) {
        speechText = currentWord;
        sendSpeechText(isFinal);
    }

    @Override
    public void OnSpeechRecognitionError(int errorCode, String errorMsg, boolean isReady) {
        Log.d(TAG, "Error generated ->" + errorCode + " -> " + errorMsg);
        speechChannel.invokeMethod("SpeechRecognizer.onSpeechAvailability", true);
//        speechChannel.invokeMethod("SpeechRecognizer.onError", errorCode + ": " + errorMsg);
    }

    @Override
    public void onReadyForSpeech(boolean isReady) {
        speechChannel.invokeMethod("SpeechRecognizer.onSpeechAvailability", isReady);
    }
}