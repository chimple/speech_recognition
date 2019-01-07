import 'dart:async';

import 'package:flutter/services.dart';

class SpeechRecognition {
  static const MethodChannel _channel =
      const MethodChannel('speech_recognition');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
