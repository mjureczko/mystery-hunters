package pl.marianjureczko.mysteryhunters

import android.Manifest
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
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
    }
}
