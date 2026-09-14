package pl.marianjureczko.mysteryhunters.usecase

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.marianjureczko.mysteryhunters.model.RouteArranger
import pl.marianjureczko.mysteryhunters.port.TestRouteStoragePort

class DeletePointOfInterestUCTest {

    private val storage = TestRouteStoragePort()
    private val sut = DeletePointOfInterestUC(storage)

    @Test
    fun `SHOULD remove only the selected point`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3)
        val removed = route.pointsOfInterest[1]

        // when
        val actual = sut(route, removed.id)

        // then
        assertThat(actual.pointsOfInterest)
            .isEqualTo(route.pointsOfInterest.filter { it.id != removed.id })
    }

    @Test
    fun `SHOULD forget the remembered point WHEN the removed one was navigated to`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3)
        val removed = route.pointsOfInterest[2]
        val navigating = route.copy(lastSelectedPointId = removed.id)

        // when
        val actual = sut(navigating, removed.id)

        // then
        assertThat(actual.lastSelectedPointId).isNull()
    }

    @Test
    fun `SHOULD keep the remembered point WHEN another one was removed`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3)
        val remembered = route.pointsOfInterest[0]
        val navigating = route.copy(lastSelectedPointId = remembered.id)

        // when
        val actual = sut(navigating, route.pointsOfInterest[2].id)

        // then
        assertThat(actual.lastSelectedPointId).isEqualTo(remembered.id)
    }
}
