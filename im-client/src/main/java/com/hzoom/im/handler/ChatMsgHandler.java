package com.hzoom.im.handler;

import com.hzoom.im.proto.ProtoMsg;
import com.hzoom.im.sender.LoginSender;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.stereotype.Component;

@ChannelHandler.Sharable
@Component
public class ChatMsgHandler extends ChannelInboundHandlerAdapter {
    private LoginSender sender;

    public ChatMsgHandler(LoginSender sender) {
        this.sender = sender;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //判断消息实例
        if (null == msg || !(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }

        //判断类型
        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        ProtoMsg.HeadType headType = pkg.getType();
        if (!headType.equals(ProtoMsg.HeadType.MESSAGE_REQUEST)) {
            super.channelRead(ctx, msg);
            return;
        }

        ProtoMsg.MessageRequest req= pkg.getMessageRequest();
        String content=req.getContent();
        String uid=req.getFrom();

        System.out.println(" 收到消息 from uid:" + uid + " -> " + content);
    }
}
