package com.cypre.datastructure;

import com.cypre.internal.Dict;

import java.util.List;

public class RedisHash implements RedisData{
    private volatile long timeout = -1;
    private Dict<RedisBytes,RedisBytes> hash;

    public RedisHash() {
        this.hash = new Dict<>();
    }

    @Override
    public long timeout() {
        return timeout;
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public int put(RedisBytes field, RedisBytes value){
        return hash.put(field, value)==null?1:0;
    }

    public Dict<RedisBytes,RedisBytes> getHash() {
        return hash;
    }

    public int del(List<RedisBytes> fields){
        return (int)fields.stream().filter(field -> hash.remove(field) != null).count();
    }
}
