package no.mattilsynet.ephyto.api.mocks.nats

import no.mattilsynet.fisk.libs.reactivenats.ReactiveKeyValue
import no.mattilsynet.fisk.libs.reactivenats.ReactiveKeyValueEntry
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class ReactiveKeyValueMock : ReactiveKeyValue {
    override fun get(key: String): Mono<out ReactiveKeyValueEntry> = Mono.empty()

    override fun getAll(): Flux<out ReactiveKeyValueEntry> = Flux.empty()

    override fun keys(): Flux<String> = Flux.empty()

    override fun purge(key: String): Mono<String> = Mono.empty()

    override fun purgeAll(): Flux<String> = Flux.empty()

    override fun put(key: String, value: ByteArray): Mono<Long> = Mono.just(0)

    override fun put(key: String, value: Number): Mono<Long> = Mono.just(0)
}

