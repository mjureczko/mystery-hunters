package pl.marianjureczko.mysteryhunters.model

/**
 * A route the user hunts along. Routes are stored locally; [id] is assigned by the storage.
 *
 * [lastSelectedPointId] remembers which point was navigated to most recently, so that reopening
 * the searching screen resumes where the hunter left off.
 */
data class Route(
    val id: Long = NOT_PERSISTED,
    val name: String,
    val pointsOfInterest: List<PointOfInterest> = emptyList(),
    val lastSelectedPointId: Int? = null
) {
    companion object {
        const val NOT_PERSISTED = 0L
    }

    val isPersisted: Boolean
        get() = id != NOT_PERSISTED

    val caughtCount: Int
        get() = pointsOfInterest.count { it.caught }

    val pointsCount: Int
        get() = pointsOfInterest.size

    val allCaught: Boolean
        get() = pointsOfInterest.isNotEmpty() && pointsOfInterest.all { it.caught }

    /**
     * Continues after the highest id ever used on the route, so removing a point from the middle
     * never renumbers the points that stay. The user sees these ids, and they are immutable.
     */
    fun nextPointId(): Int =
        (pointsOfInterest.maxOfOrNull { it.id } ?: (PointOfInterest.FIRST_ID - 1)) + 1

    fun pointById(id: Int?): PointOfInterest? =
        if (id == null) null else pointsOfInterest.find { it.id == id }

    fun firstPoint(): PointOfInterest? = pointsOfInterest.minByOrNull { it.id }

    fun firstNotCaughtPoint(): PointOfInterest? =
        pointsOfInterest.filter { !it.caught }.minByOrNull { it.id }

    /**
     * The point following [pointId] in id order, wrapping around to the first one. Used by the
     * "change current point" button on the searching screen.
     */
    fun pointAfter(pointId: Int?): PointOfInterest? {
        val ordered = pointsOfInterest.sortedBy { it.id }
        if (ordered.isEmpty()) {
            return null
        }
        val currentIndex = ordered.indexOfFirst { it.id == pointId }
        return ordered[(currentIndex + 1) % ordered.size]
    }

    fun withPoint(point: PointOfInterest): Route =
        copy(pointsOfInterest = (pointsOfInterest.filter { it.id != point.id } + point).sortedBy { it.id })

    fun withoutPoint(pointId: Int): Route =
        copy(pointsOfInterest = pointsOfInterest.filter { it.id != pointId })
}
