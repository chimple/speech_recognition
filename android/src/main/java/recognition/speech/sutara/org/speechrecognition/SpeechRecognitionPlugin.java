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
import java.util.Map;


/**
 * SpeechRecognitionPlugin
 */
public class SpeechRecognitionPlugin implements MethodCallHandler, OnSpeechRecognitionListener {

    private static final String TAG = "SpeechRecognitionPlugin";
    private SpeechRecognition speech;
    private MethodChannel speechChannel;
    String speechText = "";
    private boolean isUserAborted = false;
    private boolean isSpeechRecognitionEnabled = false;
    private Locale currentLocale = null;
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
    }

    private void initializeSpeechRecognition() {
        Log.d(TAG, "initializeSpeechRecognition");
        speech = new SpeechRecognition(activity.getApplicationContext(), this.currentLocale);
        speech.setSpeechRecognitionListener(this);
        speech.useOnlyOfflineRecognition(true);
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("SpeechRecognizer.initialize")) {
            this.currentLocale = activity.getResources().getConfiguration().locale;
            initializeSpeechRecognition();
            this.isSpeechRecognitionEnabled = speech.isSpeechRecognitionAvailable();
            Log.d(TAG, "Current Locale : " + this.currentLocale.toString());
            speechChannel.invokeMethod("SpeechRecognizer.onCurrentLocale", this.currentLocale.toString());
            speechChannel.invokeMethod("SpeechRecognizer.onSpeechAvailability", this.isSpeechRecognitionEnabled);
            result.success(isSpeechRecognitionEnabled);
        } else if (call.method.equals("SpeechRecognizer.listen")) {
            this.isUserAborted = false;
            this.configureLocale((Map<String, String>) call.arguments);
            if (this.speech == null) {
                Log.d(TAG, "reinitializing speech recognizer ....");
                initializeSpeechRecognition();
            }
            speech.startSpeechRecognition(this.currentLocale);
            result.success(true);
        } else if (call.method.equals("SpeechRecognizer.stop")) {
            this.isUserAborted = true;
            Log.d(TAG, "SpeechRecognizer.stop invoked");
            speech.stopSpeechRecognition();
            result.success(true);
        } else if (call.method.equals("SpeechRecognizer.changeLocale")) {
            this.isUserAborted = true;
            Log.d(TAG, "SpeechRecognizer.changeLocale invoked with args:" + call.arguments.toString());
            destroy();
            this.configureLocale((Map<String, String>) call.arguments);
            if (this.speech == null) {
                Log.d(TAG, "reinitializing speech recognizer ....");
                initializeSpeechRecognition();
            }
            result.success(true);
        } else {
            result.notImplemented();
        }
    }

    private void configureLocale(Map<String, String> args) {
        String lang = args.get("lang");
        String country = args.get("country");
        Log.d(TAG, "listen lang : " + lang);
        Log.d(TAG, "listen country : " + country);
        this.currentLocale = new Locale(lang, country);
    }

    private void destroy() {
        if (speech != null) {
            speech.destroy();
            speech = null;
        }
    }


    private void sendSpeechText(boolean isCompleted, boolean shouldRestart) {
        Log.d(TAG, "sendSpeechText -> " + speechText + " isCompleted ->" + isCompleted);
        if(isCompleted) {
            speechChannel.invokeMethod("SpeechRecognizer.onSpeechRecognitionResult", speechText);
            speechText = "";
        }

        if(shouldRestart) {
            speech.startSpeechRecognition(this.currentLocale);
        }

    }

    @Override
    public void OnSpeechRecognitionStarted() {
        speechText = "";
    }

    @Override
    public void OnSpeechRecognitionStopped() {
        Log.d(TAG, "listener -> OnSpeechRecognitionStopped");
        speech.startSpeechRecognition(this.currentLocale);
    }

    @Override
    public void OnSpeechRecognitionFinalResult(String finalSentence, boolean isCompleted) {
        speechText = finalSentence;
        Log.d(TAG, "onResults -> " + speechText);
        sendSpeechText(isCompleted, true);
    }

    @Override
    public void OnSpeechRecognitionCurrentResult(String currentWord, boolean isCompleted) {
        speechText = currentWord;
        sendSpeechText(isCompleted, false);
    }

    @Override
    public void OnSpeechRecognitionError(int errorCode, String errorMsg, boolean isError) {
        Log.d(TAG, "Error generated ->" + errorCode + " -> " + errorMsg);
        if (errorCode == SpeechRecognizer.ERROR_SERVER) {
            this.showInstallOfflineVoiceFiles(activity.getApplicationContext());
            destroy();
            this.handleErrors();
        } else if (errorCode == SpeechRecognizer.ERROR_NO_MATCH || errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
            this.handleErrors();
        }
    }

    private void handleErrors() {
        speechChannel.invokeMethod("SpeechRecognizer.onSpeechRecognitionError", true);
        speechChannel.invokeMethod("SpeechRecognizer.onSpeechAvailability", this.isSpeechRecognitionEnabled);
        sendSpeechText(true, false);
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

