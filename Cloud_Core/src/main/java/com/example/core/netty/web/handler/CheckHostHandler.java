package com.example.core.netty.web.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.util.StringUtils;

import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class CheckHostHandler extends AbstractHandler{
    @Override
    public void doFilter(ChannelHandlerContext ctx, FullHttpRequest req, HandlerChain chain) {
        HttpHeaders headers = req.headers();
        String host = headers.get(HttpHeaderNames.HOST);
        if (StringUtils.isEmpty(host)) {
            if (forbiddenByteBuf != null) {
                resp = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN, forbiddenByteBuf.retainedDuplicate());
            } else {
                resp = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
            }
            sendHttpResponse(ctx, req, resp);
            return;
        }

        if (!StringUtils.isEmpty(chain.endpointServer.getHost()) && !chain.endpointServer.getHost().equals("0.0.0.0") && !chain.endpointServer.getHost().equals(host.split(":")[0])) {
            if (forbiddenByteBuf != null) {
                resp = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN, forbiddenByteBuf.retainedDuplicate());
            } else {
                resp = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
            }
            sendHttpResponse(ctx, req, resp);
            return;
        }
        chain.doFilter(ctx,req,chain);
    }
}
