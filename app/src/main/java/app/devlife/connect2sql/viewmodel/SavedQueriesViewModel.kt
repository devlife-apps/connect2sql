package app.devlife.connect2sql.viewmodel

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import app.devlife.connect2sql.db.model.query.SavedQuery
import app.devlife.connect2sql.db.provider.ContentUriHelper
import javax.inject.Inject

class SavedQueriesViewModel @Inject constructor(
    var application: Application,
    var savedQuerySqlModel: SavedQuery.SavedQuerySqlModel
) : ViewModel() {

    private val savedQueries: MutableMap<Long, MutableLiveData<List<SavedQuery>>> = mutableMapOf()

    private lateinit var contentObserver: ContentObserver

    fun getSavedQueries(connectionInfoId: Long): LiveData<List<SavedQuery>?> {
        if (!::contentObserver.isInitialized) {
            contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    savedQueries.forEach { connectionInfoId, _ ->
                        refreshSavedQueries(connectionInfoId)
                    }
                }
            }
        }

        return savedQueries.getOrPut(connectionInfoId) {
            MutableLiveData<List<SavedQuery>>().apply {
                value = listOf()
            }
        }.also {
            refreshSavedQueries(connectionInfoId)
        }
    }

    private fun refreshSavedQueries(connectionInfoId: Long) {
        Thread {
            application.contentResolver?.query(
                SAVED_QUERY_CONTENT_URI,
                null,
                "${SavedQuery.Column.CONNECTION_ID}=$connectionInfoId",
                null,
                "${SavedQuery.Column.NAME} ASC")
                ?.let { cursor ->
                    val connectionInfoSavedQueries = mutableListOf<SavedQuery>()
                    while (cursor.moveToNext()) {
                        connectionInfoSavedQueries.add(savedQuerySqlModel.hydrateObject(cursor))
                    }
                    savedQueries.get(connectionInfoId)?.value = connectionInfoSavedQueries
                    cursor.close()
                }
        }.run()
    }

    companion object {
        private val SAVED_QUERY_CONTENT_URI = ContentUriHelper.getContentUri(SavedQuery.SavedQuerySqlModel::class.java)
    }
}