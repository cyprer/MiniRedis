package com.cypre.command;

import com.cypre.command.Impl.string.Get;
import com.cypre.command.Impl.string.Set;
import com.cypre.command.Impl.Ping;
import com.cypre.command.Impl.set.Sadd;
import com.cypre.command.Impl.set.Spop;
import com.cypre.command.Impl.set.Srem;
import com.cypre.command.Impl.list.Lpush;
import com.cypre.command.Impl.list.Lpop;
import com.cypre.command.Impl.list.Lrange;
import com.cypre.command.Impl.hash.Hset;
import com.cypre.command.Impl.hash.Hget;
import com.cypre.command.Impl.hash.Hdel;
import com.cypre.command.Impl.zset.Zadd;
import com.cypre.command.Impl.zset.Zrange;
import com.cypre.command.Impl.Select;
import lombok.Getter;
import com.cypre.server.core.RedisCore;
import java.util.function.Function;

@Getter
public enum CommandType {
    PING(core ->new Ping()),
    SET(Set::new),
    GET(Get::new),
    SADD(Sadd::new),
    SPOP(Spop::new),
    SREM(Srem::new),
    LPUSH(Lpush::new),
    LPOP(Lpop::new),
    LRANGE(Lrange::new),
    HSET(Hset::new),
    HGET(Hget::new),
    HDEL(Hdel::new),
    ZADD(Zadd::new),
    ZRANGE(Zrange::new),
    SELECT(Select::new);
    private final Function<RedisCore, Command> supplier;

    CommandType(Function<RedisCore, Command> supplier) {
        this.supplier = supplier;
    }
}
