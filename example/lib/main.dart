import 'package:flutter/material.dart';
import 'package:speech_recognition/speech_recognition.dart';
import 'package:simple_permissions/simple_permissions.dart';

void main() async {
  runApp(new SpeechRecognitionApp());
}

const languages = const [const Language('Hindi', 'hi_IN')];

class Language {
  final String name;
  final String code;

  const Language(this.name, this.code);
}

class SpeechRecognitionApp extends StatefulWidget {
  @override
  _SpeechRecognitionState createState() => new _SpeechRecognitionState();
}

class _SpeechRecognitionState extends State<SpeechRecognitionApp> {
  SpeechRecognition _speech;
  bool _speechRecognitionAvailable = false;
  bool _isListening = false;
  String speechText = '';
  Language selectedLang = languages.first;

  @override
  initState() {
    super.initState();
    initialize();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  void initialize() {
    SimplePermissions.requestPermission(Permission.RecordAudio);

    print('_SpeechRecognitionState.initialize... ');
    _speech = new SpeechRecognition();
    _speech.setSpeechRecognitionAvailableHandler(onSpeechRecognitionAvailable);
    _speech.setSpeechCurrentLocaleHandler(onSpeechCurrentLocaleSelected);
    _speech.setSpeechRecognitionOnBeginningHandler(onSpeechRecognitionBegan);
    _speech.setSpeechRecognitionResultHandler(onSpeechRecognitionResult);
    _speech.setSpeechRecognitionOnEndedHandler(onSpeechRecognitionEnded);
    _speech.setSpeechRecognitionOnErrorHandler(onSpeechRecognitionError);

    _speech
        .initialize()
        .then((res) => setState(() => _speechRecognitionAvailable = res));
  }

  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new Scaffold(
        appBar: new AppBar(
          title: new Text('SpeechRecognition'),
          actions: [
            new PopupMenuButton<Language>(
              onSelected: _selectLangHandler,
              itemBuilder: (BuildContext context) => _buildLanguagesWidgets,
            )
          ],
        ),
        body: new Padding(
            padding: new EdgeInsets.all(8.0),
            child: new Center(
              child: new Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  new Expanded(
                      child: new Container(
                          padding: const EdgeInsets.all(8.0),
                          color: Colors.grey.shade200,
                          child: new Text(speechText))),
                  _buildButton(
                    onPressed: _speechRecognitionAvailable && !_isListening
                        ? () => startListening()
                        : null,
                    label: _isListening
                        ? 'Listening...'
                        : 'Listen (${selectedLang.code})',
                  ),
                  _buildButton(
                    onPressed: _isListening ? () => cancelListening() : null,
                    label: 'Cancel',
                  ),
                  _buildButton(
                    onPressed: _isListening ? () => stopListening() : null,
                    label: 'Stop',
                  ),
                ],
              ),
            )),
      ),
    );
  }

  List<CheckedPopupMenuItem<Language>> get _buildLanguagesWidgets => languages
      .map((l) => new CheckedPopupMenuItem<Language>(
            value: l,
            checked: selectedLang == l,
            child: new Text(l.name),
          ))
      .toList();

  void _selectLangHandler(Language lang) {
    setState(() => selectedLang = lang);
  }

  Widget _buildButton({String label, VoidCallback onPressed}) => new Padding(
        padding: new EdgeInsets.all(12.0),
        child: new RaisedButton(
          color: Colors.cyan.shade600,
          onPressed: onPressed,
          child: new Text(
            label,
            style: const TextStyle(color: Colors.white),
          ),
        ),
      );

  void startListening() =>
      _speech.listen(locale: selectedLang.code).then((result) =>
          print('_SpeechRecognitionAppState.start => result ${result}'));

  void cancelListening() {
    _speech.cancel().then((result) {
      print('_speechRecognitionAvailable $_speechRecognitionAvailable');
      setState(() => _isListening = !result);
    });
  }

  void stopListening() =>
      _speech.stop().then((result) => setState(() => _isListening = !result));

  void onSpeechRecognitionAvailable(bool result) =>
      setState(() => _speechRecognitionAvailable = result);

  void onSpeechCurrentLocaleSelected(String locale) {
    print(
        '_SpeechRecognitionAppState.onSpeechCurrentLocaleSelected... $locale');
    setState(
        () => selectedLang = languages.firstWhere((l) => l.code == locale));
  }

  void onSpeechRecognitionBegan() => setState(() => _isListening = true);

  void onSpeechRecognitionResult(String text) =>
      setState(() => speechText = text);

  void onSpeechRecognitionEnded() {
    print('_SpeechRecognitionAppState.onSpeechRecognitionEnded');
    setState(() => _isListening = false);
  }

  void onSpeechRecognitionError(bool result) =>
      setState(() => _isListening = !result);
}
