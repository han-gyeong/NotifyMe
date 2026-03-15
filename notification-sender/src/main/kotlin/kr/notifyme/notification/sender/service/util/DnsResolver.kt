package kr.notifyme.notification.sender.service.util

import org.springframework.stereotype.Component
import org.xbill.DNS.MXRecord
import org.xbill.DNS.Name
import org.xbill.DNS.Type
import org.xbill.DNS.lookup.LookupSession

@Component
class DnsResolver {

    fun resolveMx(domain: String): List<String> {

        val lookupSession = LookupSession.defaultBuilder().build()
        val mxLookup = Name.fromString(domain)

        val answers = lookupSession
            .lookupAsync(mxLookup, Type.MX)
            .toCompletableFuture()
            .get()

        return answers.records
            .map { it as MXRecord }
            .sortedBy { it.priority }
            .map { it.target.toString(true) }
    }
}