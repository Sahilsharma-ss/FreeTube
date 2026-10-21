package com.freetube.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.freetube.app.data.model.Bookmark
import com.freetube.app.data.model.HistoryItem

@Database(
    entities = [Bookmark::class, HistoryItem::class],
    version  = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao
}
