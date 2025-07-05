package com.cypre;

import com.cypre.server.RedisMiniServer;
import com.cypre.server.RedisServer;

import java.io.FileNotFoundException;

public class RedisServerLauncher {
    public static void main(String[] args) throws Exception {
        RedisServer redisServer =  new RedisMiniServer("localhost", 6379);

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try{
                redisServer.stop();
                Thread.sleep(500);
            }catch(Exception e){
                e.printStackTrace();
            }
        }));
        redisServer.start();
    }
}
