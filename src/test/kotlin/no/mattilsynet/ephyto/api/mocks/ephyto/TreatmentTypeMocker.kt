package no.mattilsynet.ephyto.api.mocks.ephyto

import _int.ippc.ephyto.TreatmentType
import javax.xml.datatype.DatatypeFactory

object TreatmentTypeMocker {

    fun createTreatmentType(): TreatmentType =
        TreatmentType().also { treatmentType ->
            treatmentType.code = "code"
            treatmentType.description = "description"
            treatmentType.isActive = true
            treatmentType.lang = "lang"
            treatmentType.lastModified = DatatypeFactory.newInstance().newXMLGregorianCalendar()
            treatmentType.level = 1
            treatmentType.parentCode = "parentCode"
        }

}
