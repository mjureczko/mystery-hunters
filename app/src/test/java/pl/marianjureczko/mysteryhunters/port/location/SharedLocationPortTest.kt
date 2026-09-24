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

package pl.marianjureczko.mysteryhunters.port.location

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.Dispatchers
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pl.marianjureczko.poszukiwacz.compass.api.AndroidLocation
import pl.marianjureczko.poszukiwacz.compass.api.LocationPort

@OptIn(ExperimentalCoroutinesApi::class)
class SharedLocationPortTest {

    private val realPort = RecordingLocationPort()
    private lateinit var sut: SharedLocationPort

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        sut = SharedLocationPort(realPort)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `SHOULD hand every position to every screen that is listening`() {
        // given
        val compassScreen = Listener().also { sut.startFetching(CoroutineScope(Job()), it) }
        val cameraScreen = Listener().also { sut.startFetching(CoroutineScope(Job()), it) }
        val somewhere = locationAt(51.25, 16.93)

        // when
        realPort.report(somewhere)

        // then
        assertThat(compassScreen.received).containsExactly(somewhere)
        assertThat(cameraScreen.received).containsExactly(somewhere)
    }

    @Test
    fun `SHOULD subscribe to the real port only once however many screens are listening`() {
        // given
        sut.startFetching(CoroutineScope(Job()), Listener())

        // when
        sut.startFetching(CoroutineScope(Job()), Listener())

        // then
        assertThat(realPort.timesStarted).isEqualTo(1)
    }

    @Test
    fun `SHOULD go on feeding the others when one screen stops listening`() {
        // given
        val compassScreen = Listener().also { sut.startFetching(CoroutineScope(Job()), it) }
        val cameraScope = CoroutineScope(Job())
        sut.startFetching(cameraScope, Listener())

        // when
        cameraScope.cancel()
        val somewhere = locationAt(51.25, 16.93)
        realPort.report(somewhere)

        // then
        assertThat(compassScreen.received).containsExactly(somewhere)
        assertThat(realPort.stopped).isFalse()
    }

    @Test
    fun `SHOULD stop the real fetching once no screen is listening any more`() {
        // given
        val compassScope = CoroutineScope(Job())
        val cameraScope = CoroutineScope(Job())
        sut.startFetching(compassScope, Listener())
        sut.startFetching(cameraScope, Listener())

        // when
        compassScope.cancel()
        cameraScope.cancel()

        // then
        assertThat(realPort.stopped).isTrue()
    }

    @Test
    fun `SHOULD hand the position it already knows to a screen that starts listening later`() {
        // given
        sut.startFetching(CoroutineScope(Job()), Listener())
        val somewhere = locationAt(51.25, 16.93)
        realPort.report(somewhere)

        // when
        val cameraScreen = Listener().also { sut.startFetching(CoroutineScope(Job()), it) }

        // then
        assertThat(cameraScreen.received).containsExactly(somewhere)
    }

    private fun locationAt(latitude: Double, longitude: Double): AndroidLocation =
        object : AndroidLocation {
            override var latitude: Double = latitude
            override var longitude: Double = longitude
            override val accuracy: Float = 1f
            override val observedAt: Long = 0L
        }

    private class Listener : (AndroidLocation) -> Unit {
        val received = mutableListOf<AndroidLocation>()
        override fun invoke(location: AndroidLocation) {
            received.add(location)
        }
    }

    /** Stands in for the port of the compass module, which keeps room for a single listener. */
    private class RecordingLocationPort : LocationPort {
        var timesStarted = 0
        var stopped = false
        private var listener: ((AndroidLocation) -> Unit)? = null

        override fun startFetching(
            coroutineScope: CoroutineScope,
            updateLocationCallback: (AndroidLocation) -> Unit
        ) {
            timesStarted++
            stopped = false
            listener = updateLocationCallback
        }

        override fun stopFetching() {
            stopped = true
            listener = null
        }

        fun report(location: AndroidLocation) {
            listener?.invoke(location)
        }
    }
}
