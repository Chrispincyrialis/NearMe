package com.nearme.data

import androidx.room.TypeConverter
import com.nearme.model.Channel
import com.nearme.model.Direction

class Converters {
    @TypeConverter
    fun directionToString(value: Direction): String = value.name

    @TypeConverter
    fun stringToDirection(value: String): Direction = Direction.valueOf(value)

    @TypeConverter
    fun channelToString(value: Channel): String = value.name

    @TypeConverter
    fun stringToChannel(value: String): Channel = Channel.valueOf(value)
}
