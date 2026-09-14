package pl.marianjureczko.mysteryhunters.screen.routeeditor

import pl.marianjureczko.mysteryhunters.model.Route

data class RouteEditorState(
    val route: Route = Route(name = ""),
    val name: String = "",
    /** Id of the point being edited, null when a brand new point is being created. */
    val editedPointId: Int? = null,
    val draftLatitude: Double? = null,
    val draftLongitude: Double? = null,
    val draftDescription: String = "",
    val pointEditorOpen: Boolean = false,
    val listening: Boolean = false,
    val recognizedPartial: String = "",
    val pointToDelete: Int? = null,
    val messageId: Int? = null
) {
    val canSavePoint: Boolean
        get() = draftLatitude != null && draftLongitude != null

    /** The id shown to the user while editing: the existing one, or the one about to be given. */
    val editedPointDisplayId: Int
        get() = editedPointId ?: route.nextPointId()
}
