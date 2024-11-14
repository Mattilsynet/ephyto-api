package no.mattilsynet.ephyto.api.mocks.ephyto

import _int.ippc.ephyto.Statement
import javax.xml.datatype.DatatypeFactory

object StatementMocker {

    fun createStatementMock(): Statement =
        Statement().also { statement ->
            statement.code = "code"
            statement.docTypeOnly = 0
            statement.isActive = true
            statement.lang = "DK"
            statement.lastModified = DatatypeFactory.newInstance().newXMLGregorianCalendar()
            statement.text = "text"
        }

}

