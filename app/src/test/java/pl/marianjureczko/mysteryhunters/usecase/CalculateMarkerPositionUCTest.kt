package pl.marianjureczko.mysteryhunters.usecase

import com.ocadotechnology.gembus.test.someFloat
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset.offset
import org.junit.jupiter.api.Test
import pl.marianjureczko.mysteryhunters.usecase.CalculateMarkerPositionUC.Companion.FARTHEST_RENDERING_DISTANCE_IN_METERS
import pl.marianjureczko.mysteryhunters.usecase.CalculateMarkerPositionUC.Companion.NEAREST_RENDERING_DISTANCE_IN_METERS

class CalculateMarkerPositionUCTest {

    private val sut = CalculateMarkerPositionUC()
    private val tolerance = offset(0.01f)

    @Test
    fun `SHOULD place the marker straight ahead WHEN the phone points at the point`() {
        // given
        val bearing = 42f

        // when
        val actual = sut(bearing, bearing, 10f)

        // then
        assertThat(actual.x).isCloseTo(0f, tolerance)
        assertThat(actual.z).isCloseTo(-10f, tolerance)
    }

    @Test
    fun `SHOULD place the marker to the right WHEN the point is a quarter turn clockwise`() {
        // given
        val azimuth = 0f

        // when
        val actual = sut(90f, azimuth, 10f)

        // then
        assertThat(actual.x).isCloseTo(10f, tolerance)
        assertThat(actual.z).isCloseTo(0f, tolerance)
    }

    @Test
    fun `SHOULD place the marker to the left WHEN the point is a quarter turn anticlockwise`() {
        // given
        val azimuth = 0f

        // when
        val actual = sut(270f, azimuth, 10f)

        // then
        assertThat(actual.x).isCloseTo(-10f, tolerance)
        assertThat(actual.z).isCloseTo(0f, tolerance)
    }

    @Test
    fun `SHOULD place the marker behind WHEN the hunter faces away from the point`() {
        // given
        val azimuth = 0f

        // when
        val actual = sut(180f, azimuth, 10f)

        // then
        assertThat(actual.z).isCloseTo(10f, tolerance)
    }

    @Test
    fun `SHOULD not draw the marker closer than the nearest rendering distance`() {
        // given
        val tooClose = 0.1f

        // when
        val actual = sut(0f, 0f, tooClose)

        // then
        assertThat(-actual.z).isCloseTo(NEAREST_RENDERING_DISTANCE_IN_METERS, tolerance)
    }

    @Test
    fun `SHOULD not draw the marker farther than the catch range`() {
        // given
        val tooFar = 500f

        // when
        val actual = sut(0f, 0f, tooFar)

        // then
        assertThat(-actual.z).isCloseTo(FARTHEST_RENDERING_DISTANCE_IN_METERS, tolerance)
    }

    @Test
    fun `SHOULD keep the marker slightly below eye level`() {
        // given
        val distance = someFloat(NEAREST_RENDERING_DISTANCE_IN_METERS, FARTHEST_RENDERING_DISTANCE_IN_METERS)

        // when
        val actual = sut(someFloat(0f, 360f), someFloat(0f, 360f), distance)

        // then
        assertThat(actual.y).isLessThan(0f)
    }
}
