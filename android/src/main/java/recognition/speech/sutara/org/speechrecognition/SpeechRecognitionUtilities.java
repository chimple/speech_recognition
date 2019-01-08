package recognition.speech.sutara.org.speechrecognition;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.speech.RecognizerIntent;

import java.util.List;

final class SpeechRecognitionUtilities {
    static boolean isSpeechRecognitionEnabled(Context context) {

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0)
            return false;

        return true;
    }
}