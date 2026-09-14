package pl.marianjureczko.mysteryhunters

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import pl.marianjureczko.mysteryhunters.model.RouteArranger
import pl.marianjureczko.mysteryhunters.screen.collected.COLLECTED_POINTS_LIST
import pl.marianjureczko.mysteryhunters.screen.collected.COLLECTED_POINT_ROW
import pl.marianjureczko.mysteryhunters.screen.collected.NOT_CAUGHT_INFO
import pl.marianjureczko.mysteryhunters.screen.pointdetail.POINT_DESCRIPTION_LABEL
import pl.marianjureczko.mysteryhunters.screen.routelist.SELECT_ROUTE_BUTTON
import pl.marianjureczko.mysteryhunters.screen.searching.COLLECTED_POINTS_BUTTON

@HiltAndroidTest
class CollectedPointsScreenTest : AbstractUiTest() {

    @Test
    fun showTheDescriptionOfAPointThatHasBeenCaught() {
        // given
        val base = RouteArranger.routeWithPoints(2)
        val caught = base.pointsOfInterest.first().copy(caught = true, description = "Odkryta tajemnica")
        val route = given(base.withPoint(caught))
        openCollectedPoints(route.name)

        // when
        composeRule.onNodeWithContentDescription("$COLLECTED_POINT_ROW ${caught.id}").performClick()

        // then
        waitUntilDisplayed(POINT_DESCRIPTION_LABEL)
        composeRule.onNodeWithText("Odkryta tajemnica").assertIsDisplayed()
    }

    @Test
    fun explainThatAPointHasNotBeenCaughtYet() {
        // given
        val route = given(RouteArranger.routeWithPoints(2))
        openCollectedPoints(route.name)

        // when
        composeRule.onNodeWithContentDescription("$COLLECTED_POINT_ROW 2").performClick()

        // then
        waitUntilDisplayed(NOT_CAUGHT_INFO)
        composeRule.onNodeWithContentDescription(NOT_CAUGHT_INFO).assertIsDisplayed()
    }

    private fun openCollectedPoints(routeName: String) {
        waitUntilDisplayed("$SELECT_ROUTE_BUTTON $routeName")
        composeRule.onNodeWithContentDescription("$SELECT_ROUTE_BUTTON $routeName").performClick()
        waitUntilDisplayed(COLLECTED_POINTS_BUTTON)
        composeRule.onNodeWithContentDescription(COLLECTED_POINTS_BUTTON).performClick()
        waitUntilDisplayed(COLLECTED_POINTS_LIST)
    }
}
