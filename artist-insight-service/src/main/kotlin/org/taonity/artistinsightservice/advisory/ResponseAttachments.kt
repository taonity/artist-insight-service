package org.taonity.artistinsightservice.advisory

import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

/**
 * Per-request accumulator for [Advisory] notices that should be returned to the
 * client alongside the regular response payload.
 *
 * Thread-safety: this bean is `@RequestScope` and therefore bound to the request
 * processing thread. It is NOT safe to access from:
 *  - `@Async` methods (the proxy resolves the request-scoped bean on the calling
 *    thread; the async worker thread has no active request scope),
 *  - reactive pipelines that hop threads,
 *  - background `@Scheduled` tasks.
 *
 * If a non-request-thread component needs to surface an advisory, either return
 * the advisory from the method and let the request thread add it, or use a
 * `RequestContextHolder.currentRequestAttributes()` lookup with explicit
 * propagation.
 */
@Component
@RequestScope
class ResponseAttachments {
    private val notices: MutableMap<Advisory, List<String>> = linkedMapOf()

    val advisories: MutableSet<Advisory> = object : AbstractMutableSet<Advisory>() {
        override val size: Int get() = notices.size
        override fun iterator() = notices.keys.iterator()
        override fun add(element: Advisory): Boolean {
            if (notices.containsKey(element)) return false
            notices[element] = emptyList()
            return true
        }
    }

    fun add(advisory: Advisory, vararg args: String) {
        notices[advisory] = args.toList()
    }

    fun advisoryDtos(): Set<AdvisoryDto> =
        notices.entries.map { (advisory, args) -> advisory.toDto(args) }.toSet()
}
