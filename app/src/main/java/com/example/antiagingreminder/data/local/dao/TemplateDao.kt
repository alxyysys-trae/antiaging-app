package com.example.antiagingreminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.antiagingreminder.data.local.entity.TemplateEntity
import kotlinx.coroutines.flow.Flow

/**
 * 模板数据访问对象。
 * 模板用于「一键添加常用计划」，仅查询与批量插入。
 */
@Dao
interface TemplateDao {

    @Query("SELECT * FROM templates ORDER BY id ASC")
    fun observeAll(): Flow<List<TemplateEntity>>

    @Query("SELECT COUNT(*) FROM templates")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(templates: List<TemplateEntity>)
}
