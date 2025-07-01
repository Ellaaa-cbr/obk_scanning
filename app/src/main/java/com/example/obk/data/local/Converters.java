package com.example.obk.data.local;

import androidx.room.TypeConverter;

import java.util.Date;

public class Converters {

    /**
     * Convert a Long timestamp (milliseconds since epoch) from the database
     * into a Java Date object.
     *
     * @param value the timestamp value, or null
     * @return the corresponding Date, or null if value was null
     */
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    /**
     * Convert a Java Date object into a Long timestamp for database storage.
     *
     * @param date the Date to convert, or null
     * @return milliseconds since epoch, or null if date was null
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
