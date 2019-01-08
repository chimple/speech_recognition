package recognition.speech.sutara.org.speechrecognition;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
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
        this.queryLanguages(activity.getApplicationContext());
        this.initializeSpeechRecognition();
    }

    private void initializeSpeechRecognition() {
        Log.d(TAG, "initializeSpeechRecognition");
        speech = new SpeechRecognition(activity.getApplicationContext());
        speech.setSpeechRecognitionListener(this);
        speech.useOnlyOfflineRecognition(true);
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (this.speech.getSpeechRecognizer() == null) {
            this.speech.initializeSpeechRecognitionParameters();
        }
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
    public void OnSpeechRecognitionError(int errorCode, String errorMsg, boolean isError) {
        Log.d(TAG, "Error generated ->" + errorCode + " -> " + errorMsg);
        if (errorCode == SpeechRecognizer.ERROR_SERVER) {
            this.showInstallOfflineVoiceFiles(activity.getApplicationContext());
            speech.destroy();
            speechChannel.invokeMethod("SpeechRecognizer.onSpeechRecognitionError", true);
        } else if (errorCode == SpeechRecognizer.ERROR_NO_MATCH) {
            speechChannel.invokeMethod("SpeechRecognizer.onSpeechRecognitionError", true);
        }
        speechChannel.invokeMethod("SpeechRecognizer.onSpeechAvailability", true);
    }

    @Override
    public void onReadyForSpeech(boolean isReady) {
        speechChannel.invokeMethod("SpeechRecognizer.onSpeechAvailability", isReady);
    }

    public static final String PACKAGE_NAME_GOOGLE_NOW = "com.google.android.googlequicksearchbox";
    public static final String ACTIVITY_INSTALL_OFFLINE_FILES = "com.google.android.voicesearch.greco3.languagepack.InstallActivity";

    public static boolean showInstallOfflineVoiceFiles(final Context ctx) {

        final Intent intent = new Intent();
        intent.setComponent(new ComponentName(PACKAGE_NAME_GOOGLE_NOW, ACTIVITY_INSTALL_OFFLINE_FILES));

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        try {
            ctx.startActivity(intent);
            return true;
        } catch (final ActivityNotFoundException e) {

        } catch (final Exception e) {

        }

        return false;
    }

    /**
     * Open speech recognition settings activity
     *
     * @return true in case activity was launched, false otherwise
     **/
    public boolean openSpeechRecognitionSettings(final Context ctx) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        boolean started = false;
        ComponentName[] components = new ComponentName[]{
                new ComponentName("com.google.android.googlequicksearchbox", "com.google.android.apps.gsa.settingsui.VoiceSearchPreferences"),
                new ComponentName("com.google.android.voicesearch", "com.google.android.voicesearch.VoiceSearchPreferences"),
                new ComponentName("com.google.android.googlequicksearchbox", "com.google.android.voicesearch.VoiceSearchPreferences"),
                new ComponentName("com.google.android.googlequicksearchbox", "com.google.android.apps.gsa.velvet.ui.settings.VoiceSearchPreferences")
        };
        for (ComponentName componentName : components) {
            try {
                intent.setComponent(componentName);
                ctx.startActivity(intent);
                started = true;
                break;
            } catch (final Exception e) {

            }
        }
        return started;
    }

    public void queryLanguages(Context ctx) {
        Intent intent = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
        ctx.sendOrderedBroadcast(intent, null, new HintReceiver(),
                null, Activity.RESULT_OK, null, null);
    }

    private static class HintReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // the list of supported languages.
            ArrayList<CharSequence> hints = getResultExtras(true)
                    .getCharSequenceArrayList(
                            RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES);

            Iterator<CharSequence> itHints = hints.iterator();
            while (itHints.hasNext()) {
                Log.d(TAG, "supported language: " + itHints.next());
            }
        }
    }
}

