package com.cypre.command;

import com.cypre.protocal.Resp;

public interface Command {
    CommandType getType();
    void setContext(Resp[] array);
    Resp handle();
}
