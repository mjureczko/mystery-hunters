package pl.marianjureczko.mysteryhunters.screen

object Screens {

    object RouteList {
        const val ROUTE = "routes"
    }

    object RouteEditor {
        const val PARAMETER_ROUTE_ID = "route_id"
        const val NEW_ROUTE = 0L
        private const val PATH = "routeeditor"
        const val ROUTE = "$PATH/{$PARAMETER_ROUTE_ID}"

        fun doRoute(routeId: Long): String = "$PATH/$routeId"
    }

    object Searching {
        const val PARAMETER_ROUTE_ID = "route_id"
        private const val PATH = "searching"
        const val ROUTE = "$PATH/{$PARAMETER_ROUTE_ID}"

        fun doRoute(routeId: Long): String = "$PATH/$routeId"
    }

    object Camera {
        const val PARAMETER_ROUTE_ID = "route_id"
        const val PARAMETER_POINT_ID = "point_id"
        private const val PATH = "camera"
        const val ROUTE = "$PATH/{$PARAMETER_ROUTE_ID}/{$PARAMETER_POINT_ID}"

        fun doRoute(routeId: Long, pointId: Int): String = "$PATH/$routeId/$pointId"
    }

    object PointDetail {
        const val PARAMETER_ROUTE_ID = "route_id"
        const val PARAMETER_POINT_ID = "point_id"
        private const val PATH = "point"
        const val ROUTE = "$PATH/{$PARAMETER_ROUTE_ID}/{$PARAMETER_POINT_ID}"

        fun doRoute(routeId: Long, pointId: Int): String = "$PATH/$routeId/$pointId"
    }

    object CollectedPoints {
        const val PARAMETER_ROUTE_ID = "route_id"
        private const val PATH = "collected"
        const val ROUTE = "$PATH/{$PARAMETER_ROUTE_ID}"

        fun doRoute(routeId: Long): String = "$PATH/$routeId"
    }
}
