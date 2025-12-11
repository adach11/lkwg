package com.seele.game.config;

import com.seele.game.entity.PetLevelSkill;
import com.seele.game.entity.PetTemplate;
import com.seele.game.entity.Skill;
import com.seele.game.enums.PetRarity;
import com.seele.game.enums.PetStatus;
import com.seele.game.enums.PetType;
import com.seele.game.enums.SkillType;
import com.yourname.petbattle.enums.*;
import com.seele.game.repository.PetLevelSkillRepository;
import com.seele.game.repository.PetTemplateRepository;
import com.seele.game.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 数据初始化器
 * 应用启动时初始化基础数据（宠物模板、技能等）
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PetTemplateRepository petTemplateRepository;
    private final SkillRepository skillRepository;
    private final PetLevelSkillRepository petLevelSkillRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("开始初始化游戏数据...");

        // 检查是否已初始化
        if (petTemplateRepository.count() > 0) {
            log.info("数据已存在，跳过初始化");
            return;
        }

        initializeSkills();
        initializePets();
        initializePetSkills();

        log.info("游戏数据初始化完成！");
    }

    /**
     * 初始化技能
     */
    private void initializeSkills() {
        log.info("初始化技能数据...");

        // 火系技能
        createSkill("火花", SkillType.PHYSICAL, PetType.FIRE, 40, 100, 25, 0,
                   null, 0, "发射小火焰攻击对手");
        createSkill("火焰喷射", SkillType.MAGIC, PetType.FIRE, 90, 100, 15, 0,
                   PetStatus.BURN, 10, "喷射烈焰攻击对手，有10%几率造成烧伤");
        createSkill("火焰旋涡", SkillType.MAGIC, PetType.FIRE, 120, 85, 10, 0,
                   null, 0, "强力的火焰攻击");
        createSkill("炽热冲锋", SkillType.PHYSICAL, PetType.FIRE, 85, 100, 15, 0,
                   null, 0, "用火焰包裹身体冲撞对手");

        // 水系技能
        createSkill("水枪", SkillType.PHYSICAL, PetType.WATER, 40, 100, 25, 0,
                   null, 0, "用水枪攻击对手");
        createSkill("水炮", SkillType.MAGIC, PetType.WATER, 110, 80, 5, 0,
                   null, 0, "发射强力水炮攻击");
        createSkill("冰冻光线", SkillType.MAGIC, PetType.WATER, 90, 100, 10, 0,
                   PetStatus.FREEZE, 10, "发射冰冷光线，有10%几率冰冻对手");
        createSkill("水之波动", SkillType.MAGIC, PetType.WATER, 60, 100, 20, 0,
                   null, 0, "水的波动攻击");

        // 草系技能
        createSkill("藤鞭", SkillType.PHYSICAL, PetType.GRASS, 45, 100, 25, 0,
                   null, 0, "用藤鞭抽打对手");
        createSkill("飞叶快刀", SkillType.PHYSICAL, PetType.GRASS, 55, 95, 25, 0,
                   null, 0, "飞叶切割对手");
        createSkill("寄生种子", SkillType.STATUS, PetType.GRASS, 0, 90, 10, 0,
                   PetStatus.POISON, 100, "种下寄生种子，每回合吸取对手HP");
        createSkill("阳光烈焰", SkillType.MAGIC, PetType.GRASS, 120, 100, 10, 0,
                   null, 0, "收集阳光能量发动强力攻击");

        // 电系技能
        createSkill("电击", SkillType.MAGIC, PetType.ELECTRIC, 40, 100, 30, 0,
                   PetStatus.PARALYSIS, 10, "发出电击攻击，有10%几率麻痹对手");
        createSkill("十万伏特", SkillType.MAGIC, PetType.ELECTRIC, 90, 100, 15, 0,
                   PetStatus.PARALYSIS, 10, "强力电击攻击");
        createSkill("打雷", SkillType.MAGIC, PetType.ELECTRIC, 110, 70, 10, 0,
                   PetStatus.PARALYSIS, 30, "召唤雷电攻击，威力巨大但命中率较低");
        createSkill("电光一闪", SkillType.PHYSICAL, PetType.ELECTRIC, 40, 100, 30, 1,
                   null, 0, "先制攻击，必定先手");

        // 土系技能
        createSkill("落石", SkillType.PHYSICAL, PetType.GROUND, 50, 90, 15, 0,
                   null, 0, "扔石头攻击对手");
        createSkill("地震", SkillType.PHYSICAL, PetType.GROUND, 100, 100, 10, 0,
                   null, 0, "引发地震攻击全场");
        createSkill("挖洞", SkillType.PHYSICAL, PetType.GROUND, 80, 100, 10, 0,
                   null, 0, "钻入地下后发动攻击");

        // 龙系技能
        createSkill("龙之怒", SkillType.MAGIC, PetType.DRAGON, 40, 100, 10, 0,
                   null, 0, "释放龙之力量");
        createSkill("龙爪", SkillType.PHYSICAL, PetType.DRAGON, 80, 100, 15, 0,
                   null, 0, "用锋利的爪子撕裂对手");
        createSkill("龙之波动", SkillType.MAGIC, PetType.DRAGON, 85, 100, 10, 0,
                   null, 0, "释放龙之冲击波");
        createSkill("逆鳞", SkillType.PHYSICAL, PetType.DRAGON, 120, 100, 10, 0,
                   null, 0, "失去理智的强力攻击");

        // 通用技能
        createSkill("撞击", SkillType.PHYSICAL, PetType.FIRE, 40, 100, 35, 0,
                   null, 0, "用身体撞击对手");
        createSkill("叫声", SkillType.STATUS, PetType.FIRE, 0, 100, 40, 0,
                   null, 0, "发出叫声降低对手攻击");
        createSkill("睡觉", SkillType.STATUS, PetType.FIRE, 0, 100, 10, 0,
                   null, 0, "睡觉恢复HP");

        log.info("初始化了{}个技能", skillRepository.count());
    }

    /**
     * 初始化宠物
     */
    private void initializePets() {
        log.info("初始化宠物数据...");

        // 初始宠物 - 火系
        createPet("火焰犬", PetType.FIRE, PetRarity.RARE, true,
                 50, 65, 45, 55, 50, 60,
                 3.0, 2.5, 1.8, 2.0, 1.8, 2.2,
                 "忠诚的火系宠物，速度较快");

        // 初始宠物 - 水系
        createPet("水灵龟", PetType.WATER, PetRarity.RARE, true,
                 60, 50, 65, 50, 60, 40,
                 3.2, 1.8, 2.5, 1.8, 2.3, 1.5,
                 "防御力强的水系宠物");

        // 初始宠物 - 草系
        createPet("青叶蛇", PetType.GRASS, PetRarity.RARE, true,
                 55, 55, 50, 60, 55, 55,
                 2.8, 2.0, 1.9, 2.3, 2.0, 2.0,
                 "平衡型的草系宠物");

        // 其他宠物
        createPet("雷电鼠", PetType.ELECTRIC, PetRarity.UNCOMMON, false,
                 45, 55, 40, 70, 45, 75,
                 2.5, 2.0, 1.5, 2.8, 1.7, 2.8,
                 "速度极快的电系宠物");

        createPet("岩石怪", PetType.GROUND, PetRarity.UNCOMMON, false,
                 70, 80, 90, 40, 70, 30,
                 3.5, 3.0, 3.5, 1.5, 2.5, 1.2,
                 "物理防御超高的土系宠物");

        createPet("幼龙", PetType.DRAGON, PetRarity.EPIC, false,
                 60, 70, 60, 70, 60, 65,
                 3.0, 2.8, 2.3, 2.8, 2.3, 2.5,
                 "潜力巨大的龙系宠物");

        createPet("炎魔", PetType.FIRE, PetRarity.EPIC, false,
                 70, 90, 60, 110, 70, 80,
                 3.3, 3.5, 2.2, 4.0, 2.5, 3.0,
                 "火系高级宠物，魔攻超强");

        createPet("海皇", PetType.WATER, PetRarity.EPIC, false,
                 85, 75, 80, 90, 85, 60,
                 3.8, 2.8, 3.0, 3.5, 3.2, 2.2,
                 "水系高级宠物，生存能力强");

        createPet("森林之王", PetType.GRASS, PetRarity.EPIC, false,
                 75, 80, 75, 95, 80, 70,
                 3.5, 3.0, 2.8, 3.8, 3.0, 2.6,
                 "草系高级宠物，全面均衡");

        createPet("雷神", PetType.ELECTRIC, PetRarity.LEGENDARY, false,
                 70, 85, 60, 125, 70, 110,
                 3.2, 3.2, 2.2, 5.0, 2.5, 4.5,
                 "传说中的电系宠物，速度和魔攻惊人");

        log.info("初始化了{}只宠物", petTemplateRepository.count());
    }

    /**
     * 初始化宠物可学习的技能
     */
    private void initializePetSkills() {
        log.info("初始化宠物技能学习配置...");

        // 火焰犬技能
        PetTemplate fireDog = petTemplateRepository.findByName("火焰犬");
        Skill spark = skillRepository.findByName("火花");
        Skill flamethrower = skillRepository.findByName("火焰喷射");
        Skill tackle = skillRepository.findByName("撞击");
        Skill fireCharge = skillRepository.findByName("炽热冲锋");

        createPetLevelSkill(fireDog.getId(), tackle.getId(), 1);
        createPetLevelSkill(fireDog.getId(), spark.getId(), 5);
        createPetLevelSkill(fireDog.getId(), flamethrower.getId(), 15);
        createPetLevelSkill(fireDog.getId(), fireCharge.getId(), 25);

        // 水灵龟技能
        PetTemplate waterTurtle = petTemplateRepository.findByName("水灵龟");
        Skill waterGun = skillRepository.findByName("水枪");
        Skill iceBeam = skillRepository.findByName("冰冻光线");
        Skill waterPulse = skillRepository.findByName("水之波动");

        createPetLevelSkill(waterTurtle.getId(), tackle.getId(), 1);
        createPetLevelSkill(waterTurtle.getId(), waterGun.getId(), 5);
        createPetLevelSkill(waterTurtle.getId(), waterPulse.getId(), 12);
        createPetLevelSkill(waterTurtle.getId(), iceBeam.getId(), 20);

        // 青叶蛇技能
        PetTemplate grassSnake = petTemplateRepository.findByName("青叶蛇");
        Skill vineWhip = skillRepository.findByName("藤鞭");
        Skill razorLeaf = skillRepository.findByName("飞叶快刀");
        Skill leechSeed = skillRepository.findByName("寄生种子");

        createPetLevelSkill(grassSnake.getId(), tackle.getId(), 1);
        createPetLevelSkill(grassSnake.getId(), vineWhip.getId(), 5);
        createPetLevelSkill(grassSnake.getId(), razorLeaf.getId(), 10);
        createPetLevelSkill(grassSnake.getId(), leechSeed.getId(), 15);

        // 雷电鼠技能
        PetTemplate elecMouse = petTemplateRepository.findByName("雷电鼠");
        Skill thunder = skillRepository.findByName("电击");
        Skill quickAttack = skillRepository.findByName("电光一闪");
        Skill thunderbolt = skillRepository.findByName("十万伏特");

        createPetLevelSkill(elecMouse.getId(), quickAttack.getId(), 1);
        createPetLevelSkill(elecMouse.getId(), thunder.getId(), 8);
        createPetLevelSkill(elecMouse.getId(), thunderbolt.getId(), 20);

        log.info("初始化了{}条宠物技能配置", petLevelSkillRepository.count());
    }

    private void createSkill(String name, SkillType skillType, PetType petType,
                            int power, int accuracy, int maxPp, int priority,
                            PetStatus statusEffect, int effectChance, String description) {
        Skill skill = new Skill();
        skill.setName(name);
        skill.setSkillType(skillType);
        skill.setPetType(petType);
        skill.setPower(power);
        skill.setAccuracy(accuracy);
        skill.setMaxPp(maxPp);
        skill.setPriority(priority);
        skill.setStatusEffect(statusEffect);
        skill.setEffectChance(effectChance);
        skill.setDescription(description);
        skillRepository.save(skill);
    }

    private void createPet(String name, PetType type, PetRarity rarity, boolean isStarter,
                          int baseHp, int baseAtk, int baseDef, int baseMagAtk, int baseMagDef, int baseSpd,
                          double hpGrowth, double atkGrowth, double defGrowth,
                          double magAtkGrowth, double magDefGrowth, double spdGrowth,
                          String description) {
        PetTemplate pet = new PetTemplate();
        pet.setName(name);
        pet.setType(type);
        pet.setRarity(rarity);
        pet.setIsStarter(isStarter);
        pet.setBaseHp(baseHp);
        pet.setBaseAttack(baseAtk);
        pet.setBaseDefense(baseDef);
        pet.setBaseMagicAttack(baseMagAtk);
        pet.setBaseMagicDefense(baseMagDef);
        pet.setBaseSpeed(baseSpd);
        pet.setHpGrowth(hpGrowth);
        pet.setAttackGrowth(atkGrowth);
        pet.setDefenseGrowth(defGrowth);
        pet.setMagicAttackGrowth(magAtkGrowth);
        pet.setMagicDefenseGrowth(magDefGrowth);
        pet.setSpeedGrowth(spdGrowth);
        pet.setDescription(description);
        pet.setImageUrl("/images/pets/" + name + ".png");
        petTemplateRepository.save(pet);
    }

    private void createPetLevelSkill(Long petTemplateId, Long skillId, int learnLevel) {
        PetLevelSkill pls = new PetLevelSkill();
        pls.setPetTemplateId(petTemplateId);
        pls.setSkillId(skillId);
        pls.setLearnLevel(learnLevel);
        petLevelSkillRepository.save(pls);
    }
}
