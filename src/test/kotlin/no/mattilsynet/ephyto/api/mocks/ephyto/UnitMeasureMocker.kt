package no.mattilsynet.ephyto.api.mocks.ephyto

import _int.ippc.ephyto.UnitMeasure
import javax.xml.datatype.DatatypeFactory

object UnitMeasureMocker {
    fun createUnitMeasureMock(): UnitMeasure =
        UnitMeasure().also { unitMeasure ->
            unitMeasure.code = "code"
            unitMeasure.isActive = true
            unitMeasure.lastModified = DatatypeFactory.newInstance().newXMLGregorianCalendar()
            unitMeasure.name = "name"
            unitMeasure.symbol = "kg/m"
        }
}
