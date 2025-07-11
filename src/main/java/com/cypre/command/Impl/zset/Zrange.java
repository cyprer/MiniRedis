package com.cypre.command.Impl.zset;

import com.cypre.command.Command;
import com.cypre.command.CommandType;
import com.cypre.datastructure.RedisBytes;
import com.cypre.datastructure.RedisData;
import com.cypre.internal.SkipList;
import com.cypre.protocal.BulkString;
import com.cypre.protocal.Errors;
import com.cypre.protocal.Resp;
import com.cypre.protocal.RespArray;
import com.cypre.server.core.RedisCore;
import com.cypre.datastructure.RedisZest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Zrange implements Command {
    private RedisCore redisCore;
    private RedisBytes key;
    private int start;
    private int stop;
    private boolean withScores = false;

    public Zrange(RedisCore redisCore) {
        this.redisCore = redisCore;
    }

    @Override
    public CommandType getType() {
        return CommandType.ZRANGE;
    }

    @Override
    public void setContext(Resp[] array) {
        if(array.length < 4){
            throw new IllegalArgumentException("ZRANGE command requires at least 4 arguments.");
        }
        key = ((BulkString)array[1]).getContent();
        RedisBytes startBytes = ((BulkString)array[2]).getContent();
        start = Integer.parseInt(startBytes.getString());
        RedisBytes stopBytes = ((BulkString)array[3]).getContent();
        stop = Integer.parseInt(stopBytes.getString());
        if(array.length == 5){
            RedisBytes option = ((BulkString)array[4]).getContent();
            if(option.getString().equalsIgnoreCase("WITHSCORES")){
                withScores = true;
            }
        }
    }

    @Override
    public Resp handle() {
        try{
            // 检查key是否存在
            RedisData data = redisCore.get(key);
            if(data == null) return new RespArray(new Resp[0]);

            // 检查类型
            if(!(data instanceof RedisZest)){
                return new Errors("ERR wrong type for 'zrange' command");
            }

            RedisZest redisZset = (RedisZest)data;
            int size = redisZset.size();
            if(size == 0) return new RespArray(new Resp[0]);

            // 处理索引
            int startIndex = start;
            int stopIndex = stop;

            if(startIndex < 0) startIndex = size + startIndex;
            if(stopIndex < 0) stopIndex = size + stopIndex;

            startIndex = Math.max(0, startIndex);
            stopIndex = Math.min(size-1, stopIndex);

            if(startIndex > stopIndex){
                return new RespArray(new Resp[0]);
            }

            // 获取范围数据
            List<SkipList.SkipListNode<String>> range;
            try {
                range = redisZset.getRange(startIndex, stopIndex);
                if(range == null) {
                    return new RespArray(new Resp[0]);
                }

                // 不需要再反转列表，因为已经在SkipList中正确排序
                // Collections.reverse(range);
            } catch (Exception e) {
                return new Errors("ERR Failed to get range: " + e.getMessage());
            }

            // 构建返回数据
            List<Resp> respList = new ArrayList<>();
            try {
                for(SkipList.SkipListNode<String> node : range){
                    if(node == null || node.member == null) {
                        continue; // 跳过空节点
                    }

                    respList.add(new BulkString(new RedisBytes(node.member.getBytes())));
                    if(withScores){
                        respList.add(new BulkString(new RedisBytes(String.valueOf(node.score).getBytes())));
                    }
                }
            } catch (Exception e) {
                return new Errors("ERR Failed to process range results: " + e.getMessage());
            }

            return new RespArray(respList.toArray(new Resp[0]));
        } catch(Exception e) {
            // 打印异常堆栈供调试
            e.printStackTrace();
            if(e.getMessage() == null) {
                return new Errors("ERR Internal error in 'zrange' command");
            }
            return new Errors("ERR " + e.getMessage());
        }
    }
    @Override
    public boolean isWriteCommand() {
        return false;
    }
}