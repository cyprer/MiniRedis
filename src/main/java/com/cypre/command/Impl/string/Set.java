package com.cypre.command.Impl.string;

import com.cypre.command.Command;
import com.cypre.command.CommandType;
import com.cypre.datastructure.RedisBytes;
import com.cypre.datastructure.RedisData;
import com.cypre.datastructure.RedisString;
import com.cypre.internal.Sds;
import com.cypre.protocal.BulkString;
import com.cypre.protocal.Resp;
import com.cypre.protocal.SimpleString;
import com.cypre.server.core.RedisCore;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Set implements Command {
    private RedisBytes key;
    private RedisBytes value;
    private RedisCore redisCore;

    public Set(RedisCore redisCore) {
        this.redisCore = redisCore;
    }
    @Override
    public CommandType getType() {
        return CommandType.SET;
    }

    @Override
    public void setContext(Resp[] array) {
        if(array.length < 3){
            throw new IllegalStateException("参数不足");
        }
        key = ((BulkString)array[1]).getContent();
        value = ((BulkString)array[2]).getContent();
    }

    @Override
    public Resp handle() {
        if(redisCore.get(key) != null){
            RedisData data = redisCore.get(key);
            if(data instanceof RedisString){
                RedisString redisString = (RedisString) data;
                redisString.setSds(new Sds(value.getBytes()));
                return new SimpleString("OK");
            }
        }
        redisCore.put(key, new RedisString(new Sds(value.getBytes())));
        log.info("set key:{} value:{}", key, value);

        return new SimpleString("OK");
    }
}
