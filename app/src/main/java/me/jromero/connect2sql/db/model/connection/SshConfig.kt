package me.jromero.connect2sql.db.model.connection

data class Address(val host: String, val port: Int)
data class SshConfig(val address: Address, val authentication: Authentication)
data class SshProxy(val proxy: SshConfig, val serviceAddress: Address)

interface Authentication
data class BasicAuth(val username: String, val password: String) : Authentication