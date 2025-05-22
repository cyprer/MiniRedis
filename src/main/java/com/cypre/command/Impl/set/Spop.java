package com.cypre.command.Impl.set;

import com.cypre.command.Command;
import com.cypre.command.CommandType;
import com.cypre.datastructure.RedisBytes;
import com.cypre.datastructure.RedisData;
import com.cypre.datastructure.RedisSet;
import com.cypre.protocal.BulkString;
import com.cypre.protocal.Errors;
import com.cypre.protocal.Resp;
import com.cypre.protocal.RespArray;
import com.cypre.server.core.RedisCore;

import java.util.List;

public class Spop implements Command {
    private RedisCore redisCore;
    private RedisBytes key;
    private int count = 1;

    public Spop(RedisCore redisCore) {
        this.redisCore = redisCore;
    }
    @Override
    public CommandType getType() {
        return CommandType.SPOP;
    }

    @Override
    public void setContext(Resp[] array) {
        key = ((BulkString)array[1]).getContent();
        if(array.length > 2){
            try{
                count = Integer.parseInt(((BulkString)array[2]).getContent().getString());
            }catch (Exception e){
                count=1;
            }
        }
    }

    @Override
    public Resp handle() {
        RedisData redisData = redisCore.get(key);
        if(redisData == null){
            if(count == 1){
                return new BulkString((RedisBytes)null);
            }
            return new RespArray(new Resp[0]);
        }

        if(redisData instanceof RedisSet){
            RedisSet redisSet = (RedisSet) redisData;

            if(redisSet.size() == 0){
                if(count == 1) return new BulkString((RedisBytes)null);
                return new RespArray(new Resp[0]);
            }

            List<RedisBytes> poppedElements = redisSet.pop(count);
            if(poppedElements.isEmpty()){
                return new RespArray(new Resp[0]);
            }
            redisCore.put(key,redisSet);

            if(count == 1 && poppedElements.size() == 1) {
                return new BulkString(poppedElements.get(0));
            }

            return new RespArray(poppedElements.stream().map(BulkString::new).toArray(Resp[]::new));
        }
        return new Errors("命令执行失败");
    }
}
