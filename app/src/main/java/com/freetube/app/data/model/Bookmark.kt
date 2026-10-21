package com.freetube.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val thumbnail: String = "",
    val channelName: String = "",
    val savedAt: Long = System.currentTimeMillis()
)
