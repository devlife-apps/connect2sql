package app.devlife.connect2sql.db.model.connection

data class Address(val host: String, val port: Int)
data class SshConfig(val address: Address, val authentication: Authentication)
data class SshTunnelConfig(val proxy: SshConfig, val serviceAddress: Address)

sealed class Authentication(val username: String)
data class None(private val username_: String) : Authentication(username_)
data class BasicAuth(private val username_: String, val password: String?) : Authentication(username_)
data class PrivateKey(private val username_: String, val passphrase: String?, val privateKeyContents: String) : Authentication(username_)