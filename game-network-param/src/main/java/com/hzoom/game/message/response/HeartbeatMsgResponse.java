package com.hzoom.game.message.response;

import com.hzoom.game.message.message.AbstractJsonMessage;
import com.hzoom.game.message.message.IMessage;
import com.hzoom.game.message.message.MessageMetadata;

@MessageMetadata(messageId = 2, serviceId = 1, messageType = IMessage.MessageType.RESPONSE)
public class HeartbeatMsgResponse extends AbstractJsonMessage<HeartbeatMsgResponse.ResponseBody> {
    @Override
    protected Class<ResponseBody> getBodyObjClass() {
        return ResponseBody.class;
    }

    public static class ResponseBody {
        private long serverTime;

        public long getServerTime() {
            return serverTime;
        }

        public void setServerTime(long serverTime) {
            this.serverTime = serverTime;
        }

    }
}
