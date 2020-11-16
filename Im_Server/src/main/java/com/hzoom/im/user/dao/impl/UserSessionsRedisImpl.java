package com.hzoom.im.user.dao.impl;

import com.hzoom.core.redis.RedisUtils;
import com.hzoom.im.distributed.Peer;
import com.hzoom.im.user.UserSessions;
import com.hzoom.im.user.dao.UserSessionsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserSessionsRedisImpl implements UserSessionsDAO {
    @Autowired
    private Peer peer;
    public static final String REDIS_PREFIX = "UserSessions:uid:";
    private static final int EXPIRE_TIME = 60 * 4;//4小时
    @Autowired
    RedisUtils redisUtils;

    @Override
    public void save(UserSessions userSessions) {
        String redisKey = getRedisKey(userSessions.getUserId());
        redisUtils.setexObject(redisKey,EXPIRE_TIME,redisKey);
    }

    @Override
    public UserSessions get(String userId) {
        String redisKey = getRedisKey(userId);
        return redisUtils.getObject(redisKey,UserSessions.class);
    }

    @Override
    public void cacheUser(String uid, String sessionId) {
        UserSessions userSessions = get(sessionId);
        if (null==userSessions){
            userSessions =new UserSessions(uid);
        }
        userSessions.addNodeBySessionId(sessionId,peer.getLocalImNode());
        save(userSessions);
    }

    @Override
    public void removeUserSession(String userId, String sessionId) {
        UserSessions userSessions = get(userId);
        if (null==userSessions){
            userSessions = new UserSessions(userId);
        }
        userSessions.removeNodeBySessionId(sessionId);
        save(userSessions);
    }

    public static String getRedisKey(String userId){
        return REDIS_PREFIX + userId;
    }
}
