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
        val route = givenRouteWithHunterStandingMetersAway(0.0)

        // when
        openCameraScreen(route.name)
        waitUntilDisplayed(BIG_CATCH_BUTTON)
        composeRule.onNodeWithContentDescription(BIG_CATCH_BUTTON).performClick()

        // then
        composeRule.waitUntil(WAIT_TIMEOUT_IN_MILLIS) {
            storedRoute(route.id)?.pointById(1)?.caught == true
        }
        waitUntilDisplayed(POINT_DESCRIPTION_LABEL)
        assertThat(storedRoute(route.id)?.pointById(1)?.caught).isTrue()
    }

    @Test
    fun switchToTheNextNotCaughtPointAfterCatching() {
        // given
        val route = givenRouteWithHunterStandingMetersAway(0.0, pointsCount = 3)

        // when
        openCameraScreen(route.name)
        waitUntilDisplayed(BIG_CATCH_BUTTON)
        composeRule.onNodeWithContentDescription(BIG_CATCH_BUTTON).performClick()

        // then
        composeRule.waitUntil(WAIT_TIMEOUT_IN_MILLIS) {
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
        waitUntilDisplayed(BIG_CATCH_BUTTON)
        composeRule.onNodeWithContentDescription(BIG_CATCH_BUTTON).performClick()

        // then
        waitUntilDisplayed(TOO_FAR_DIALOG)
        composeRule.onNodeWithContentDescription(NOTHING_IN_RANGE_MESSAGE).assertIsDisplayed()
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
        waitUntilDisplayed(AR_SCENE)
        composeRule.onNodeWithContentDescription(AR_SCENE).assertIsDisplayed()
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
    }
}
