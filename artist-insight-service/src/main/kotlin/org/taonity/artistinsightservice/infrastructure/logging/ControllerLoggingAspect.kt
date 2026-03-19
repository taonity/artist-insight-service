package org.taonity.artistinsightservice.infrastructure.logging

import mu.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*

@Aspect
@Component
class ControllerLoggingAspect {

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    fun restControllerBean() {}

    @Pointcut(
        "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
        "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
        "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
        "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
        "@annotation(org.springframework.web.bind.annotation.PatchMapping) || " +
        "@annotation(org.springframework.web.bind.annotation.RequestMapping)"
    )
    fun requestMappingMethod() {}

    @Around("restControllerBean() && requestMappingMethod()")
    fun logControllerMethod(joinPoint: ProceedingJoinPoint): Any? {
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method
        val className = joinPoint.target.javaClass.simpleName
        val methodName = method.name

        val httpMethod = resolveHttpMethod(method)
        val path = resolvePath(joinPoint.target.javaClass, method)

        LOGGER.debug { "$httpMethod $path -> $className.$methodName()" }

        val startTime = System.currentTimeMillis()
        return try {
            val result = joinPoint.proceed()
            val elapsed = System.currentTimeMillis() - startTime
            LOGGER.info { "$httpMethod $path -> $className.$methodName() completed in ${elapsed}ms" }
            result
        } catch (ex: Throwable) {
            val elapsed = System.currentTimeMillis() - startTime
            LOGGER.warn { "$httpMethod $path -> $className.$methodName() failed in ${elapsed}ms with ${ex.javaClass.simpleName}" }
            throw ex
        }
    }

    private fun resolveHttpMethod(method: java.lang.reflect.Method): String {
        return when {
            method.isAnnotationPresent(GetMapping::class.java) -> "GET"
            method.isAnnotationPresent(PostMapping::class.java) -> "POST"
            method.isAnnotationPresent(PutMapping::class.java) -> "PUT"
            method.isAnnotationPresent(DeleteMapping::class.java) -> "DELETE"
            method.isAnnotationPresent(PatchMapping::class.java) -> "PATCH"
            method.isAnnotationPresent(RequestMapping::class.java) -> {
                val mapping = method.getAnnotation(RequestMapping::class.java)
                mapping.method.firstOrNull()?.name ?: "REQUEST"
            }
            else -> "UNKNOWN"
        }
    }

    private fun resolvePath(controllerClass: Class<*>, method: java.lang.reflect.Method): String {
        val classLevelPath = controllerClass.getAnnotation(RequestMapping::class.java)
            ?.value?.firstOrNull()
            ?: controllerClass.getAnnotation(RequestMapping::class.java)
                ?.path?.firstOrNull()
            ?: ""

        val methodLevelPath = when {
            method.isAnnotationPresent(GetMapping::class.java) ->
                method.getAnnotation(GetMapping::class.java).value.firstOrNull()
                    ?: method.getAnnotation(GetMapping::class.java).path.firstOrNull()
            method.isAnnotationPresent(PostMapping::class.java) ->
                method.getAnnotation(PostMapping::class.java).value.firstOrNull()
                    ?: method.getAnnotation(PostMapping::class.java).path.firstOrNull()
            method.isAnnotationPresent(PutMapping::class.java) ->
                method.getAnnotation(PutMapping::class.java).value.firstOrNull()
                    ?: method.getAnnotation(PutMapping::class.java).path.firstOrNull()
            method.isAnnotationPresent(DeleteMapping::class.java) ->
                method.getAnnotation(DeleteMapping::class.java).value.firstOrNull()
                    ?: method.getAnnotation(DeleteMapping::class.java).path.firstOrNull()
            method.isAnnotationPresent(PatchMapping::class.java) ->
                method.getAnnotation(PatchMapping::class.java).value.firstOrNull()
                    ?: method.getAnnotation(PatchMapping::class.java).path.firstOrNull()
            method.isAnnotationPresent(RequestMapping::class.java) ->
                method.getAnnotation(RequestMapping::class.java).value.firstOrNull()
                    ?: method.getAnnotation(RequestMapping::class.java).path.firstOrNull()
            else -> null
        } ?: ""

        return "$classLevelPath$methodLevelPath".ifEmpty { "/" }
    }
}
