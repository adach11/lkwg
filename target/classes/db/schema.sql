-- 宠物对战游戏数据库表结构
-- 数据库: lkwg-dev

-- 1. 宠物模板表
CREATE TABLE IF NOT EXISTS `pet_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '宠物名称',
  `type` VARCHAR(20) NOT NULL COMMENT '属性类型：FIRE/WATER/GRASS/ELECTRIC/GROUND/DRAGON',
  `rarity` VARCHAR(20) NOT NULL COMMENT '稀有度：COMMON/UNCOMMON/RARE/EPIC/LEGENDARY',
  `base_hp` INT NOT NULL COMMENT '基础生命值',
  `base_attack` INT NOT NULL COMMENT '基础攻击力',
  `base_defense` INT NOT NULL COMMENT '基础防御力',
  `base_magic_attack` INT NOT NULL COMMENT '基础魔法攻击力',
  `base_magic_defense` INT NOT NULL COMMENT '基础魔法防御力',
  `base_speed` INT NOT NULL COMMENT '基础速度',
  `hp_growth` DOUBLE NOT NULL COMMENT 'HP成长率',
  `attack_growth` DOUBLE NOT NULL COMMENT '攻击成长率',
  `defense_growth` DOUBLE NOT NULL COMMENT '防御成长率',
  `magic_attack_growth` DOUBLE NOT NULL COMMENT '魔攻成长率',
  `magic_defense_growth` DOUBLE NOT NULL COMMENT '魔防成长率',
  `speed_growth` DOUBLE NOT NULL COMMENT '速度成长率',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '宠物描述',
  `image_url` VARCHAR(255) DEFAULT NULL COMMENT '图片URL',
  `is_starter` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为初始宠物',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_type` (`type`),
  KEY `idx_rarity` (`rarity`),
  KEY `idx_is_starter` (`is_starter`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物模板表';

-- 2. 技能表
CREATE TABLE IF NOT EXISTS `skill` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '技能名称',
  `skill_type` VARCHAR(20) NOT NULL COMMENT '技能类型：PHYSICAL/MAGIC/STATUS',
  `pet_type` VARCHAR(20) NOT NULL COMMENT '技能属性',
  `power` INT NOT NULL COMMENT '威力(0-150)',
  `accuracy` INT NOT NULL COMMENT '命中率(30-100)',
  `max_pp` INT NOT NULL COMMENT '最大使用次数',
  `priority` INT NOT NULL DEFAULT 0 COMMENT '先制度(-7到+7)',
  `status_effect` VARCHAR(20) DEFAULT NULL COMMENT '附加状态效果',
  `effect_chance` INT DEFAULT NULL COMMENT '状态效果触发概率(0-100)',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '技能描述',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_skill_type` (`skill_type`),
  KEY `idx_pet_type` (`pet_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能表';

-- 3. 玩家宠物实例表
CREATE TABLE IF NOT EXISTS `player_pet` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `player_id` BIGINT NOT NULL COMMENT '玩家ID',
  `pet_template_id` BIGINT NOT NULL COMMENT '宠物模板ID',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `level` INT NOT NULL DEFAULT 1 COMMENT '当前等级',
  `exp` INT NOT NULL DEFAULT 0 COMMENT '当前经验值',
  `next_level_exp` INT NOT NULL COMMENT '升到下一级所需经验值',
  `iv_hp` INT NOT NULL COMMENT '生命值IV(0-31)',
  `iv_attack` INT NOT NULL COMMENT '攻击力IV(0-31)',
  `iv_defense` INT NOT NULL COMMENT '防御力IV(0-31)',
  `iv_magic_attack` INT NOT NULL COMMENT '魔攻IV(0-31)',
  `iv_magic_defense` INT NOT NULL COMMENT '魔防IV(0-31)',
  `iv_speed` INT NOT NULL COMMENT '速度IV(0-31)',
  `current_hp` INT NOT NULL COMMENT '当前HP',
  `max_hp` INT NOT NULL COMMENT '最大HP',
  `attack` INT NOT NULL COMMENT '攻击力',
  `defense` INT NOT NULL COMMENT '防御力',
  `magic_attack` INT NOT NULL COMMENT '魔法攻击力',
  `magic_defense` INT NOT NULL COMMENT '魔法防御力',
  `speed` INT NOT NULL COMMENT '速度',
  `status` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '当前状态',
  `friendship` INT NOT NULL DEFAULT 50 COMMENT '亲密度(0-255)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_pet_template_id` (`pet_template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家宠物实例表';

-- 4. 宠物升级技能关联表
CREATE TABLE IF NOT EXISTS `pet_level_skill` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pet_template_id` BIGINT NOT NULL COMMENT '宠物模板ID',
  `skill_id` BIGINT NOT NULL COMMENT '技能ID',
  `learn_level` INT NOT NULL COMMENT '学会等级',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pet_skill_level` (`pet_template_id`, `skill_id`, `learn_level`),
  KEY `idx_pet_template_id` (`pet_template_id`),
  KEY `idx_skill_id` (`skill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物升级技能关联表';

-- 5. 玩家宠物已学会的技能表
CREATE TABLE IF NOT EXISTS `player_pet_skill` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `player_pet_id` BIGINT NOT NULL COMMENT '玩家宠物ID',
  `skill_id` BIGINT NOT NULL COMMENT '技能ID',
  `current_pp` INT NOT NULL COMMENT '当前PP',
  `max_pp` INT NOT NULL COMMENT '最大PP',
  `position` INT DEFAULT NULL COMMENT '技能位置(0-3)',
  `is_equipped` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否装备',
  `learned_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '学会时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pet_skill` (`player_pet_id`, `skill_id`),
  KEY `idx_player_pet_id` (`player_pet_id`),
  KEY `idx_skill_id` (`skill_id`),
  KEY `idx_is_equipped` (`is_equipped`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家宠物技能表';

-- 6. 玩家队伍表
CREATE TABLE IF NOT EXISTS `player_team` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `player_id` BIGINT NOT NULL COMMENT '玩家ID',
  `position` INT NOT NULL COMMENT '队伍位置(1-6)',
  `player_pet_id` BIGINT DEFAULT NULL COMMENT '宠物ID',
  `team_name` VARCHAR(50) DEFAULT NULL COMMENT '队伍名称',
  `is_active` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为当前使用的队伍',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_position` (`player_id`, `position`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_player_pet_id` (`player_pet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家队伍表';
