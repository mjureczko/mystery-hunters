package pl.marianjureczko.mysteryhunters.port

/**
 * Stands in for Vosk. By default it reports that recognition is impossible, which is exactly the
 * situation in which the app has to fall back to the text field.
 */
class TestSpeechToTextPort : SpeechToTextPort {

    var supported: Boolean = false
    var recognizedText: String = ""
    var released: Boolean = false

    override suspend fun initialize(languageTag: String): Boolean = supported

    override fun startListening(listener: SpeechToTextPort.RecognitionListener) {
        if (supported) {
            listener.onResult(recognizedText)
        } else {
            listener.onError("not initialized")
        }
    }

    override fun stopListening() = Unit

    override fun release() {
        released = true
    }
}
