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

public class Lpop implements Command {
    private RedisCore redisCore;
    private RedisBytes key;

    public Lpop(RedisCore redisCore) {
        this.redisCore = redisCore;
    }

    @Override
    public CommandType getType() {
        return CommandType.LPOP;
    }

    @Override
    public void setContext(Resp[] array) {
        if(array.length <2){
            throw new RuntimeException("参数错误");
        }
        key = ((BulkString)array[1]).getContent();
    }

    @Override
    public Resp handle() {
        RedisData redisData = redisCore.get(key);

        if(redisData == null) return new BulkString((RedisBytes)null);
        if(redisData instanceof RedisList){
            RedisList list = (RedisList) redisData;
            RedisBytes lpop = list.lpop();

            redisCore.put(key, list);
            return new BulkString(lpop);
        }
        return new Errors("命令执行失败");
    }
    @Override
    public boolean isWriteCommand() {
        return true;
    }
}
