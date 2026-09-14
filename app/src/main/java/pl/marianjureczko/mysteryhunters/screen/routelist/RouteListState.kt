package pl.marianjureczko.mysteryhunters.screen.routelist

import pl.marianjureczko.mysteryhunters.model.Route

data class RouteListState(
    val routes: List<Route> = emptyList(),
    val routeToDelete: Route? = null
)
