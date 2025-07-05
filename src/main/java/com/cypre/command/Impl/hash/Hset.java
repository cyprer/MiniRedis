package com.cypre.command.Impl.hash;

import com.cypre.command.Command;
import com.cypre.command.CommandType;
import com.cypre.datastructure.RedisBytes;
import com.cypre.datastructure.RedisData;
import com.cypre.datastructure.RedisHash;
import com.cypre.protocal.BulkString;
import com.cypre.protocal.Errors;
import com.cypre.protocal.Resp;
import com.cypre.protocal.RespInteger;
import com.cypre.server.core.RedisCore;

public class Hset implements Command {
    private RedisCore redisCore;
    private RedisBytes key;
    private RedisBytes field;
    private RedisBytes value;

    public Hset(RedisCore redisCore) {
        this.redisCore = redisCore;
    }

    @Override
    public CommandType getType() {
        return CommandType.HSET;
    }

    @Override
    public void setContext(Resp[] array) {
        if(array.length == 4){
            key = ((BulkString)array[1]).getContent();
            field = ((BulkString)array[2]).getContent();
            value = ((BulkString)array[3]).getContent();
        }
        else{
            throw new IllegalStateException("参数错误");
        }

    }

    @Override
    public Resp handle() {
        RedisData redisData = redisCore.get(key);
        if(redisData == null){
            RedisHash hash = new RedisHash();
            int put = hash.put(field, value);
            redisCore.put(key, hash);
            return new RespInteger(put);
        }
        else if(redisData instanceof RedisHash){
            RedisHash hash = (RedisHash) redisData;
            int put = hash.put(field, value);
            redisCore.put(key, hash);
            return new RespInteger(put);
        }
        return new Errors("参数错误");
    }
    @Override
    public boolean isWriteCommand() {
        return true;
    }
}