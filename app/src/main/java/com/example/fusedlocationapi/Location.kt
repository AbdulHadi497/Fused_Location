package com.example.fusedlocationapi

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Location {

    @PrimaryKey(autoGenerate = true)
    var userIds: Int = 0

    @ColumnInfo(name = "longitude")
    var longitude: String? = null

    @ColumnInfo(name = "latitude")
    var latitude: String? = null
}