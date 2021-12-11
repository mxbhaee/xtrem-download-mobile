
package xtrem.download.mobile.core.storage.converter;

import androidx.room.TypeConverter;

import java.util.UUID;

public class UUIDConverter
{
    @TypeConverter
    public static UUID toUUID(String uuidStr)
    {
        if (uuidStr == null)
            return null;

        UUID uuid = null;
        try {
            uuid = UUID.fromString(uuidStr);

        } catch (IllegalArgumentException e) {
            return null;
        }

        return uuid;
    }

    @TypeConverter
    public static String fromUUID(UUID uuid)
    {
        return uuid == null ? null : uuid.toString();
    }
}
