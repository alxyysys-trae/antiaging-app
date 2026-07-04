package com.example.antiagingreminder.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.antiagingreminder.data.local.dao.HistoryDao
import com.example.antiagingreminder.data.local.dao.PlanDao
import com.example.antiagingreminder.data.local.dao.TemplateDao
import com.example.antiagingreminder.data.local.entity.HistoryEntity
import com.example.antiagingreminder.data.local.entity.PlanEntity
import com.example.antiagingreminder.data.local.entity.ReminderTimeEntity
import com.example.antiagingreminder.data.local.entity.TemplateEntity
import com.example.antiagingreminder.data.template.TemplateProvider

/**
 * Room 本地数据库。
 * 首次创建时通过 Callback 预置模板数据，便于用户快速添加常用计划。
 *
 * 注意：onCreate 回调执行时数据库实例尚未完成构建（INSTANCE 为 null），
 * 因此不能通过 DAO 访问，而是直接用 SupportSQLiteDatabase 执行原始 SQL 插入。
 */
@Database(
    entities = [
        PlanEntity::class,
        ReminderTimeEntity::class,
        HistoryEntity::class,
        TemplateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun planDao(): PlanDao
    abstract fun historyDao(): HistoryDao
    abstract fun templateDao(): TemplateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** 单例获取数据库，首次创建时预填充模板 */
        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anti_aging.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // 直接用原始 SQL 插入模板，避免访问尚未初始化的 DAO
                            prepopulateTemplates(db)
                        }
                    })
                    // 移除 fallbackToDestructiveMigration：版本升级时不应静默删除用户数据
                    // 未来版本升级需提供显式 Migration
                    .build()
                    .also { INSTANCE = it }
            }

        /**
         * 在数据库首次创建时，用原始 SQL 批量插入模板数据。
         * 此时不依赖 DAO（数据库对象尚未完成构建），直接操作 SupportSQLiteDatabase。
         */
        private fun prepopulateTemplates(db: SupportSQLiteDatabase) {
            TemplateProvider.allTemplates().forEach { t ->
                db.execSQL(
                    """
                    INSERT INTO templates (title, description, type, times, repeatDays)
                    VALUES (?, ?, ?, ?, ?)
                    """.trimIndent(),
                    arrayOf(
                        t.title,
                        t.description,
                        t.type,
                        t.times,
                        t.repeatDays
                    )
                )
            }
        }
    }
}
