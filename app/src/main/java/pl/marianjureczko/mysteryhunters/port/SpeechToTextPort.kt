package pl.marianjureczko.mysteryhunters.port

/**
 * Wraps the on device speech recognition engine. Nothing is ever sent to a network service and no
 * audio is stored; only the recognised text leaves this port.
 */
interface SpeechToTextPort {

    /**
     * Prepares the engine for the given language. Returns false when recognition is not possible on
     * this device (model missing, engine unsupported), in which case the caller falls back to text
     * input.
     */
    suspend fun initialize(languageTag: String): Boolean

    fun startListening(listener: RecognitionListener)

    fun stopListening()

    fun release()

    interface RecognitionListener {
        fun onPartialResult(text: String)
        fun onResult(text: String)
        fun onError(message: String)
    }
}
