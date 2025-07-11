package com.cypre.command.Impl.list;

import com.cypre.command.Command;
import com.cypre.command.CommandType;
import com.cypre.datastructure.RedisBytes;
import com.cypre.datastructure.RedisData;
import com.cypre.datastructure.RedisList;
import com.cypre.protocal.BulkString;
import com.cypre.protocal.Errors;
import com.cypre.protocal.Resp;
import com.cypre.protocal.RespArray;
import com.cypre.server.core.RedisCore;

import java.util.List;

public class Lrange implements Command {
    private RedisBytes key;
    private RedisCore redisCore;
    private int start;
    private int stop;

    public Lrange(RedisCore redisCore)
    {
        this.redisCore = redisCore;
    }

    @Override
    public CommandType getType() {
        return CommandType.LRANGE;
    }

    @Override
    public void setContext(Resp[] array) {
        if(array.length < 4){
            throw new IllegalStateException("参数不足");
        }
        key = ((BulkString)array[1]).getContent();
        start = Integer.parseInt(((BulkString)array[2]).getContent().getString());
        stop = Integer.parseInt(((BulkString)array[3]).getContent().getString());
    }

    @Override
    public Resp handle() {
        RedisData redisData  = redisCore.get(key);
        if(redisData == null) return new BulkString((RedisBytes)null);
        if(redisData instanceof RedisList){
            RedisList redisList = (RedisList) redisData;
            List<RedisBytes> lrange = redisList.lrange(start, stop);

            Resp[] respArray =  new Resp[lrange.size()];
            for(int i = 0; i < lrange.size(); i++){
                respArray[i] = new BulkString(lrange.get(i));
            }
            return new RespArray(respArray);
        }
        return new Errors("命令执行失败");
    }
    @Override
    public boolean isWriteCommand() {
        return false;
    }
}