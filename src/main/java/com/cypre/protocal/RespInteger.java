package com.cypre.protocal;

import com.cypre.protocal.Resp;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

@Getter
public class RespInteger extends Resp {
    private int content;

    public RespInteger(int content) {
        this.content = content;
    }

    @Override
    public void encode(Resp resp, ByteBuf byteBuf) {
        byteBuf.writeByte(':');
        byteBuf.writeBytes(String.valueOf(((RespInteger)resp).getContent()).getBytes());
        byteBuf.writeBytes(CRLF);
    }
}
