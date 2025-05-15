package com.cypre.server.core;

import com.cypre.datastructure.RedisData;

import java.util.Set;

public interface RedisCore {
    Set<byte[]> keys();
    void put(byte[] key, RedisData value);
    RedisData get(byte[] key);
    void selectDB(int dbIndex);
    int getDBNum();
    int getCurrentDBIndex();
}
