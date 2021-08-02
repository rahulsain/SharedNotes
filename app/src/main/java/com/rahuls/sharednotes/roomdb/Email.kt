package com.rahuls.sharednotes.roomdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "email_table")
class Email(@ColumnInfo(name = "text") val text: String) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}