package com.cypre.command.Impl;

import com.cypre.command.Command;
import com.cypre.command.CommandType;
import com.cypre.protocal.BulkString;
import com.cypre.protocal.Errors;
import com.cypre.protocal.Resp;
import com.cypre.protocal.RespInteger;
import com.cypre.server.core.RedisCore;

public class Select implements Command {
    private RedisCore redisCore;
    private int dbIndex;

    public Select(RedisCore redisCore) {
        this.redisCore = redisCore;
    }
    @Override
    public CommandType getType() {
        return CommandType.SELECT;
    }

    @Override
    public void setContext(Resp[] array) {
        if(array.length == 2){
            dbIndex = Integer.parseInt(((BulkString)array[1]).getContent().getString());
        }
        else{
            throw new IllegalStateException("参数错误");
        }
    }

    @Override
    public Resp handle() {
        try{
            redisCore.selectDB(dbIndex);
            return new RespInteger(1);
        }catch(Exception e){
            return new Errors("参数错误");
        }
    }

    @Override
    public boolean isWriteCommand() {
        return true;
    }
}
