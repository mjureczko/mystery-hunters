package pl.marianjureczko.mysteryhunters.usecase

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.marianjureczko.mysteryhunters.model.RouteArranger
import pl.marianjureczko.mysteryhunters.port.TestRouteStoragePort

class SelectPointToSearchUCTest {

    private val storage = TestRouteStoragePort()
    private val sut = SelectPointToSearchUC(storage)

    @Test
    fun `SHOULD navigate to the first point WHEN the route is opened for the first time`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3)

        // when
        val actual = sut(route)

        // then
        assertThat(actual.lastSelectedPointId).isEqualTo(route.firstPoint()?.id)
    }

    @Test
    fun `SHOULD restore the remembered point WHEN the route is reopened`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3)
        val remembered = route.pointsOfInterest[2]
        val reopened = route.copy(lastSelectedPointId = remembered.id)

        // when
        val actual = sut(reopened)

        // then
        assertThat(actual.lastSelectedPointId).isEqualTo(remembered.id)
    }

    @Test
    fun `SHOULD fall back to the first point WHEN the remembered one no longer exists`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(2)
        val stale = route.copy(lastSelectedPointId = route.nextPointId())

        // when
        val actual = sut(stale)

        // then
        assertThat(actual.lastSelectedPointId).isEqualTo(route.firstPoint()?.id)
    }

    @Test
    fun `SHOULD remember the choice WHEN the route is opened for the first time`() = runTest {
        // given
        val route = storage.save(RouteArranger.routeWithPoints(3))

        // when
        val actual = sut(route)

        // then
        assertThat(storage.stored(route.id)?.lastSelectedPointId).isEqualTo(actual.lastSelectedPointId)
    }

    @Test
    fun `SHOULD change nothing WHEN the route has no points`() = runTest {
        // given
        val route = RouteArranger.routeWithoutPoints()

        // when
        val actual = sut(route)

        // then
        assertThat(actual.lastSelectedPointId).isNull()
        assertThat(storage.saveCount).isZero()
    }
}
