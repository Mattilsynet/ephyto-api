package no.mattilsynet.ephyto.api

class EphytoKeystorePropertySetter {
    private val ephytoKeystorePath = System.getenv("EPHYTO_KEYSTORE_PATH")
    private val ephytoKeystorePassword = System.getenv("EPHYTO_KEYSTORE_PASSWORD")

    fun setSystemJavaKeystore() {
        System.setProperty("javax.net.ssl.keyStore", ephytoKeystorePath)
        System.setProperty("javax.net.ssl.keyStorePassword", ephytoKeystorePassword)
    }
}
