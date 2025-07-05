package com.cypre.command.Impl.list;

import com.cypre.command.Command;
import com.cypre.command.CommandType;
import com.cypre.datastructure.RedisBytes;
import com.cypre.datastructure.RedisData;
import com.cypre.datastructure.RedisList;
import com.cypre.protocal.BulkString;
import com.cypre.protocal.Errors;
import com.cypre.protocal.Resp;
import com.cypre.server.core.RedisCore;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Lpush implements Command {
    private RedisCore redisCore;
    private List<RedisBytes> element;
    private RedisBytes key;

    public Lpush(RedisCore redisCore) {
        this.redisCore = redisCore;
    }

    @Override
    public CommandType getType() {
        return CommandType.LPUSH;
    }

    @Override
    public void setContext(Resp[] array) {
        if(array.length < 3){
            throw new IllegalStateException("参数不足");
        }
        key = ((BulkString)array[1]).getContent();
        element = Stream.of(array).skip(2).map(e -> ((BulkString)e).getContent()).collect(Collectors.toList());
    }

    @Override
    public Resp handle() {
        RedisData redistData = redisCore.get(key);
        RedisList redisList;

        if(redistData == null) redisList = new RedisList();
        else if (redistData instanceof RedisList) redisList = (RedisList)redistData;
        else return new Errors("key not exist");

        RedisBytes[] elementArray = element.toArray(new RedisBytes[0]);
        redisList.lpush(elementArray);
        redisCore.put(key, redisList);
        int size = redisList.size();
        return new BulkString(new RedisBytes(String.valueOf(size).getBytes()));
    }
    @Override
    public boolean isWriteCommand() {
        return true;
    }

}