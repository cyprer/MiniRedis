package com.cypre.server.handler;

import com.cypre.aof.AofManager;
import com.cypre.datastructure.RedisBytes;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import com.cypre.command.Command;
import com.cypre.command.CommandType;
import com.cypre.protocal.BulkString;
import com.cypre.protocal.Errors;
import com.cypre.protocal.Resp;
import com.cypre.protocal.RespArray;
import com.cypre.server.core.RedisCore;

@Slf4j
@Getter
@Sharable
public class RespCommandHandler extends SimpleChannelInboundHandler<Resp> {

    private AofManager aofManager;

    private final RedisCore redisCore;
    public RespCommandHandler(RedisCore redisCore, AofManager aofManager) {
        this.redisCore = redisCore;
        this.aofManager = aofManager;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Resp msg) throws Exception {
        if(msg instanceof RespArray){
            RespArray respArray = (RespArray) msg;
            Resp response = processCommand(respArray);

            if(response!=null){
                ctx.channel().writeAndFlush(response);
            }
        }else{
            ctx.channel().writeAndFlush(new Errors("不支持的命令"));
        }
    }

    private Resp processCommand(RespArray respArray) {
        if(respArray.getContent().length==0){
            return new Errors("命令不能为空");
        }

        try{
            Resp[] array = respArray.getContent();
            RedisBytes cmd = ((BulkString)array[0]).getContent();
            String commandName = cmd.getString().toUpperCase();
            CommandType commandType;

            try{
                commandType = CommandType.valueOf(commandName);
            }catch (IllegalArgumentException e){
                return new Errors("命令不存在");
            }

            Command command = commandType.getSupplier().apply(redisCore);
            command.setContext(array);
            Resp result = command.handle();
            if(aofManager !=null && command.isWriteCommand()){
                aofManager.append(respArray);
            }
            return result;
        }catch (Exception e){
            log.error("命令执行失败",e);
            return new Errors("命令执行失败");
        }
    }
}
