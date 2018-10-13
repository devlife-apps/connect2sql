package app.devlife.connect2sql.viewmodel

import android.content.Context
import java.lang.ref.WeakReference

/**
 * Can be added to [android.arch.lifecycle.ViewModel]s created by [ViewModelFactory] so that context
 * can be provided to said [android.arch.lifecycle.ViewModel]. [context] is immediately set
 * after construction.
 */
interface ContextAware {
    var context: WeakReference<Context>
}