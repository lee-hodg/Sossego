package com.example.android.sossego.database.quotes.database

import androidx.room.TypeConverter
import java.text.SimpleDateFormat

class Converter{
    companion object{

        @TypeConverter
        @JvmStatic
        fun toDate(value:String): Long{
            return SimpleDateFormat("yyyy-MM-dd").parse(value).time
        }

        @TypeConverter
        @JvmStatic
        fun fromDate(value:Long):String{
            return SimpleDateFormat("yyyy-MM-dd")
                .format(value).toString()
        }
    }
}