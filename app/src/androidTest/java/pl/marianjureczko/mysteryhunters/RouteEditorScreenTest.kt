package pl.marianjureczko.mysteryhunters

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.performTextInput
import com.ocadotechnology.gembus.test.someString
import dagger.hilt.android.testing.HiltAndroidTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import pl.marianjureczko.mysteryhunters.model.PointOfInterest
import pl.marianjureczko.mysteryhunters.model.RouteArranger
import androidx.compose.ui.semantics.SemanticsProperties
import pl.marianjureczko.mysteryhunters.screen.routeeditor.MICROPHONE_BUTTON
import pl.marianjureczko.mysteryhunters.screen.routeeditor.POINT_COORDINATES_LABEL
import pl.marianjureczko.mysteryhunters.screen.routeeditor.POINT_DESCRIPTION_FIELD
import pl.marianjureczko.mysteryhunters.screen.routeeditor.ROUTE_MAP
import pl.marianjureczko.mysteryhunters.screen.routeeditor.ROUTE_NAME_FIELD
import pl.marianjureczko.mysteryhunters.screen.routeeditor.SAVE_POINT_BUTTON
import pl.marianjureczko.mysteryhunters.screen.routeeditor.SAVE_ROUTE_NAME_BUTTON
import pl.marianjureczko.mysteryhunters.screen.routelist.ADD_ROUTE_BUTTON
import pl.marianjureczko.mysteryhunters.screen.routelist.EDIT_ROUTE_BUTTON

@HiltAndroidTest
class RouteEditorScreenTest : AbstractUiTest() {

    @Test
    fun createRouteWithName() {
        // given
        val name = someString()
        composeRule.onNodeWithContentDescription(ADD_ROUTE_BUTTON).performClick()
        waitUntilDisplayed(ROUTE_NAME_FIELD)

        // when
        composeRule.onNodeWithContentDescription(ROUTE_NAME_FIELD).performTextInput(name)
        composeRule.onNodeWithContentDescription(SAVE_ROUTE_NAME_BUTTON).performClick()

        // then
        composeRule.waitUntil(WAIT_TIMEOUT_IN_MILLIS) { storage.lastSaved?.name == name }
        assertThat(storage.lastSaved?.name).isEqualTo(name)
    }

    @Test
    fun addPointToRouteByTappingTheMap() {
        // given
        val route = given(RouteArranger.routeWithoutPoints())
        val description = someString()
        openEditorOf(route.name)

        // when
        composeRule.onNodeWithContentDescription(ROUTE_MAP).performClick()
        waitUntilDisplayed(POINT_DESCRIPTION_FIELD)
        composeRule.onNodeWithContentDescription(POINT_DESCRIPTION_FIELD).performTextInput(description)
        composeRule.onNodeWithContentDescription(SAVE_POINT_BUTTON).performClick()

        // then
        composeRule.waitUntil(WAIT_TIMEOUT_IN_MILLIS) {
            storedRoute(route.id)?.pointsOfInterest?.isNotEmpty() == true
        }
        val saved = storedRoute(route.id)!!.pointsOfInterest.single()
        assertThat(saved.id).isEqualTo(PointOfInterest.FIRST_ID)
        assertThat(saved.description).isEqualTo(description)
    }

    @Test
    fun editDescriptionAndCoordinatesOfExistingPoint() {
        // given
        val route = given(RouteArranger.routeWithPoints(2))
        val edited = route.pointsOfInterest.first()
        val newDescription = someString()
        openEditorOf(route.name)
        composeRule.onNodeWithContentDescription("Edit point ${edited.id}").performClick()
        waitUntilDisplayed(POINT_DESCRIPTION_FIELD)

        // when
        composeRule.onNodeWithContentDescription(POINT_DESCRIPTION_FIELD).performTextClearance()
        composeRule.onNodeWithContentDescription(POINT_DESCRIPTION_FIELD).performTextInput(newDescription)
        val originalCoordinates = displayedCoordinates()
        composeRule.onNodeWithContentDescription(ROUTE_MAP).performTouchInput {
            // away from the centre, which is where the map recentred on the edited point
            click(Offset(width * 0.25f, height * 0.25f))
        }
        // osmdroid only confirms a single tap once the double tap window has passed
        composeRule.waitUntil(WAIT_TIMEOUT_IN_MILLIS) { displayedCoordinates() != originalCoordinates }
        composeRule.onNodeWithContentDescription(SAVE_POINT_BUTTON).performClick()

        // then
        composeRule.waitUntil(WAIT_TIMEOUT_IN_MILLIS) {
            storedRoute(route.id)?.pointById(edited.id)?.description == newDescription
        }
        val saved = storedRoute(route.id)!!.pointById(edited.id)!!
        assertThat(saved.id).isEqualTo(edited.id)
        assertThat(saved.description).isEqualTo(newDescription)
        assertThat(saved.latitude).isNotEqualTo(edited.latitude)
    }

    @Test
    fun fallBackToTextInputWhenSpeechRecognitionIsNotSupported() {
        // given
        speechToTextPort.supported = false
        val route = given(RouteArranger.routeWithoutPoints())
        openEditorOf(route.name)
        composeRule.onNodeWithContentDescription(ROUTE_MAP).performClick()
        waitUntilDisplayed(POINT_DESCRIPTION_FIELD)

        // when
        composeRule.onNodeWithContentDescription(MICROPHONE_BUTTON).performClick()

        // then
        composeRule.onNodeWithContentDescription(POINT_DESCRIPTION_FIELD).assertIsDisplayed()
    }

    private fun displayedCoordinates(): String =
        composeRule.onNodeWithContentDescription(POINT_COORDINATES_LABEL)
            .fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .joinToString { it.text }

    private fun openEditorOf(routeName: String) {
        waitUntilDisplayed("$EDIT_ROUTE_BUTTON $routeName")
        composeRule.onNodeWithContentDescription("$EDIT_ROUTE_BUTTON $routeName").performClick()
        waitUntilDisplayed(ROUTE_MAP)
    }
}
