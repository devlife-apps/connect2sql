package app.devlife.connect2sql.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class ViewModelFactory
@Inject
constructor(
    private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val creator = creators[modelClass] ?: findCompatibleCreator(modelClass)
        ?: throw IllegalArgumentException("Unknown model class $modelClass")

        @Suppress("UNCHECKED_CAST")
        return creator.get() as T
    }

    private fun <T : ViewModel> findCompatibleCreator(modelClass: Class<T>):
        @JvmSuppressWildcards Provider<ViewModel>? {

        for ((key, value) in creators) {
            if (modelClass.isAssignableFrom(key)) {
                return value
            }
        }

        return null
    }
}