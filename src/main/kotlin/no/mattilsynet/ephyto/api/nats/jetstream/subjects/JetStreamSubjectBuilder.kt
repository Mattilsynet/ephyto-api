package no.mattilsynet.ephyto.api.nats.jetstream.subjects


object JetStreamSubjectBuilder {

        fun ephytoImportEnvelopesV1() = JetStreamSubject("mattilsynet.ephyto.import.envelopes.v1")

        fun ephytoImportEnvelopesFailedV1() = JetStreamSubject("mattilsynet.ephyto.import.envelopes.failed.v1")

}
