package com.cypre;

import com.cypre.server.RedisMiniServer;
import com.cypre.server.RedisServer;

public class RedisServerLauncher {
    public static void main(String[] args) {
        RedisServer redisServer =  new RedisMiniServer("localhost", 6379);
        redisServer.start();
    }
}
