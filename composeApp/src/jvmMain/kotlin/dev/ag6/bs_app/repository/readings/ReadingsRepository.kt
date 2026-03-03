package dev.ag6.bs_app.repository.readings

import dev.ag6.bs_app.model.reading.GlucoseReading

interface ReadingsRepository {
    suspend fun getCurrentReading(): GlucoseReading?

    suspend fun getGraphReadings(): List<GlucoseReading>

    //TODO: allow user to select which patient to get readings for if there's multiple
    suspend fun getPatientId(): String?
}