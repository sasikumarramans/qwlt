package com.qwlt.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface StepCounterDao {
    @Insert
    fun insert(day: StepCounterDB)

    @Update
    fun update(day: StepCounterDB)

    @Query("UPDATE step_count_table SET steps = steps + :stepsToAdd WHERE id IN(SELECT id FROM step_count_table ORDER BY date DESC LIMIT 1)")
    fun addLatestSteps(stepsToAdd: Float)

    @Query("SELECT * FROM step_count_table ORDER BY date DESC")
    fun getAllObservable(): LiveData<List<StepCounterDB>>

    @Query("SELECT * FROM step_count_table WHERE date = :key")
    fun getStepCountByDate(key:String): List<StepCounterDB>
/*
    @Query("SELECT * FROM step_count_table WHERE substr(date,1,length(:startDate)) BETWEEN :startDate AND :endDate;")
*/
    @Query("SELECT * FROM step_count_table WHERE date BETWEEN :startDate AND :endDate")
    fun getStepCountByDate(startDate:String,endDate:String): List<StepCounterDB>

    @Query("SELECT * FROM step_count_table ORDER BY date DESC LIMIT 1")
    fun getLatestObservable(): StepCounterDB

}