package dev.ag6.libredesktop.model.connection

import kotlinx.serialization.Serializable

@Serializable
data class GraphConnectionData(
    val connection: ConnectionData,
    val graphData: List<GlucoseItem>
)