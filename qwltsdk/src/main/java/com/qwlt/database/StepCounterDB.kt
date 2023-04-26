package com.qwlt.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_count_table")
data class StepCounterDB(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    var date: String = "",
    var steps: Float = 0f,

)
