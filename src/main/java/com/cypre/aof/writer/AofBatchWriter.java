package com.cypre.aof.writer;

import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class AofBatchWriter {
    private final Writer writer;

    //异步刷盘
    private final ScheduledExecutorService flushScheduler;      //定时刷盘线程池
    private final AtomicBoolean forceFlush = new AtomicBoolean(false);
    private final int flushInterval;

    //队列
    private static final int DEFAULT_QUEUQ_SIZE = 1000;
    private final BlockingQueue<ByteBuf> writeQueue;        //写入队列，存储待写入的 ByteBuf
    private final Thread writeThread;
    private final AtomicBoolean running = new AtomicBoolean(true);


    //背压阈值
    private static final int DEFAULT_BACKPRESSURE_THRESHOLD = 6*1024*1024;
    private final AtomicInteger pendingBytes = new AtomicInteger(0);        //记录队列中待处理的字节数，用于背压控制

    //批处理参数
    public static final int MIN_BATCH_SIZE = 16;
    public static final int MAX_BATCH_SIZE = 50;

    public static final int  MIN_BATCH_TIMEOUT_MS = 2;
    public static final int  MAX_BATCH_TIMEOUT_MS = 10;

    //大命令的参数
    private static final int LARGE_COMMAND_THRESHOLD = 512*1024;

    public AofBatchWriter(Writer fileWriter, int  flushInterval){
        this.writer = fileWriter;
        this.flushInterval = flushInterval;



        this.writeQueue = new LinkedBlockingDeque<>(DEFAULT_QUEUQ_SIZE);
        this.writeThread = new Thread(this::processWriteQueue);
        this.writeThread.setName("AOF-Writer-Thread");
        this.writeThread.setDaemon(true);
        this.writeThread.start();

        this.flushScheduler = new ScheduledThreadPoolExecutor(1, r->{
            Thread thread = new Thread(r);
            thread.setName("AOF-Flush-Thread");
            thread.setDaemon(true);
            return thread;
        });

        if(flushInterval > 0){
            this.flushScheduler.scheduleAtFixedRate(()->{
                try{
                    if(forceFlush.compareAndSet(true, false)){
                        writer.flush();
                    }
                }catch(Exception e){
                    log.error("Failed to flush AOF file", e);
                }
            },flushInterval, flushInterval, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
    }

    public void processWriteQueue(){
        ByteBuf[] batch = new ByteBuf[MAX_BATCH_SIZE];
        int batchSize = 0;

        while(running.get() || !writeQueue.isEmpty()){
            try{
                int currentBatchSize = calculateBatchSize(batchSize);
                long timeout = calculateTimeout(currentBatchSize);

                long deadline = System.currentTimeMillis() + timeout;

                while(batchSize < currentBatchSize && System.currentTimeMillis() < deadline){
                    ByteBuf item = writeQueue.poll(Math.max(1,  deadline - System.currentTimeMillis()),
                            TimeUnit.MILLISECONDS);

                    if(item != null){
                        batch[batchSize++] = item;
                    }else if(batchSize == 0){
                        Thread.yield();
                    }else{
                        break;
                    }
                }

                if(batchSize > 0){
                    writeBatch(batch, batchSize);
                    for(int i = 0; i < batchSize; i++){
                        pendingBytes.addAndGet(-batch[i].readableBytes());
                        batch[i].release();
                        batch[i] = null;
                    }
                    batchSize = 0;
                }
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            catch
            (Exception e){
                log.error("Failed to process write queue", e);
                for(int i = 0; i < batchSize; i++){
                    batch[i].release();
                    batch[i] = null;
                }
                batchSize = 0;
            }
        }
    }

    private void writeBatch(ByteBuf[] batch, int batchSize) {
        if(batchSize <= 0) return;
        try{
            int totalBytes = 0;
            for(int i = 0; i < batchSize; i++){
                totalBytes+=batch[i].readableBytes();
            }

            ByteBuffer buffer = ByteBuffer.allocate(totalBytes);

            for(int i = 0; i < batchSize; i++){
                buffer.put(batch[i].nioBuffer());
            }
            buffer.flip();
            writer.write(buffer);
        }catch(Exception e){
            log.error("Failed to write batch to AOF file", e);
        }
    }



    public void write(ByteBuf byteBuf) throws IOException {
        int byteSize = byteBuf.readableBytes();
        if(byteSize > LARGE_COMMAND_THRESHOLD){
            try{
                ByteBuffer byteBuffer = byteBuf.nioBuffer();
                writer.write(byteBuffer);
            }finally {
                byteBuf.release();
            }
        }

        pendingBytes.addAndGet(byteSize);

        if(pendingBytes.get() > DEFAULT_BACKPRESSURE_THRESHOLD ||
                writeQueue.size() >DEFAULT_QUEUQ_SIZE *0.75){
            applyBackpressure();
        }

        try{
            boolean success = writeQueue.offer(byteBuf, 3, TimeUnit.SECONDS);
            if(!success){
                ByteBuffer byteBuffer = byteBuf.nioBuffer();
                writer.write(byteBuffer);
                byteBuf.release();
            }
            forceFlush.set(true);
        }catch(Exception e){
            byteBuf.release();
            Thread.currentThread().interrupt();
        }

    }

    public void flush() throws Exception{
        while(!writeQueue.isEmpty()){
            try{
                Thread.sleep(1);
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
                break;
            }
            writer.flush();
        }
    }

    public void close() throws Exception{
        if(flushScheduler != null){
            flushScheduler.shutdown();
            try{
                flushScheduler.shutdownNow();
                flushScheduler.awaitTermination(3, TimeUnit.SECONDS);
            }catch(InterruptedException e){
                flushScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        flush();
        running.set(false);
        writeThread.interrupt();
    }

    private void applyBackpressure() {
        if(pendingBytes.get() > DEFAULT_BACKPRESSURE_THRESHOLD){
            try{
                long waitTime = Math.min(20,(pendingBytes.get()-DEFAULT_BACKPRESSURE_THRESHOLD)/1024);
                Thread.sleep(waitTime);
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
    }

    private long calculateTimeout(int currentBatchSize) {
        int queueSize = writeQueue.size();
        if(queueSize > DEFAULT_QUEUQ_SIZE / 2){
            return MIN_BATCH_TIMEOUT_MS;
        }
        return MAX_BATCH_TIMEOUT_MS;
    }

    private int calculateBatchSize(int batchSize) {
        int queueSize = writeQueue.size();
        int result = Math.min(MAX_BATCH_SIZE,Math.min(MIN_BATCH_SIZE, MIN_BATCH_SIZE + queueSize / 20));
        return result;
    }


}
