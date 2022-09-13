package com.example.fusedlocationapi

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query


@Dao
interface UserDao {
    @Query("select * from Location where userIds = :userIds")
    fun get(userIds: String): Location

//    @Query("Select * From Location Where userId In (:userIds)")
//    fun loadAllByIds(userIds : IntArray): List<Location>

    @Insert
    fun insertAll(location: Location) : Long

    @Delete
    fun delete(location: Location)
}