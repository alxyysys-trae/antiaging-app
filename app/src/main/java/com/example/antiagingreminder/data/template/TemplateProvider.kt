package com.example.antiagingreminder.data.template

import com.example.antiagingreminder.data.PlanType
import com.example.antiagingreminder.data.local.entity.TemplateEntity

/**
 * 模板数据提供者。
 *
 * 基于世界顶级抗衰选手（Siim Land、Julie Gibson、Dan Sullivan、Bryan Johnson、清水健）
 * 的科学方法，结合用户个人情况（24岁/177cm/65kg/BMI 20.7）定制。
 *
 * 核心策略：
 * - 睡眠优化：22:30 入睡，睡前冷热交替疗法 + 蓝光阻断
 * - 16:8 断食：进食窗口 07:00-15:00，激活细胞自噬
 * - 高蛋白饮食：每日 100-130g 蛋白质，支持肌肉合成与睾酮
 * - 补剂方案：NMN 410mg + 锌 15mg + D3 2000IU + 甘氨酸镁
 * - 运动方案：周一上肢/周三下肢/周五有氧+冷水浴/周末户外
 * - 盆底训练：每日凯格尔运动，改善射精控制
 * - 痔疮管理：低冲击训练 + 温水坐浴 + 膳食纤维
 */
object TemplateProvider {

    fun allTemplates(): List<TemplateEntity> = listOf(

        // ==================== 睡眠优化（基于 Siim Land / Julie Gibson） ====================

        TemplateEntity(
            title = "睡前准备：关闭电子设备",
            description = "关闭手机、电脑等电子设备，调暗室内灯光。提前 30 分钟关闭电子设备可使褪黑素水平提高 30%，改善睡眠质量。睡前可进行 5 分钟深呼吸冥想。",
            type = PlanType.MINDFULNESS.name,
            times = "21:30",
            repeatDays = "1,2,3,4,5,6,7"
        ),
        TemplateEntity(
            title = "睡前冷热交替疗法",
            description = "先温水淋浴 15 分钟放松肌肉，最后 30 秒切换至冷水（18-20°C）。温水后短暂冷水浴可激活棕色脂肪、提高睡眠质量、激活 AMPK 抗衰通路。来自 Julie Gibson 的桑拿+冷水浴策略。",
            type = PlanType.EXERCISE.name,
            times = "22:00",
            repeatDays = "1,2,3,4,5,6,7"
        ),
        TemplateEntity(
            title = "蓝光阻断",
            description = "开启手机夜间模式或佩戴防蓝光眼镜。蓝光抑制褪黑素分泌延迟入睡，阻断蓝光可使褪黑素提前 30-90 分钟分泌。来自 Siim Land 的睡前一小时防蓝光策略。",
            type = PlanType.MINDFULNESS.name,
            times = "20:00",
            repeatDays = "1,2,3,4,5,6,7"
        ),
        TemplateEntity(
            title = "睡前正念冥想",
            description = "睡前 10-15 分钟 4-7-8 呼吸法或身体扫描冥想。降低皮质醇水平、激活副交感神经、改善深睡眠比例。深睡眠是生长激素分泌的关键时段。来自 Dan Sullivan 的 30 分钟正念冥想策略。",
            type = PlanType.MINDFULNESS.name,
            times = "22:15",
            repeatDays = "1,2,3,4,5,6,7"
        ),

        // ==================== 晨间启动（基于 Siim Land / Bryan Johnson） ====================

        TemplateEntity(
            title = "晨起温水 + 柠檬",
            description = "起床后空腹饮用 300-500ml 温水（37-40°C），可加数滴柠檬汁。唤醒消化系统、补充夜间流失水分、促进肝脏排毒。等待 15 分钟后再进食。",
            type = PlanType.WATER.name,
            times = "06:05",
            repeatDays = "1,2,3,4,5,6,7"
        ),
        TemplateEntity(
            title = "凯格尔运动（晨间）",
            description = "收缩盆底肌 5-10 秒后放松 5-10 秒，每组 10 次，共 3 组。早晨进行盆底肌训练可提高全天性功能，改善射精控制力。注意避免腹部、臀部代偿，保持正常呼吸。来自 Dan Sullivan 的每日凯格尔策略。",
            type = PlanType.EXERCISE.name,
            times = "06:15",
            repeatDays = "1,2,3,4,5,6,7"
        ),
        TemplateEntity(
            title = "核心训练：平板支撑",
            description = "手肘与肩同宽，身体保持一条线，避免塌腰。吸气 2 秒，呼气 4 秒，不憋气（吹蜡烛呼吸法）。从 30 秒逐渐增至 60 秒，共 4 组。强化深层核心肌群，改善腰腹力量，低冲击适合痔疮患者。来自用户方案中的早餐前核心训练。",
            type = PlanType.EXERCISE.name,
            times = "06:20",
            repeatDays = "1,2,3,4,5,6,7"
        ),
        TemplateEntity(
            title = "早晨训练：有氧热身",
            description = "早餐后进行 10 分钟有氧热身（快走或跳绳）。2024 年《运动医学》研究显示，早晨有氧运动可提高睾酮水平。同时到户外接受自然光照 10-20 分钟，调节昼夜节律、促进皮质醇正常分泌。来自用户方案的 7:30-8:00 早晨训练。",
            type = PlanType.EXERCISE.name,
            times = "07:30",
            repeatDays = "1,2,3,4,5,6,7"
        ),

        // ==================== 补剂方案（基于用户个人方案） ====================

        TemplateEntity(
            title = "NMN + 白藜芦醇（空腹）",
            description = "空腹服用 NMN 410mg + 白藜芦醇 100mg + 紫檀芪 100mg + 麦角硫因 50mg + PQQ 20mg + 亚精胺 20mg。激活 SIRT1 长寿基因、提升 NAD+ 水平、维持细胞自噬活性。晨间空腹服用效果最佳。",
            type = PlanType.DIET.name,
            times = "06:45",
            repeatDays = "1,2,3,4,5,6,7"
        ),
        TemplateEntity(
            title = "维生素 D3 + K2 + 锌（随早餐）",
            description = "随早餐服用维生素 D3（2000IU）+ K2（100mcg）+ 锌（15mg）。D3 为脂溶性须随含脂肪的餐食服用；锌支持睾酮合成、改善性欲。来自 Julie Gibson 的 D3+K2 补充策略。",
            type = PlanType.DIET.name,
            times = "07:00",
            repeatDays = "1,2,3,4,5,6,7"
        ),
        TemplateEntity(
            title = "辅酶 Q10 + Omega-3（随午餐）",
            description = "随午餐服用辅酶 Q10（100-200mg）+ 高品质鱼油（EPA+DHA 1000mg+）。抗炎护心、维护线粒体功能、降低前列腺炎症。Q10 与鱼油均为脂溶性，须随餐服用。单位就餐日额外补充姜黄素 500mg + 维C 500mg 抗炎。",
            type = PlanType.DIET.name,
            times = "12:30",
            repeatDays = "1,3,5"
        ),
        TemplateEntity(
            title = "甘氨酸镁（睡前）",
            description = "睡前 30 分钟服用甘氨酸镁（200-400mg）。促进肌肉放松、改善睡眠质量、调节神经传导。甘氨酸镁吸收好且不易腹泻，优于氧化镁。来自 Julie Gibson 的甘氨酸镁+牛磺酸镁策略。",
            type = PlanType.DIET.name,
            times = "22:00",
            repeatDays = "1,2,3,4,5,6,7"
        ),

        // ==================== 饮食与断食（基于 Siim Land 16:8） ====================

        TemplateEntity(
            title = "高蛋白早餐",
            description = "燕麦 100g + 蛋白质粉 20g + 水果 100g，或鸡蛋 3 个 + 坚果 20g + 蔬菜沙拉 150g。每日蛋白质目标 100-130g（1.5-2g/kg 体重），早餐约占 30-40g。",
            type = PlanType.DIET.name,
            times = "06:30",
            repeatDays = "1,2,3,4,5,6,7"
        ),
        TemplateEntity(
            title = "16:8 断食开始",
            description = "晚餐后进入断食窗口，不再进食。适应期约12小时断食（19:00-7:00），强化期逐步延长至16小时（可将早餐延至10:00或取消晚餐）。断食期间仅允许饮水、黑咖啡或绿茶。16小时断食激活细胞自噬、降低炎症因子、改善胰岛素敏感性。来自 Siim Land 的 16:8 断食法。",
            type = PlanType.DIET.name,
            times = "19:00",
            repeatDays = "1,2,3,4,5,6,7"
        ),
        TemplateEntity(
            title = "午餐：高蛋白低脂",
            description = "杂粮饭 1 拳 + 瘦肉/鱼肉 1 拳 + 蔬菜 1.5-2 拳。单位就餐日选择清蒸鱼、鸡胸肉等低脂菜品，避免油炸。餐后补充 Omega-3（亚麻籽油 1 汤匙或核桃 10-15 粒）抗炎。",
            type = PlanType.DIET.name,
            times = "12:30",
            repeatDays = "1,2,3,4,5,6,7"
        ),
        TemplateEntity(
            title = "晚餐：蛋白质 + 蔬菜",
            description = "鸡胸肉/豆腐 1 拳 + 蔬菜 2 拳 + 健康脂肪 1 汤匙（橄榄油/坚果）。18:00 前完成进食以优化睡眠质量。增加膳食纤维（燕麦、糙米、西兰花、菠菜、芹菜）预防痔疮。避免辛辣、酒精等刺激性食物。来自用户方案的 18:00-18:30 晚餐。",
            type = PlanType.DIET.name,
            times = "18:00",
            repeatDays = "1,2,3,4,5,6,7"
        ),
        TemplateEntity(
            title = "抗氧化水果加餐",
            description = "蓝莓 150g 或草莓、黑莓等浆果，富含花青素与多酚类抗氧化物，清除自由基、保护端粒。避免高糖水果。来自 Dan Sullivan 的抗氧化饮食策略。",
            type = PlanType.DIET.name,
            times = "10:30",
            repeatDays = "1,2,3,4,5"
        ),

        // ==================== 运动方案（基于清水健 / Siim Land） ====================

        TemplateEntity(
            title = "周一：上肢力量 + 凯格尔",
            description = "胸推 4×12-15 + 引体向上 4×10-12 + 平板支撑 4×30-60 秒 + 凯格尔 3×10-15 次。约 2 小时。高蛋白饮食维持肌肉量，凯格尔增强盆底肌群改善射精控制。",
            type = PlanType.EXERCISE.name,
            times = "17:00",
            repeatDays = "1"
        ),
        TemplateEntity(
            title = "周三：下肢力量 + 凯格尔",
            description = "靠墙静蹲 4×30-60 秒 + 臀桥 4×12-15 + 死虫式 3×15-20 + 凯格尔 3×10-15 次。约 2 小时。靠墙静蹲替代深蹲，避免腹压骤升加重痔疮；臀桥可提升睾酮水平（清水健），死虫式强化深层核心。全程使用吹蜡烛呼吸法（吸气 2 秒、呼气 4 秒），绝不憋气。",
            type = PlanType.EXERCISE.name,
            times = "17:00",
            repeatDays = "3"
        ),
        TemplateEntity(
            title = "周五：有氧 + 冷水浴",
            description = "5 公里慢跑（配速 6-7 分钟/公里）+ 10 分钟凯格尔 + 10 分钟冷水浴（18-20°C）。约 2 小时。有氧提高心肺功能，冷水浴激活 AMPK 通路和 Sirtuin 长寿蛋白，改善性功能。",
            type = PlanType.EXERCISE.name,
            times = "17:00",
            repeatDays = "5"
        ),
        TemplateEntity(
            title = "周末：户外活动",
            description = "慢走或骑行 30-60 分钟，或游泳 30-45 分钟。游泳低冲击适合痔疮患者。清水健的冷水浴改善血管内皮功能，促进血液循环。每日步行 7000-8000 步。",
            type = PlanType.EXERCISE.name,
            times = "09:00",
            repeatDays = "6,7"
        ),

        // ==================== 饮水管理 ====================

        TemplateEntity(
            title = "分时补水 2000ml",
            description = "分 6-8 次小口饮水，每次 200-300ml，全天总量 2000ml+。保持肠道湿润预防痔疮，促进代谢废物排出。避免一次大量饮水增加肾脏负担。",
            type = PlanType.WATER.name,
            times = "07:00,09:30,11:30,13:30,15:00,17:00",
            repeatDays = "1,2,3,4,5,6,7"
        ),

        // ==================== 健康管理 ====================

        TemplateEntity(
            title = "久坐起身活动",
            description = "每工作 60 分钟起身活动 5 分钟，拉伸颈肩、胸椎与髋部。配合深呼吸 5 次降低交感神经张力。改善血液循环，预防痔疮，保护颈椎。来自 Bryan Johnson 的每 30 分钟轻度运动策略。",
            type = PlanType.EXERCISE.name,
            times = "10:00,11:00,14:00,15:00,16:00,20:00",
            repeatDays = "1,2,3,4,5"
        ),
        TemplateEntity(
            title = "NASA 式午间小憩",
            description = "午休时进行 20 分钟 NASA 式小睡（不超过 30 分钟，避免进入深睡眠）。NASA 研究显示 26 分钟小睡可提升飞行员表现 34%、警觉性 54%。熬夜后次日可增加 90 分钟慢波睡眠补偿。设好闹钟，避免睡过头影响夜间入睡。",
            type = PlanType.MINDFULNESS.name,
            times = "13:00",
            repeatDays = "1,2,3,4,5"
        ),
        TemplateEntity(
            title = "痔疮护理：温水坐浴",
            description = "训练后或睡前进行 39°C 温水坐浴 15-20 分钟。促进局部血液循环、缓解症状。每日饮水 2000ml+，增加膳食纤维（燕麦、糙米、西兰花）。避免辛辣、酒精。",
            type = PlanType.OTHER.name,
            times = "22:30",
            repeatDays = "1,2,3,4,5,6,7"
        )
    )
}
