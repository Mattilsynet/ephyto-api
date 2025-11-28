package no.mattilsynet.ephyto.api.mocks.ephyto

import _int.ippc.ephyto.Condition
import javax.xml.datatype.DatatypeFactory

object ConditionMocker {
    fun createConditionMock(
        lang: String = "en",
    ): Condition =
        Condition().also { condition ->
            condition.code = "code"
            condition.isActive = true
            condition.lang = lang
            condition.lastModified = DatatypeFactory.newInstance().newXMLGregorianCalendar()
            condition.name = "name - $lang"
        }
}
