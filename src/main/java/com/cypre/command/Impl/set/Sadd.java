package com.cypre.command.Impl.set;

import com.cypre.command.Command;
import com.cypre.command.CommandType;
import com.cypre.datastructure.RedisBytes;
import com.cypre.datastructure.RedisData;
import com.cypre.datastructure.RedisSet;
import com.cypre.protocal.BulkString;
import com.cypre.protocal.Resp;
import com.cypre.protocal.RespInteger;
import com.cypre.server.core.RedisCore;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Sadd implements Command {
    private RedisCore redisCore;
    private RedisBytes key;
    private List<RedisBytes> members;

    public Sadd(RedisCore redisCore) {
        this.redisCore = redisCore;

    }
    @Override
    public CommandType getType() {
        return CommandType.SADD;
    }

    @Override
    public void setContext(Resp[] array) {
        key = ((BulkString)array[1]).getContent();
        members = Stream.of(array).skip(2).map(BulkString.class::cast).map(BulkString::getContent).collect(Collectors.toList());
    }

    @Override
    public Resp handle() {
        RedisSet redisSet = null;
        RedisData redisData = redisCore.get(key);
        if(redisData == null) redisSet = new RedisSet();
        if(redisData instanceof RedisSet){
            redisSet = (RedisSet) redisData;
        }
        int add = redisSet.add(members);
        redisCore.put(key, redisSet);
        return new RespInteger(add);
    }
}