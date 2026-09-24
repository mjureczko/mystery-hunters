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

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.model.RouteArranger
import pl.marianjureczko.mysteryhunters.screen.camera.AR_SCENE
import pl.marianjureczko.mysteryhunters.screen.camera.BIG_CATCH_BUTTON
import pl.marianjureczko.mysteryhunters.screen.camera.CAMERA_PREVIEW
import pl.marianjureczko.mysteryhunters.screen.camera.CLOSE_LOOK_AROUND_MESSAGE
import pl.marianjureczko.mysteryhunters.screen.camera.MARK_NOT_VISIBLE_DIALOG
import pl.marianjureczko.mysteryhunters.screen.camera.FLAT_QUESTION_MARK
import pl.marianjureczko.mysteryhunters.screen.camera.NOTHING_IN_RANGE_MESSAGE
import pl.marianjureczko.mysteryhunters.screen.camera.TOO_FAR_DIALOG
import pl.marianjureczko.mysteryhunters.screen.pointdetail.POINT_DESCRIPTION_LABEL
import pl.marianjureczko.mysteryhunters.screen.routelist.SELECT_ROUTE_BUTTON
import pl.marianjureczko.mysteryhunters.screen.searching.CATCH_POINT_BUTTON
import pl.marianjureczko.poszukiwacz.compass.api.AndroidLocation

@HiltAndroidTest
class CameraScreenTest : AbstractUiTest() {

    /** One degree of latitude is roughly 111320 m, which turns metres into coordinates. */
    private val metersPerLatitudeDegree = 111_320.0

    @Test
    fun catchThePointWhenTheHunterStandsCloseEnough() {
        // given
        arAvailabilityPort.available = false
        val route = givenRouteWithHunterStandingMetersAway(0.0)

        // when
        openCameraScreen(route.name)
        composeRule.onNodeWithContentDescription(BIG_CATCH_BUTTON).performClick()

        // then
        waitWhileAdvancingFrames("the point is caught") {
            storedRoute(route.id)?.pointById(1)?.caught == true
        }
        waitWhileAdvancingFrames("the point description shows up") { isDisplayed(POINT_DESCRIPTION_LABEL) }
        assertThat(storedRoute(route.id)?.pointById(1)?.caught).isTrue()
    }

    @Test
    fun switchToTheNextNotCaughtPointAfterCatching() {
        // given
        arAvailabilityPort.available = false
        val route = givenRouteWithHunterStandingMetersAway(0.0, pointsCount = 3)

        // when
        openCameraScreen(route.name)
        composeRule.onNodeWithContentDescription(BIG_CATCH_BUTTON).performClick()

        // then
        waitWhileAdvancingFrames("the hunt moves on to the next point") {
            storedRoute(route.id)?.lastSelectedPointId == 2
        }
        assertThat(storedRoute(route.id)?.lastSelectedPointId).isEqualTo(2)
    }

    @Test
    fun refuseToCatchAndExplainWhenTheHunterIsTooFarAway() {
        // given
        val route = givenRouteWithHunterStandingMetersAway(100.0)

        // when
        openCameraScreen(route.name)
        composeRule.onNodeWithContentDescription(BIG_CATCH_BUTTON).performClick()

        // then
        waitWhileAdvancingFrames("the too far dialog shows up") { isDisplayed(TOO_FAR_DIALOG) }
        assertThat(isDisplayed(NOTHING_IN_RANGE_MESSAGE)).isTrue()
        assertThat(storedRoute(route.id)?.pointById(1)?.caught).isFalse()
    }

    @Test
    fun showTheAugmentedRealitySceneWhenArCoreIsAvailable() {
        // given
        arAvailabilityPort.available = true
        val route = givenRouteWithHunterStandingMetersAway(0.0)

        // when
        openCameraScreen(route.name)

        // then
        waitWhileAdvancingFrames("the augmented reality scene shows up") { isDisplayed(AR_SCENE) }
        assertThat(isDisplayed(AR_SCENE)).isTrue()
    }

    @Test
    fun fallBackToAPlainCameraWithAFlatMarkWhenArCoreIsMissing() {
        // given
        arAvailabilityPort.available = false
        val route = givenRouteWithHunterStandingMetersAway(0.0)

        // when
        openCameraScreen(route.name)

        // then
        waitUntilDisplayed(CAMERA_PREVIEW)
        waitUntilDisplayed(FLAT_QUESTION_MARK)
        composeRule.onNodeWithContentDescription(FLAT_QUESTION_MARK).assertIsDisplayed()
    }

    @Test
    fun catchThePointOnADeviceWithoutArCore() {
        // given
        arAvailabilityPort.available = false
        val route = givenRouteWithHunterStandingMetersAway(0.0)

        // when
        openCameraScreen(route.name)
        waitUntilDisplayed(BIG_CATCH_BUTTON)
        composeRule.onNodeWithContentDescription(BIG_CATCH_BUTTON).performClick()

        // then
        composeRule.waitUntil(WAIT_TIMEOUT_IN_MILLIS) {
            storedRoute(route.id)?.pointById(1)?.caught == true
        }
        assertThat(storedRoute(route.id)?.pointById(1)?.caught).isTrue()
    }

    /**
     * Where the point lies and how far it is are measured only when a position fix comes in, so a
     * hunter who walks into range while holding the phone perfectly still has to be noticed by the
     * position fix alone.
     */
    @Test
    fun showTheQuestionMarkWhenTheHunterWalksIntoRange() {
        // given
        arAvailabilityPort.available = false
        val route = givenRouteWithHunterStandingMetersAway(100.0)
        openCameraScreen(route.name)
        waitUntilDisplayed(NOTHING_IN_RANGE_MESSAGE)

        // when
        composeRule.runOnIdle { locationPort.moveTo(AndroidLocation.create(52.0, 21.0)) }

        // then
        waitUntilDisplayed(FLAT_QUESTION_MARK)
        assertThat(isDisplayed(NOTHING_IN_RANGE_MESSAGE)).isFalse()
    }

    /**
     * Being close enough is not the same as having spotted the mark - it stands somewhere around
     * the hunter and may well be behind a wall - so the screen says so rather than going quiet.
     */
    @Test
    fun tellTheHunterToLookAroundOnceCloseEnough() {
        // given
        arAvailabilityPort.available = false
        val route = givenRouteWithHunterStandingMetersAway(0.0)

        // when
        openCameraScreen(route.name)

        // then
        waitUntilDisplayed(CLOSE_LOOK_AROUND_MESSAGE)
        assertThat(isDisplayed(NOTHING_IN_RANGE_MESSAGE)).isFalse()
    }

    /**
     * Standing on top of the point is not enough on a phone that draws the mark in the scene: the
     * mark has to be on the screen, with nothing in front of it, before it can be caught. Here the
     * scene never starts tracking, so it can never show the mark, and the hunt says so instead of
     * handing over the point.
     */
    @Test
    fun refuseToCatchWhileTheMarkCannotBeSeen() {
        // given
        arAvailabilityPort.available = true
        val route = givenRouteWithHunterStandingMetersAway(0.0)

        // when
        openCameraScreen(route.name)
        composeRule.onNodeWithContentDescription(BIG_CATCH_BUTTON).performClick()

        // then
        waitWhileAdvancingFrames("the mark is not visible dialog shows up") {
            isDisplayed(MARK_NOT_VISIBLE_DIALOG)
        }
        assertThat(storedRoute(route.id)?.pointById(1)?.caught).isFalse()
    }

    private fun givenRouteWithHunterStandingMetersAway(meters: Double, pointsCount: Int = 1): Route {
        val base = RouteArranger.routeWithPoints(pointsCount)
        val positioned = base.copy(
            pointsOfInterest = base.pointsOfInterest.mapIndexed { index, point ->
                point.copy(latitude = 52.0 + index * 0.01, longitude = 21.0)
            }
        )
        locationPort.currentLocation = AndroidLocation.create(52.0 + meters / metersPerLatitudeDegree, 21.0)
        return given(positioned)
    }

    private fun openCameraScreen(routeName: String) {
        waitUntilDisplayed("$SELECT_ROUTE_BUTTON $routeName")
        composeRule.onNodeWithContentDescription("$SELECT_ROUTE_BUTTON $routeName").performClick()
        waitUntilDisplayed(CATCH_POINT_BUTTON)
        composeRule.onNodeWithContentDescription(CATCH_POINT_BUTTON).performClick()
        if (arAvailabilityPort.available) {
            takeOverTheComposeClock()
        }
        waitWhileAdvancingFrames("the camera screen shows up") { isDisplayed(BIG_CATCH_BUTTON) }
    }
}
