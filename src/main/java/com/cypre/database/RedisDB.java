package com.cypre.database;

import com.cypre.datastructure.RedisBytes;
import com.cypre.datastructure.RedisData;
import com.cypre.internal.Dict;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
public class RedisDB {
    private final Dict<RedisBytes, RedisData> data;

    private final int id;

    public RedisDB(int id) {
        this.id = id;
        this.data = new Dict<>();
    }

    public Set<RedisBytes> keys(){
        return data.keySet();
    }

    public boolean exist(RedisBytes key){
        return data.containsKey(key);
    }

    public void put(RedisBytes key, RedisData value){
        data.put(key, value);
    }

    public RedisData get(RedisBytes key){
        return data.get(key);
    }

    public int size(){
        return data.size();
    }


}