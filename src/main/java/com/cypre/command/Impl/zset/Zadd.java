package com.cypre.command.Impl.zset;

import com.cypre.command.Command;
import com.cypre.command.CommandType;
import com.cypre.datastructure.RedisBytes;
import com.cypre.protocal.BulkString;
import com.cypre.protocal.Errors;
import com.cypre.protocal.Resp;
import com.cypre.protocal.RespInteger;
import com.cypre.server.core.RedisCore;
import com.cypre.datastructure.RedisZest;
import java.util.ArrayList;
import java.util.List;

public class Zadd implements Command {
    private RedisCore redisCore;
    private RedisBytes key;
    private List<Double> scores;
    private List<Object> members;

    public Zadd(RedisCore redisCore) {
        this.redisCore = redisCore;
    }
    @Override
    public CommandType getType() {
        return CommandType.ZADD;
    }

    @Override
    public void setContext(Resp[] array) {
        if(array.length < 4 || (array.length - 2)%2 != 0){
            throw new IllegalStateException("参数不足");
        }

        key = ((BulkString)array[1]).getContent();
        scores = new ArrayList<>();
        members = new ArrayList<>();

        for(int i = 2; i <array.length;i+=2){
            RedisBytes bytes = ((BulkString)array[i]).getContent();
            scores.add(Double.parseDouble(bytes.getString()));
            members.add(((BulkString)array[i+1]).getContent());
        }

    }

    @Override
    public Resp handle() {
        try{
            RedisZest zset = (RedisZest) redisCore.get(key);
            if(zset == null){
                zset = new RedisZest();
                redisCore.put(key,zset);
            }
            int count=0;
            for(int i=0;i<scores.size();i++){
                if(zset.add(scores.get(i),members.get(i))){
                    count++;
                }
            }
            return new RespInteger(count);
        }catch(Exception e){
            return new Errors("ERR " + e.getMessage());
        }
    }
}
