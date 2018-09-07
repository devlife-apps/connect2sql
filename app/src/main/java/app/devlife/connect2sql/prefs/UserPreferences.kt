package app.devlife.connect2sql.prefs

import android.content.Context
import android.content.SharedPreferences

/**

 */
class UserPreferences(context: Context) {

    private val onChangeListeners: MutableMap<(String, Any?) -> Unit, String> = hashMapOf()
    private val sharedPreferences: SharedPreferences = context.applicationContext.getSharedPreferences(
        "user.prefs", Context.MODE_PRIVATE)
    private val sharedPrefsChangeListenerDelegate: (SharedPreferences, String) -> Unit = { _, s ->
        onChangeListeners.entries.filter { it.value.equals(s) }.forEach { it ->
            it.key(s, read(s, null))
        }
    }

    fun <T> read(key: String, default: T): T {
        val entry = sharedPreferences.all.entries.firstOrNull { it.key.equals(key) }
        return when {
            entry == null -> default
            entry.value == null -> default
            else -> entry.value as T
        }
    }

    fun <T> save(option: Option<T>) {
        when (option) {
            is Option.BooleanOption ->
                sharedPreferences.edit().putBoolean(option.key, option.value).apply()
            is Option.LongOption ->
                sharedPreferences.edit().putLong(option.key, option.value).apply()
            is Option.StringOption ->
                sharedPreferences.edit().putString(option.key, option.value).apply()
        }
    }

    fun <T> registerListener(key: String, onPreferenceChangeListener: (String, T?) -> Unit) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPrefsChangeListenerDelegate)
        onChangeListeners.put(onPreferenceChangeListener as (String, Any?) -> Unit, key)
    }

    fun <T> unregisterListener(onPreferenceChangeListener: (String, T?) -> Unit) {
        onChangeListeners.remove(onPreferenceChangeListener as (String, Any?) -> Unit)
        if (onChangeListeners.isEmpty()) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(
                sharedPrefsChangeListenerDelegate)
        }
    }

    sealed class Option<T>(val key: String, val value: T) {
        class BooleanOption(key: String, value: Boolean) : Option<Boolean>(key, value)
        class LongOption(key: String, value: Long) : Option<Long>(key, value)
        class StringOption(key: String, value: String) : Option<String>(key, value)
    }
}
