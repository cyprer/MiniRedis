package com.cypre.command.Impl.set;

import com.cypre.command.Command;
import com.cypre.command.CommandType;
import com.cypre.datastructure.RedisBytes;
import com.cypre.datastructure.RedisData;
import com.cypre.datastructure.RedisSet;
import com.cypre.protocal.BulkString;
import com.cypre.protocal.Errors;
import com.cypre.protocal.Resp;
import com.cypre.protocal.RespInteger;
import com.cypre.server.core.RedisCore;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class Srem implements Command {
    private RedisCore redisCore;
    private RedisBytes key;
    private List<RedisBytes> members;
    public Srem(RedisCore redisCore) {
        this.redisCore = redisCore;
    }
    @Override
    public CommandType getType() {
        return CommandType.SREM;
    }

    @Override
    public void setContext(Resp[] array) {
        key = ((BulkString)array[1]).getContent();
        members = Stream.of(array).skip(2).map(BulkString.class::cast).map(BulkString::getContent).collect(Collectors.toList());
    }

    @Override
    public Resp handle() {
        RedisData redisData = redisCore.get(key);
        if(redisData == null) return new Errors("ERR no such key");
        int count=0;
        if(redisData instanceof RedisSet){
            RedisSet redisSet = (RedisSet) redisData;
            for(RedisBytes member : members){
                count+=redisSet.remove(member);
                log.info("remove {} from set {}", member.getString(), key.getString());
            }
        }
        return new RespInteger(count);
    }
    @Override
    public boolean isWriteCommand() {
        return true;
    }
}
