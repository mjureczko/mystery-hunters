package pl.marianjureczko.mysteryhunters.port.speech

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.LogLevel
import org.vosk.LibVosk
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.SpeechService
import pl.marianjureczko.mysteryhunters.port.SpeechToTextPort
import org.vosk.android.RecognitionListener as VoskRecognitionListener

/**
 * Offline speech recognition backed by Vosk. Recognition happens entirely on the device; no audio
 * is stored and no network service is contacted.
 *
 * The language models are not part of the repository, they are fetched by the
 * `downloadVoskModels` Gradle task. When a model is missing [initialize] answers false and the
 * caller falls back to plain text input.
 */
class VoskSpeechToTextPort(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher
) : SpeechToTextPort {

    companion object {
        const val POLISH = "pl"
        const val ENGLISH = "en"
        private const val SAMPLE_RATE = 16000.0f
        private val MODEL_ASSET_DIRECTORIES = mapOf(
            POLISH to "vosk-model-small-pl-0.22",
            ENGLISH to "vosk-model-small-en-us-0.15"
        )

        fun modelAssetDirectory(languageTag: String): String? =
            MODEL_ASSET_DIRECTORIES[languageTag.lowercase().substringBefore('-')]
    }

    private val TAG = javaClass.simpleName

    private var model: Model? = null
    private var speechService: SpeechService? = null

    override suspend fun initialize(languageTag: String): Boolean = withContext(ioDispatcher) {
        val assetDirectory = modelAssetDirectory(languageTag)
        if (assetDirectory == null) {
            Log.i(TAG, "no offline model configured for $languageTag")
            return@withContext false
        }
        if (!isModelBundled(assetDirectory)) {
            Log.i(TAG, "model $assetDirectory is not bundled, falling back to text input")
            return@withContext false
        }
        try {
            LibVosk.setLogLevel(LogLevel.WARNINGS)
            val unpackedPath = org.vosk.android.StorageService.sync(context, assetDirectory, assetDirectory)
            model = Model(unpackedPath)
            true
        } catch (e: Exception) {
            Log.w(TAG, "offline speech recognition unavailable", e)
            model = null
            false
        }
    }

    private fun isModelBundled(assetDirectory: String): Boolean =
        try {
            context.assets.list(assetDirectory)?.isNotEmpty() == true
        } catch (e: Exception) {
            false
        }

    override fun startListening(listener: SpeechToTextPort.RecognitionListener) {
        val readyModel = model
        if (readyModel == null) {
            listener.onError("not initialized")
            return
        }
        try {
            val recognizer = Recognizer(readyModel, SAMPLE_RATE)
            speechService = SpeechService(recognizer, SAMPLE_RATE).also {
                it.startListening(object : VoskRecognitionListener {
                    override fun onPartialResult(hypothesis: String?) {
                        listener.onPartialResult(extract(hypothesis, "partial"))
                    }

                    override fun onResult(hypothesis: String?) {
                        listener.onResult(extract(hypothesis, "text"))
                    }

                    override fun onFinalResult(hypothesis: String?) {
                        listener.onResult(extract(hypothesis, "text"))
                    }

                    override fun onError(exception: Exception?) {
                        listener.onError(exception?.message ?: "recognition failed")
                    }

                    override fun onTimeout() {
                        listener.onError("timeout")
                    }
                })
            }
        } catch (e: Exception) {
            Log.w(TAG, "failed to start listening", e)
            listener.onError(e.message ?: "recognition failed")
        }
    }

    /** Vosk answers with a small JSON document; only the recognised text is taken out of it. */
    private fun extract(hypothesis: String?, field: String): String =
        try {
            if (hypothesis.isNullOrBlank()) "" else JSONObject(hypothesis).optString(field, "")
        } catch (e: Exception) {
            ""
        }

    override fun stopListening() {
        speechService?.stop()
        speechService?.shutdown()
        speechService = null
    }

    override fun release() {
        stopListening()
        model?.close()
        model = null
    }
}
