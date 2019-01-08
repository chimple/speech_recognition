import 'dart:async';
import 'dart:ui';
import 'package:flutter/services.dart';

typedef void SpeechRecognitionBoolHandler(bool isSpeechAvailable);
typedef void SpeechRecognitionResultHandler(String message);
typedef void SpeechCurrentLocalHandler(String locale);

class SpeechRecognition {
  static final SpeechRecognition _instance = new SpeechRecognition._internal();

  factory SpeechRecognition() => _instance;

  static final MethodChannel _methodChannel =
      const MethodChannel('speech_recognition');

  SpeechRecognition._internal() {
    _methodChannel.setMethodCallHandler(_registerSpeechAPIHandlers);
  }

  SpeechRecognitionBoolHandler speechRecognitionAvailableHandler;
  SpeechRecognitionResultHandler speechRecognitionResultHandler;
  SpeechCurrentLocalHandler speechCurrentLocalHandler;
  VoidCallback speechRecognitionOnBeginningHandler;
  VoidCallback speechRecognitionOnEndedHandler;
  SpeechRecognitionBoolHandler speechRecognitionOnErrorHandler;

  static Future<String> get platformVersion async {
    final String version =
        await _methodChannel.invokeMethod('getPlatformVersion');
    return version;
  }

  Future _registerSpeechAPIHandlers(MethodCall call) async {
    print("_registerSpeechAPIHandlers call ${call.method} ${call.arguments}");
    switch (call.method) {
      case "SpeechRecognizer.onSpeechAvailability":
        speechRecognitionAvailableHandler(call.arguments);
        break;
      case "SpeechRecognizer.onCurrentLocale":
        speechCurrentLocalHandler(call.arguments);
        break;
      case "SpeechRecognizer.onSpeech":
        speechRecognitionResultHandler(call.arguments);
        break;
      case "SpeechRecognizer.onSpeechRecognitionBegin":
        speechRecognitionOnBeginningHandler();
        break;
      case "SpeechRecognizer.onSpeechRecognitionEnded":
        speechRecognitionOnEndedHandler();
        break;
      case "SpeechRecognizer.onSpeechRecognitionError":
        speechRecognitionOnErrorHandler(call.arguments);
        break;
      default:
        print('Unknown method ${call.method} ');
    }
  }

  void setSpeechRecognitionAvailableHandler(
          SpeechRecognitionBoolHandler handler) =>
      speechRecognitionAvailableHandler = handler;

  void setSpeechCurrentLocaleHandler(SpeechCurrentLocalHandler handler) =>
      speechCurrentLocalHandler = handler;

  void setSpeechRecognitionResultHandler(
          SpeechRecognitionResultHandler handler) =>
      speechRecognitionResultHandler = handler;

  void setSpeechRecognitionOnBeginningHandler(VoidCallback handler) =>
      speechRecognitionOnBeginningHandler = handler;

  void setSpeechRecognitionOnEndedHandler(VoidCallback handler) =>
      speechRecognitionOnEndedHandler = handler;

  void setSpeechRecognitionOnErrorHandler(
          SpeechRecognitionBoolHandler handler) =>
      speechRecognitionOnErrorHandler = handler;

  // Operations

  Future<dynamic> initialize() =>
      _methodChannel.invokeMethod("SpeechRecognizer.initialize");

  Future<dynamic> listen({String locale}) =>
      _methodChannel.invokeMethod("SpeechRecognizer.listen", locale);

  Future<dynamic> cancel() =>
      _methodChannel.invokeMethod("SpeechRecognizer.cancel");

  Future<dynamic> stop() =>
      _methodChannel.invokeMethod("SpeechRecognizer.stop");
}
