package app.devlife.connect2sql.db.model.connection

data class Address(val host: String, val port: Int)
data class SshConfig(val address: Address, val authentication: Authentication)
data class SshProxy(val proxy: SshConfig, val serviceAddress: Address)

sealed class Authentication
data class None(val username: String) : Authentication()
data class BasicAuth(val username: String, val password: String?) : Authentication()
data class PrivateKey(val username: String, val privateKeyContents: String): Authentication()