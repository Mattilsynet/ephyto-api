package no.mattilsynet.ephyto.api.nats.jetstream.subjects

import no.mattilsynet.fisk.libs.nats.virtual.JetStreamSubject


object JetStreamSubjectBuilder {

    fun ephytoImportEnvelopesV1() =
        JetStreamSubject("mattilsynet.ephyto.import.envelopes.v1", "ephyto_import_envelopes_v1")

    fun ephytoImportEnvelopesFailedV1() =
        JetStreamSubject("mattilsynet.ephyto.import.envelopes.failed.v1", "ephyto_import_envelopes_failed_v1")

}
