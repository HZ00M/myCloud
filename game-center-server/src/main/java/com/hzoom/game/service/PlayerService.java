package com.hzoom.game.service;

import com.hzoom.core.redis.RedisService;
import com.hzoom.game.dao.PlayerDao;
import com.hzoom.game.entity.Player;
import com.hzoom.game.error.GameCenterError;
import com.hzoom.game.exception.ErrorException;
import com.hzoom.game.redis.RedisKeyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PlayerService {
    @Autowired
    private PlayerDao playerDao;
    @Autowired
    private RedisService redisService;

    private String getNickNameKey(String zoneId) {
        return RedisKeyEnum.PLAYER_NICKNAME.getKey(zoneId);
    }

    private boolean saveNickNameIfAbsent(String zoneId, String nickName) {
        String key = getNickNameKey(zoneId);
        return redisService.hsetnx(key, nickName, "0");
    }

    private long getNextPlayerId(String zoneId) {
        String key = RedisKeyEnum.PLAYER_ID_INCR.getKey(zoneId);
        return redisService.incr(key);
    }

    private void updatePlayerIdForNickName(String zoneId, String nickName, long playerId) {
        String key = getNickNameKey(zoneId);
        redisService.hset(key, nickName, playerId);
    }

    public Player createPlayer(String zoneId, String nickName) {
        boolean saveNickName = saveNickNameIfAbsent(zoneId, nickName);//先占坑
        if (!saveNickName) {
            throw ErrorException.newBuilder(GameCenterError.NICKNAME_EXIST).message(nickName).build();
        }
        long playerId = getNextPlayerId(zoneId);
        Player player = new Player();
        player.setPlayerId(playerId);
        player.setNickName(nickName);
        player.setLastLoginTime(System.nanoTime());
        player.setCreateTime(player.getLastLoginTime());
        updatePlayerIdForNickName(zoneId, nickName, playerId);// 再次更新一下nickName对应的playerId
        playerDao.saveOrUpdate(player, playerId);
        log.info("角色创建成功,{}", player);
        return player;
    }

}
