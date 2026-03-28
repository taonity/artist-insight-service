package org.taonity.artistinsightservice.infrastructure.logging

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EndpointLogLevel(val value: LogLevel = LogLevel.INFO)

