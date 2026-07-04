package com.example.antiagingreminder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.antiagingreminder.data.local.entity.PlanEntity
import com.example.antiagingreminder.data.local.entity.ReminderTimeEntity
import kotlinx.coroutines.flow.Flow

/**
 * 计划与时间点的数据访问对象，提供增删改查能力。
 * 查询结果使用 Flow 暴露，保证界面层能响应式更新。
 *
 * 性能优化：添加 getAllActiveTimes 批量查询避免 N+1；
 * 复杂操作包裹在 @Transaction 中。
 */
@Dao
interface PlanDao {

    // ---------------- 计划 ----------------

    @Query("SELECT * FROM plans ORDER BY createdAt DESC")
    fun observeAllPlans(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plans WHERE isActive = 1")
    suspend fun getActivePlans(): List<PlanEntity>

    @Query("SELECT * FROM plans WHERE id = :id")
    suspend fun getPlanById(id: Long): PlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: PlanEntity): Long

    @Update
    suspend fun updatePlan(plan: PlanEntity)

    @Delete
    suspend fun deletePlan(plan: PlanEntity)

    @Query("DELETE FROM plans WHERE id = :id")
    suspend fun deletePlanById(id: Long)

    // ---------------- 时间点 ----------------

    @Query("SELECT * FROM reminder_times WHERE planId = :planId ORDER BY hour, minute")
    suspend fun getTimesForPlan(planId: Long): List<ReminderTimeEntity>

    /** 一次性查询全部活动计划的时间点（用于闹钟排程，避免 N+1） */
    @Query(
        """
        SELECT t.* FROM reminder_times t
        INNER JOIN plans p ON t.planId = p.id
        WHERE p.isActive = 1
        ORDER BY t.planId, t.hour, t.minute
        """
    )
    suspend fun getAllActiveTimes(): List<ReminderTimeEntity>

    @Query("SELECT * FROM reminder_times")
    suspend fun getAllTimes(): List<ReminderTimeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTime(time: ReminderTimeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimes(times: List<ReminderTimeEntity>)

    @Delete
    suspend fun deleteTime(time: ReminderTimeEntity)

    @Query("DELETE FROM reminder_times WHERE planId = :planId")
    suspend fun deleteTimesForPlan(planId: Long)

    /**
     * 事务：用新的时间点集合替换某计划的全部时间点。
     * 保留传入的 id（非 0 时复用旧 ID，避免历史记录孤立）；
     * id 为 0 时自动生成新 ID。
     */
    @Transaction
    suspend fun replaceTimes(planId: Long, times: List<ReminderTimeEntity>) {
        deleteTimesForPlan(planId)
        times.forEach { insertTime(it.copy(planId = planId)) }
    }
}
