package com.example.core.netty.web.endpoint;

import com.example.core.netty.web.core.WebSocketChannel;
import com.example.core.netty.web.enums.ListenerTypeEnum;
import com.example.core.netty.web.resolver.*;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.springframework.beans.TypeMismatchException;

import java.util.*;

public class EndpointServer {
    private static final AttributeKey<Object> ENDPOINT_KEY = AttributeKey.valueOf("ENDPOINT_KEY");

    public static final AttributeKey<WebSocketChannel> CHANNEL_KEY = AttributeKey.valueOf("CHANNEL_KEY");

    private static final AttributeKey<String> PATH_KEY = AttributeKey.valueOf("PATH_KEY");

    public static final AttributeKey<Map<String, String>> URI_TEMPLATE = AttributeKey.valueOf("URI_TEMPLATE");

    public static final AttributeKey<Map<String, List<String>>> REQUEST_PARAM = AttributeKey.valueOf("REQUEST_PARAM");

    public final Map<String, EndpointMethodMapping> pathMethodMappingMap = new HashMap<>();

    private final EndpointConfig config;

    private Set<WsPathMatcher> pathMatchers = new HashSet<>();

    public EndpointServer(EndpointMethodMapping methodMapping, EndpointConfig config, String path) {
        addPathPojoMethodMapping(path, methodMapping);
        this.config = config;
    }

    public void addPathPojoMethodMapping(String path, EndpointMethodMapping methodMapping) {
        pathMethodMappingMap.put(path, methodMapping);
        for (MethodArgumentResolver resolver : methodMapping.getMethodMap().get(ListenerTypeEnum.OnOpen).getMethodArgumentResolvers()) {
            if (resolver instanceof PathVariableMethodArgumentResolver || resolver instanceof PathVariableMapMethodArgumentResolver) {
                pathMatchers.add(new AntPathMatcherWraaper(path));
                return;
            }
        }
        pathMatchers.add(new DefaultPathMatcher(path));
    }

    public boolean hasBeforeHandshake(Channel channel, String path) {
        EndpointMethodMapping methodMapping = getEndpointMethodMapping(path, channel);
        return methodMapping.getMethodMap().get(ListenerTypeEnum.BeforeHandshake) != null;
    }

    public void doBeforeHandshake(Channel channel, FullHttpRequest req, String path) {
        EndpointMethodMapping endpointMethodMapping = getEndpointMethodMapping(path, channel);
        Object implement = null;
        try {
            implement = endpointMethodMapping.getEndpointInstance();
        } catch (Exception e) {
            return;
        }
        channel.attr(ENDPOINT_KEY).set(implement);
        WebSocketChannel wsChannel = new WebSocketChannel(channel);
        channel.attr(CHANNEL_KEY).set(wsChannel);
        EndpointMethodMapping.MethodMapping beforeHandshake = endpointMethodMapping.getMethodMapping(ListenerTypeEnum.BeforeHandshake);
        if (beforeHandshake != null) {
            try {
                beforeHandshake.getMethod().invoke(implement, beforeHandshake.getMethodArgs(channel, req));
            } catch (TypeMismatchException e) {
                throw e;
            } catch (Throwable t) {
                //todo
            }
        }
    }

    public void doOnOpen(Channel channel, FullHttpRequest req, String path) {
        EndpointMethodMapping endpointMethodMapping = getEndpointMethodMapping(path, channel);
        Object implement = channel.attr(ENDPOINT_KEY).get();
        if (implement == null) {
            try {
                implement = endpointMethodMapping.getEndpointInstance();
                channel.attr(ENDPOINT_KEY).set(implement);
            } catch (Exception e) {
                return;
            }
            WebSocketChannel webSocketChannel = new WebSocketChannel(channel);
            channel.attr(CHANNEL_KEY).set(webSocketChannel);
        }

        EndpointMethodMapping.MethodMapping onOpen = endpointMethodMapping.getMethodMapping(ListenerTypeEnum.OnOpen);
        if (onOpen != null) {
            try {
                onOpen.getMethod().invoke(implement, onOpen.getMethodArgs(channel, req));
            } catch (TypeMismatchException e) {
                throw e;
            } catch (Throwable t) {
                //todo
            }
        }
    }

    public void doOnMessage(Channel channel, WebSocketFrame frame) {
        EndpointMethodMapping endpointMethodMapping = getEndpointMethodMapping(null, channel);
        EndpointMethodMapping.MethodMapping onMessage = endpointMethodMapping.getMethodMapping(ListenerTypeEnum.OnMessage);
        Object implement = channel.attr(ENDPOINT_KEY).get();
        if (onMessage != null) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            try {
                onMessage.getMethod().invoke(implement, onMessage.getMethodArgs(channel, textFrame));
            } catch (Throwable t) {
                //todo
            }
        }
    }

    public void doOnBinary(Channel channel, WebSocketFrame frame) {
        EndpointMethodMapping endpointMethodMapping = getEndpointMethodMapping(null, channel);
        EndpointMethodMapping.MethodMapping onBinary = endpointMethodMapping.getMethodMapping(ListenerTypeEnum.OnBinary);
        Object implement = channel.attr(ENDPOINT_KEY).get();
        if (onBinary != null) {
            BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame) frame;
            try {
                onBinary.getMethod().invoke(implement, onBinary.getMethodArgs(channel, binaryWebSocketFrame));
            } catch (Throwable t) {
                //todo
            }
        }
    }

    public void doOnEvent(Channel channel, Object evt) {
        EndpointMethodMapping endpointMethodMapping = getEndpointMethodMapping(null, channel);
        EndpointMethodMapping.MethodMapping onEvent = endpointMethodMapping.getMethodMapping(ListenerTypeEnum.OnEvent);
        Object implement = channel.attr(ENDPOINT_KEY).get();
        if (onEvent != null) {
            if (!channel.hasAttr(CHANNEL_KEY)) {
                return;
            }
            try {
                onEvent.getMethod().invoke(implement, onEvent.getMethodArgs(channel, evt));
            } catch (Throwable t) {
                //todo
            }
        }
    }

    public void doOnClose(Channel channel) {
        EndpointMethodMapping endpointMethodMapping = getEndpointMethodMapping(null, channel);
        EndpointMethodMapping.MethodMapping onClose = endpointMethodMapping.getMethodMapping(ListenerTypeEnum.OnClose);
        Object implement = channel.attr(ENDPOINT_KEY).get();
        if (onClose != null) {
            if (!channel.hasAttr(CHANNEL_KEY)) {
                return;
            }
            try {
                onClose.getMethod().invoke(implement,onClose.getMethodArgs(channel, null));
            } catch (Throwable t) {
                //todo
            }
        }
    }

    public void doOnError(Channel channel, Throwable throwable) {
        EndpointMethodMapping endpointMethodMapping = getEndpointMethodMapping(null, channel);
        EndpointMethodMapping.MethodMapping onError = endpointMethodMapping.getMethodMapping(ListenerTypeEnum.OnError);
        Object implement = channel.attr(ENDPOINT_KEY).get();
        if (onError != null) {
            if (!channel.hasAttr(CHANNEL_KEY)) {
                return;
            }
            try {
                onError.getMethod().invoke(implement,onError.getMethodArgs(channel, throwable));
            } catch (Throwable t) {
                //todo
            }
        }
    }


    private EndpointMethodMapping getEndpointMethodMapping(String path, Channel channel) {
        EndpointMethodMapping methodMapping;
        if (pathMethodMappingMap.size() == 1) {
            methodMapping = pathMethodMappingMap.values().iterator().next();
        } else {
            Attribute<String> attrPath = channel.attr(PATH_KEY);
            if (path!=null){
                attrPath.set(path);
            }
            methodMapping = pathMethodMappingMap.get(path);
            if (methodMapping == null) {
                throw new RuntimeException("path " + path + " is not in pathMethodMappingMap ");
            }
        }
        return methodMapping;
    }

    public String getHost() {
        return config.getHOST();
    }

    public int getPort() {
        return config.getPORT();
    }

    public Set<WsPathMatcher> getPathMatcherSet() {
        return pathMatchers;
    }
}
