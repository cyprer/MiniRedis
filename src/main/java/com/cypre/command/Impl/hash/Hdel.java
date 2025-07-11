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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Hdel implements Command {
    private RedisCore redisCore;
    private RedisBytes key;
    private List<RedisBytes> fields;

    public Hdel(RedisCore redisCore) {
        this.redisCore = redisCore;
    }
    @Override
    public CommandType getType() {
        return CommandType.HDEL;
    }

    @Override
    public void setContext(Resp[] array) {
        if(array.length < 3){
            throw new IllegalStateException("参数不足");
        }
        key = ((BulkString)array[1]).getContent();
        fields = Stream.of(array).skip(2).map(BulkString.class::cast).map( BulkString::getContent).collect(Collectors.toList());
    }

    @Override
    public Resp handle() {
        RedisData redisData = redisCore.get(key);
        if(redisData == null){
            return new BulkString(new RedisBytes("0".getBytes()));
        }
        if(redisData instanceof RedisHash){
            int delete = ((RedisHash) redisData).del(fields);
            return new RespInteger(delete);
        }
        return new Errors("WRONGTYPE Operation against a key holding the wrong kind of value");
    }

    @Override
    public boolean isWriteCommand() {
        return true;
    }
}