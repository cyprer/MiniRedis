package com.cypre.command.Impl.string;

import com.cypre.command.Command;
import com.cypre.command.CommandType;
import com.cypre.datastructure.RedisBytes;
import com.cypre.datastructure.RedisData;
import com.cypre.datastructure.RedisString;
import com.cypre.protocal.BulkString;
import com.cypre.protocal.Errors;
import com.cypre.protocal.Resp;
import com.cypre.protocal.SimpleString;
import com.cypre.server.core.RedisCore;
import lombok.extern.slf4j.Slf4j;

import static com.cypre.protocal.BulkString.NULL_BYTES;

@Slf4j
public class Get implements Command {
    private RedisCore redisCore;
    private RedisBytes key;

    public Get(RedisCore redisCore) {
        this.redisCore = redisCore;
    }
    @Override
    public CommandType getType() {
        return CommandType.GET;
    }

    @Override
    public void setContext(Resp[] array) {
        key = ((BulkString)array[1]).getContent();
    }

    @Override
    public Resp handle() {
        try{
            RedisData data = redisCore.get(key);
            if(data == null){
                return new BulkString(NULL_BYTES);
            }
            if(data instanceof RedisString){
                RedisString redisString = (RedisString) data;
                return new BulkString(redisString.getValue());
            }
        }catch(Exception e){
            log.error("handle error", e);
            return new Errors("ERR internal server error");
        }
        return new Errors("ERR unknown error");
    }
    @Override
    public boolean isWriteCommand() {
        return false;
    }
}
