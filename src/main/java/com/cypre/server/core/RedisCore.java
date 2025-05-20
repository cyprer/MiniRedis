package com.cypre.server.core;

import com.cypre.datastructure.RedisBytes;
import com.cypre.datastructure.RedisData;

import java.util.Set;

public interface RedisCore {
    Set<RedisBytes> keys();
    void put(RedisBytes key, RedisData value);
    RedisData get(RedisBytes key);
    void selectDB(int dbIndex);
    int getDBNum();
    int getCurrentDBIndex();
}

