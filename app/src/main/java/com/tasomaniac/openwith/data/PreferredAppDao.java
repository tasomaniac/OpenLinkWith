package com.tasomaniac.openwith.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import java.util.List;

@Dao
public interface PreferredAppDao {

  @Query("SELECT * FROM openwith WHERE preferred = 1")
  Flowable<List<PreferredApp>> allPreferredApps();

  @Query("SELECT * FROM openwith WHERE _id = :id")
  Maybe<PreferredApp> preferredAppById(long id);

  @Query("SELECT * FROM openwith WHERE host = :host")
  Flowable<List<PreferredApp>> preferredAppsByHost(String host);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(PreferredApp preferredApp);
}
