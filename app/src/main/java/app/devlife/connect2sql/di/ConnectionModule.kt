package app.devlife.connect2sql.di

import android.content.Context
import app.devlife.connect2sql.connection.ConnectionAgent
import app.devlife.connect2sql.connection.SshTunnelAgent
import com.jcraft.jsch.JSch
import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Singleton

@Module
class ConnectionModule(val context: Context) {

    @Provides
    @Singleton
    internal fun provideConnectionAgent(sshTunnelAgent: SshTunnelAgent): ConnectionAgent {
        return ConnectionAgent(sshTunnelAgent)
    }

    @Provides
    @Singleton
    internal fun provideSshTunnelAgent(jSch: JSch): SshTunnelAgent {
        return SshTunnelAgent(jSch)
    }

    @Provides
    @Singleton
    internal fun provideJsch(): JSch {
        val jSch = JSch()

        File("${context.filesDir.absolutePath}/known_hosts")
            .also { it.createNewFile() }
            .also { jSch.setKnownHosts(it.absolutePath) }

        return jSch
    }
}
