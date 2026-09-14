package pl.marianjureczko.mysteryhunters.usecase

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.marianjureczko.mysteryhunters.model.RouteArranger
import pl.marianjureczko.mysteryhunters.port.TestRouteStoragePort

class SelectNextPointUCTest {

    private val storage = TestRouteStoragePort()
    private val sut = SelectNextPointUC(storage)

    @Test
    fun `SHOULD move to the following point`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3).copy(lastSelectedPointId = 1)

        // when
        val actual = sut(route)

        // then
        assertThat(actual.lastSelectedPointId).isEqualTo(2)
    }

    @Test
    fun `SHOULD wrap around to the first point WHEN the last one is selected`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3).copy(lastSelectedPointId = 3)

        // when
        val actual = sut(route)

        // then
        assertThat(actual.lastSelectedPointId).isEqualTo(1)
    }

    @Test
    fun `SHOULD start from the first point WHEN nothing is selected yet`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3)

        // when
        val actual = sut(route)

        // then
        assertThat(actual.lastSelectedPointId).isEqualTo(1)
    }

    @Test
    fun `SHOULD persist the new selection`() = runTest {
        // given
        val route = storage.save(RouteArranger.routeWithPoints(2).copy(lastSelectedPointId = 1))

        // when
        val actual = sut(route)

        // then
        assertThat(storage.stored(route.id)).isEqualTo(actual)
    }

    @Test
    fun `SHOULD change nothing WHEN the route has no points`() = runTest {
        // given
        val route = RouteArranger.routeWithoutPoints()

        // when
        val actual = sut(route)

        // then
        assertThat(actual).isEqualTo(route)
        assertThat(storage.saveCount).isZero()
    }
}
