package com.cypre.command;

import com.cypre.command.Impl.string.Get;
import com.cypre.command.Impl.string.Set;
import com.cypre.command.Impl.Ping;
import lombok.Getter;
import com.cypre.server.core.RedisCore;
import java.util.function.Function;

@Getter
public enum CommandType {
    PING(core ->new Ping()), SET(Set::new), GET(Get::new);


    private final Function<RedisCore, Command> supplier;

    CommandType(Function<RedisCore, Command> supplier) {
        this.supplier = supplier;
    }
}
