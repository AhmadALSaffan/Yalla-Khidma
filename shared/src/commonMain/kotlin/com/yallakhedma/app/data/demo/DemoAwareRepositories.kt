package com.yallakhedma.app.data.demo

import com.yallakhedma.app.domain.model.Provider
import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.domain.repository.ProviderRepository
import com.yallakhedma.app.domain.repository.ServiceRepository
import com.yallakhedma.app.domain.util.DataResult
import com.yallakhedma.app.util.nowMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Lets the existing provider/service pages open demo listings (ids starting
 * with "demo-") without touching Firestore. Lists and writes go to the real
 * repository untouched, so demo data never mixes into live browsing.
 */
class DemoAwareProviderRepository(private val real: ProviderRepository) : ProviderRepository by real {
    override fun observeById(id: String): Flow<Provider?> =
        if (DemoDiscoveryCatalog.isDemoId(id)) flowOf(DemoDiscoveryCatalog.provider(id)) else real.observeById(id)

    override suspend fun incrementBookings(providerId: String): DataResult<Unit> =
        if (DemoDiscoveryCatalog.isDemoId(providerId)) DataResult.Success(Unit) else real.incrementBookings(providerId)
}

class DemoAwareServiceRepository(private val real: ServiceRepository) : ServiceRepository by real {
    private fun demoServices(): List<Service> {
        val today = Instant.fromEpochMilliseconds(nowMillis())
            .toLocalDateTime(TimeZone.of(DemoDiscoveryCatalog.TIME_ZONE)).date
        return DemoDiscoveryCatalog.services(today)
    }

    override fun observeByProvider(providerId: String): Flow<List<Service>> =
        if (DemoDiscoveryCatalog.isDemoId(providerId)) flowOf(demoServices().filter { it.providerId == providerId })
        else real.observeByProvider(providerId)

    override fun observeById(id: String): Flow<Service?> =
        if (DemoDiscoveryCatalog.isDemoId(id)) flowOf(demoServices().firstOrNull { it.id == id }) else real.observeById(id)
}
