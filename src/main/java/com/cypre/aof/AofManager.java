package com.cypre.aof;

import com.cypre.aof.loader.AofLoader;
import com.cypre.aof.writer.AofBatchWriter;
import com.cypre.aof.writer.AofWriter;
import com.cypre.aof.writer.Writer;
import com.cypre.protocal.RespArray;
import com.cypre.server.core.RedisCore;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AofManager     {
    private Writer aofWriter;
    private AofBatchWriter batchWriter;
    private AofLoader aofLoader;
    private String fileName;
    private boolean fileExists;
    private RedisCore redisCore;

    private static final int DEFAULT_FLUSH_INTERVAL_MS = 1000;
    private static final boolean DEFAULT_PREALLOCATE = true;

    public AofManager(String fileName,RedisCore redisCore) throws Exception {
        this(fileName,new File(fileName).exists(),DEFAULT_PREALLOCATE,DEFAULT_FLUSH_INTERVAL_MS,redisCore);
    }

    public AofManager(String fileName,boolean fileExists,boolean preallocated, int flushInterval, RedisCore redisCore) throws Exception {
        this.fileName = fileName;
        this.fileExists = fileExists;
        this.aofWriter = new AofWriter(new File(fileName), preallocated,flushInterval, null);
        this.batchWriter = new AofBatchWriter(aofWriter,flushInterval);
        this.redisCore = redisCore;
        this.aofLoader = new AofLoader(fileName,redisCore);
    }

    public void append(RespArray respArray) throws IOException {
        ByteBuf byteBuf = Unpooled.buffer();
        respArray.encode(respArray, byteBuf);
        batchWriter.write(byteBuf);
    }

    public void load(){
        aofLoader.load();
    }

    public void close() throws Exception {

        if(batchWriter != null){
            batchWriter.close();
        }
        if(aofWriter != null){
            aofWriter.close();
        }
        if(aofLoader != null){
            aofLoader.close();
        }
    }

    public void flush() throws Exception {
        if(batchWriter != null){
            batchWriter.flush();
        }
    }

}
