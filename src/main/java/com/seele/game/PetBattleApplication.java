package com.seele.game;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 宠物对战游戏主应用
 */
@SpringBootApplication
@MapperScan("com.seele.game.mapper")
public class PetBattleApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetBattleApplication.class, args);
    }
}
