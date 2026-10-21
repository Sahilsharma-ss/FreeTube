package com.freetube.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val thumbnail: String = "",
    val channelName: String = "",
    val watchedAt: Long = System.currentTimeMillis()
)
