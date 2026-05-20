package org.taonity.artistinsightservice.observability.logging

import org.hibernate.boot.Metadata
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.event.service.spi.EventListenerRegistry
import org.hibernate.event.spi.EventType
import org.hibernate.integrator.spi.Integrator
import org.hibernate.service.spi.SessionFactoryServiceRegistry

class EntityChangeLoggingIntegrator : Integrator {

    override fun integrate(
        metadata: Metadata,
        bootstrapContext: org.hibernate.boot.spi.BootstrapContext,
        sessionFactory: SessionFactoryImplementor
    ) {
        // Verbose per-entity change logging is dev-only — gate on the `local` profile so
        // production sessions don't pay the listener overhead or leak entity state to logs.
        if (!isLocalProfileActive()) {
            return
        }

        val registry = sessionFactory.serviceRegistry.getService(EventListenerRegistry::class.java)
            ?: return

        val listener = EntityChangeLoggingListener()

        registry.appendListeners(EventType.POST_INSERT, listener)
        registry.appendListeners(EventType.POST_UPDATE, listener)
        registry.appendListeners(EventType.POST_DELETE, listener)
    }

    override fun disintegrate(sessionFactory: SessionFactoryImplementor, serviceRegistry: SessionFactoryServiceRegistry) {
        // nothing to clean up
    }

    private fun isLocalProfileActive(): Boolean {
        val raw = System.getProperty("spring.profiles.active")
            ?: System.getenv("SPRING_PROFILES_ACTIVE")
            ?: return false
        return raw.split(',').any { it.trim().equals("local", ignoreCase = true) }
    }
}

