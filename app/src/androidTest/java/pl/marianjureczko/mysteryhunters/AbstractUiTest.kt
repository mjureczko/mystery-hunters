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

package pl.marianjureczko.mysteryhunters

import android.Manifest
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.port.TestArAvailabilityPort
import pl.marianjureczko.mysteryhunters.port.TestDeviceOrientationPort
import pl.marianjureczko.mysteryhunters.port.TestLocationPort
import pl.marianjureczko.mysteryhunters.port.TestRouteStoragePort
import pl.marianjureczko.mysteryhunters.port.TestSpeechToTextPort
import javax.inject.Inject

@HiltAndroidTest
abstract class AbstractUiTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var storage: TestRouteStoragePort

    @Inject
    lateinit var speechToTextPort: TestSpeechToTextPort

    @Inject
    lateinit var arAvailabilityPort: TestArAvailabilityPort

    @Inject
    lateinit var locationPort: TestLocationPort

    @Inject
    lateinit var deviceOrientationPort: TestDeviceOrientationPort

    @Before
    fun injectDependencies() {
        hiltRule.inject()
    }

    @After
    fun restoreTheComposeClock() {
        composeRule.mainClock.autoAdvance = true
    }

    /**
     * The augmented reality view renders frame after frame, so Compose never reports itself idle
     * and every synchronised assertion would time out. From this point on the clock is driven by
     * hand, which keeps such screens testable.
     */
    protected fun takeOverTheComposeClock() {
        composeRule.mainClock.autoAdvance = false
        advanceFrames()
    }

    protected fun advanceFrames(count: Int = 30) {
        if (composeRule.mainClock.autoAdvance) {
            return
        }
        repeat(count) { composeRule.mainClock.advanceTimeByFrame() }
    }

    /** Polling variant of [waitUntilDisplayed] for screens whose clock is driven by hand. */
    protected fun waitWhileAdvancingFrames(description: String, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + WAIT_TIMEOUT_IN_MILLIS
        while (System.currentTimeMillis() < deadline) {
            if (condition()) {
                return
            }
            advanceFrames(count = 2)
            Thread.sleep(POLLING_INTERVAL_IN_MILLIS)
        }
        throw AssertionError("$description did not happen within $WAIT_TIMEOUT_IN_MILLIS ms")
    }

    protected fun isDisplayed(description: String): Boolean =
        composeRule.onAllNodesWithContentDescription(description, substring = true)
            .fetchSemanticsNodes()
            .isNotEmpty()

    protected fun given(route: Route): Route = runBlocking { storage.save(route) }

    protected fun storedRoute(routeId: Long): Route? = runBlocking { storage.load(routeId) }

    protected fun waitUntilDisplayed(description: String) {
        composeRule.waitUntil(WAIT_TIMEOUT_IN_MILLIS) {
            composeRule.onAllNodesWithContentDescription(description, substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    protected fun waitUntilTextDisplayed(text: String) {
        composeRule.waitUntil(WAIT_TIMEOUT_IN_MILLIS) {
            composeRule.onAllNodesWithText(text, substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    companion object {
        const val WAIT_TIMEOUT_IN_MILLIS = 10_000L
        const val POLLING_INTERVAL_IN_MILLIS = 16L
    }
}
