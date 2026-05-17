package com.example.browser.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.browser.data.local.dao.BookmarkDao
import com.example.browser.data.local.dao.HistoryDao
import com.example.browser.data.local.dao.TabDao
import com.example.browser.data.local.entity.BookmarkEntity
import com.example.browser.data.local.entity.HistoryEntity
import com.example.browser.data.local.entity.TabEntity

@Database(
    entities = [
        BookmarkEntity::class,
        HistoryEntity::class,
        TabEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BrowserDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao
    abstract fun tabDao(): TabDao
}
