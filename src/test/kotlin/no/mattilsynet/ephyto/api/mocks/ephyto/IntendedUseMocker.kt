package no.mattilsynet.ephyto.api.mocks.ephyto

import _int.ippc.ephyto.IntendedUse
import javax.xml.datatype.DatatypeFactory

object IntendedUseMocker {

    fun createIntendedUseMock(
        lang: String = "en",
    ): IntendedUse =
        IntendedUse().also { intendedUse ->
            intendedUse.code = "code"
            intendedUse.isActive = true
            intendedUse.lang = lang
            intendedUse.lastModified = DatatypeFactory.newInstance().newXMLGregorianCalendar()
            intendedUse.name = "name - $lang"
        }

}
