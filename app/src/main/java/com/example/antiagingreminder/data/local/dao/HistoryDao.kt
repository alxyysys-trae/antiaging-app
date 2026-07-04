package com.example.antiagingreminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.antiagingreminder.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 历史记录数据访问对象。
 * 负责记录与查询每条提醒在指定日期的完成状态。
 */
@Dao
interface HistoryDao {

    /** 观察某一天的全部完成记录 */
    @Query("SELECT * FROM history WHERE date = :date")
    fun observeByDate(date: String): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE date = :date")
    suspend fun getByDate(date: String): List<HistoryEntity>

    /** 查询某日期范围内有记录的日期，用于历史页面日历标记 */
    @Query("SELECT DISTINCT date FROM history ORDER BY date DESC")
    fun observeDates(): Flow<List<String>>

    /** 插入或更新完成记录（按唯一索引冲突时替换） */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(history: HistoryEntity)

    @Query(
        "SELECT * FROM history WHERE planId = :planId AND reminderTimeId = :reminderTimeId AND date = :date LIMIT 1"
    )
    suspend fun get(
        planId: Long,
        reminderTimeId: Long,
        date: String
    ): HistoryEntity?

    /**
     * 事务内原子切换完成状态：读取旧值 → 取反 → 写入。
     * 避免非事务的读-改-写竞态（快速连续点击两次可能都读到同一旧值）。
     */
    @Transaction
    suspend fun toggleComplete(planId: Long, reminderTimeId: Long, date: String, now: Long) {
        val existing = get(planId, reminderTimeId, date)
        val nowCompleted = !(existing?.isCompleted ?: false)
        upsert(
            HistoryEntity(
                id = existing?.id ?: 0,
                planId = planId,
                reminderTimeId = reminderTimeId,
                date = date,
                isCompleted = nowCompleted,
                completedAt = if (nowCompleted) now else null
            )
        )
    }
}
