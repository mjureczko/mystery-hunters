/*
 * Copyright (C) 2026 Marian Jureczko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

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
