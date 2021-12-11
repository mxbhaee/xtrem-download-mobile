
package xtrem.download.mobile.core.storage.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import xtrem.download.mobile.core.model.data.entity.UserAgent;

import java.util.List;

@Dao
public interface UserAgentDao
{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void add(UserAgent agent);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void add(UserAgent[] agent);

    @Delete
    void delete(UserAgent agent);

    @Query("SELECT * FROM UserAgent")
    LiveData<List<UserAgent>> observeAll();
}
