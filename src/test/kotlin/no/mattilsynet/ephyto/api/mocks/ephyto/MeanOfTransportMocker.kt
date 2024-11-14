package no.mattilsynet.ephyto.api.mocks.ephyto

import _int.ippc.ephyto.MeanOfTransport
import javax.xml.datatype.DatatypeFactory

object MeanOfTransportMocker {

    fun createMeanOfTransportMock(): MeanOfTransport =
        MeanOfTransport().also { meanOfTransport ->
            meanOfTransport.lang = "en"
            meanOfTransport.usedTransportMean = "Vessel"
            meanOfTransport.isActive = true
            meanOfTransport.modeCode = 1
            meanOfTransport.lastModified = DatatypeFactory.newInstance().newXMLGregorianCalendar()
        }

}

