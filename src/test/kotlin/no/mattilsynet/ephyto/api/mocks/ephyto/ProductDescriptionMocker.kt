package no.mattilsynet.ephyto.api.mocks.ephyto

import _int.ippc.ephyto.ProductDescription
import javax.xml.datatype.DatatypeFactory

object ProductDescriptionMocker {
    fun createProductDescriptionMock(
        lang: String = "en",
    ): ProductDescription =
        ProductDescription().also { productDescription ->
            productDescription.code = "code"
            productDescription.isActive = true
            productDescription.lang = lang
            productDescription.lastModified = DatatypeFactory.newInstance().newXMLGregorianCalendar()
            productDescription.name = "name - $lang"
        }
}
