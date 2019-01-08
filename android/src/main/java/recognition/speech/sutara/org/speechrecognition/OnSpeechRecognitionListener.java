package recognition.speech.sutara.org.speechrecognition;

public interface OnSpeechRecognitionListener {
    void OnSpeechRecognitionStarted();

    void OnSpeechRecognitionStopped();

    void onReadyForSpeech(boolean result);

    void OnSpeechRecognitionFinalResult(String finalSentence, boolean isFinal);

    void OnSpeechRecognitionCurrentResult(String currentWord, boolean isFinal);

    void OnSpeechRecognitionError(int errorCode, String errorMsg, boolean isReady);
}
