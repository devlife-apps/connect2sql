package app.devlife.connect2sql.db.model.connection

import app.devlife.connect2sql.sql.DriverType

data class ConnectionInfo(
    val id: Long,
    val name: String,
    val driverType: DriverType,
    val host: String,
    val port: Int,
    val username: String,
    val password: String?,
    val database: String?,
    val options: Map<String, String>
) {

    companion object {
        const val OPTION_INSTANCE = "instance"
        const val OPTION_DOMAIN = "domain"
        const val OPTION_USE_SSL = "use_ssl"
        const val OPTION_TRUST_CERT = "bypass_cert_validation"
    }
}