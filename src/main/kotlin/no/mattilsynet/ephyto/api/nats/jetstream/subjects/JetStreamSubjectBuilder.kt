package no.mattilsynet.ephyto.api.nats.jetstream.subjects

import no.mattilsynet.virtualnats.virtualnatscore.nats.virtual.JetStreamSubject


object JetStreamSubjectBuilder {

    fun ephytoImportEnvelopesV1() =
        JetStreamSubject("mattilsynet.ephyto.import.envelopes.v1", "ephyto_import_envelopes_v1")

    fun ephytoImportEnvelopesFailedV1(hubDeliveryNumber: String) =
        JetStreamSubject(
            stream = "ephyto_import_envelopes_failed_v1",
            subject = "mattilsynet.ephyto.import.envelopes.failed.v1.$hubDeliveryNumber",
        )

}
