package com.hzoom.game.handler.common;

import com.hzoom.game.message.GameMessageManager;
import com.hzoom.game.message.common.IMessage;
import com.hzoom.game.message.common.MessagePackage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@ChannelHandler.Sharable
@Component
public class ResponseHandler extends ChannelInboundHandlerAdapter {
    @Autowired
    private GameMessageManager gameMessageManager;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessagePackage messagePackage = (MessagePackage) msg;
        int messageId = messagePackage.getHeader().getMessageId();
        IMessage response = gameMessageManager.getResponseInstanceByMessageId(messageId);
        response.setHeader(messagePackage.getHeader());
        response.read(messagePackage.body());
        ctx.fireChannelRead(response);
    }
}
