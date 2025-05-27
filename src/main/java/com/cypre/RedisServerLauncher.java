package com.cypre;

import com.cypre.server.RedisMiniServer;
import com.cypre.server.RedisServer;

import java.io.FileNotFoundException;

public class RedisServerLauncher {
    public static void main(String[] args) throws FileNotFoundException {
        RedisServer redisServer =  new RedisMiniServer("localhost", 6379);
        redisServer.start();
    }
}
