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
