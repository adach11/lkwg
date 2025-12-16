package com.seele.game.service;

import com.seele.game.entity.PlayerPet;

/**
 * 宠物成长服务接口
 * 负责经验值、升级、属性计算等
 */
public interface IPetGrowthService {

    /**
     * 计算升级到指定等级所需的总经验值
     * 使用中速成长曲线：exp = level^3
     */
    int calculateExpForLevel(int level);

    /**
     * 计算两个等级之间需要的经验值
     */
    int calculateExpBetweenLevels(int fromLevel, int toLevel);

    /**
     * 增加经验值，可能触发升级
     * @param playerPet 玩家宠物
     * @param expGained 获得的经验值
     * @return 是否升级
     */
    boolean addExp(PlayerPet playerPet, int expGained);

    /**
     * 重新计算宠物的所有属性
     * 公式：最终属性 = (基础值 + 成长率 × 等级 + IV) × 倍率
     */
    void recalculateStats(PlayerPet playerPet);

    /**
     * 生成随机个体值 (0-31)
     */
    int generateRandomIV();

    /**
     * 生成一组完整的随机个体值
     */
    int[] generateRandomIVs();
}
