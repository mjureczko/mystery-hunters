package pl.marianjureczko.mysteryhunters.model

/**
 * A single point of interest belonging to a [Route].
 *
 * The [id] is a natural number, unique within the route, assigned automatically when the point is
 * created and never changed afterwards. It is presented to the user.
 */
data class PointOfInterest(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val caught: Boolean = false
) {
    companion object {
        const val FIRST_ID = 1
    }
}
