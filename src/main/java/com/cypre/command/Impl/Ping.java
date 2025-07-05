package com.cypre.command.Impl;

import com.cypre.command.Command;
import com.cypre.command.CommandType;
import com.cypre.protocal.Resp;
import com.cypre.protocal.SimpleString;

public class Ping implements Command {

    @Override
    public CommandType getType() {
        return CommandType.PING;
    }

    @Override
    public void setContext(Resp[] array) {
        //不需要内容
    }

    @Override
    public Resp handle() {
        return new SimpleString("PONGPONGPONGSAHUR");
    }

    @Override
    public boolean isWriteCommand() {
        return false;
    }
}
