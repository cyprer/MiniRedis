package com.cypre.command.Impl.hash;

import com.cypre.command.Command;
import com.cypre.command.CommandType;
import com.cypre.datastructure.RedisBytes;
import com.cypre.datastructure.RedisData;
import com.cypre.datastructure.RedisHash;
import com.cypre.protocal.BulkString;
import com.cypre.protocal.Errors;
import com.cypre.protocal.Resp;
import com.cypre.server.core.RedisCore;

public class Hget implements Command {
    private RedisCore redisCore;
    private RedisBytes key;
    private RedisBytes field;

    public Hget(RedisCore redisCore) {
        this.redisCore = redisCore;
    }
    @Override
    public CommandType getType() {
        return CommandType.HGET;
    }

    @Override
    public void setContext(Resp[] array) {
        if(array.length !=3){
            throw new IllegalStateException("参数不足");
        }
        key = ((BulkString)array[1]).getContent();
        field = ((BulkString)array[2]).getContent();
    }

    @Override
    public Resp handle() {
        RedisData redisData = redisCore.get(key);
        if(redisData == null) return new BulkString((RedisBytes)null);
        if(redisData instanceof RedisHash){
            RedisHash redisHash = (RedisHash) redisData;
            RedisBytes value = redisHash.getHash().get(field);
            return new BulkString(value);
        }
        return new Errors("key not hash");
    }
    @Override
    public boolean isWriteCommand() {
        return false;
    }
}