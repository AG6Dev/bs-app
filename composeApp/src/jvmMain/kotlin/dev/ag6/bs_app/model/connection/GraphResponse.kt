package dev.ag6.bs_app.model.connection

import kotlinx.serialization.Serializable

@Serializable
data class GraphResponse(
    val data: GraphConnectionData
)

@Serializable
data class GraphConnectionData(
    val connection: ConnectionData,
    val graphData: List<GlucoseItem>
)