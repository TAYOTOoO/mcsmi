/**
 * Minecraft 材质包版本转换器 - 常量定义
 * 从 Python 桌面版移植而来
 */

// pack_format 到版本字符串的映射
export const PACK_FORMAT_MAP = {
  1: '1.6-1.8',
  2: '1.9-1.10',
  3: '1.11-1.12',
  4: '1.13-1.14',
  5: '1.15-1.16.1',
  6: '1.16.2-1.16.5',
  7: '1.17',
  8: '1.18',
  9: '1.19-1.19.2',
  12: '1.19.3',
  13: '1.19.4',
  15: '1.20-1.20.1',
  18: '1.20.2',
  22: '1.20.3-1.20.4',
  32: '1.20.5-1.20.6',
  34: '1.21-1.21.1',
  42: '1.21.2-1.21.3',
  46: '1.21.4'
}

// 版本字符串到 pack_format 的反向映射
export const VERSION_TO_PACK_FORMAT_MAP = Object.fromEntries(
  Object.entries(PACK_FORMAT_MAP).map(([k, v]) => [v, parseInt(k)])
)

// pack_format 顺序
export const PACK_FORMAT_ORDER = [1, 2, 3, 4, 5, 6, 7, 8, 9, 12, 13, 15, 18, 22, 32, 34, 42, 46]

// 物品重命名映射 (旧名→新名, format 3→4)
export const ITEM_RENAME_PAIRS = {
  'gold_sword.png': 'golden_sword.png',
  'wood_sword.png': 'wooden_sword.png',
  'gold_helmet.png': 'golden_helmet.png',
  'gold_chestplate.png': 'golden_chestplate.png',
  'gold_leggings.png': 'golden_leggings.png',
  'gold_boots.png': 'golden_boots.png',
  'apple_golden.png': 'golden_apple.png',
  'bow_standby.png': 'bow.png',
  'book_enchanted.png': 'enchanted_book.png',
  'wood_axe.png': 'wooden_axe.png',
  'wood_pickaxe.png': 'wooden_pickaxe.png',
  'wood_shovel.png': 'wooden_shovel.png',
  'wood_hoe.png': 'wooden_hoe.png',
  'gold_axe.png': 'golden_axe.png',
  'gold_pickaxe.png': 'golden_pickaxe.png',
  'gold_shovel.png': 'golden_shovel.png',
  'gold_hoe.png': 'golden_hoe.png',
  'fishing_rod_uncast.png': 'fishing_rod.png',
  'potion_bottle_empty.png': 'glass_bottle.png',
  'potion_bottle_drinkable.png': 'potion.png',
  'potion_bottle_splash.png': 'splash_potion.png',
  'potion_bottle_lingering.png': 'lingering_potion.png',
  'spider_eye_fermented.png': 'fermented_spider_eye.png',
  'melon_speckled.png': 'glistering_melon_slice.png',
  'melon.png': 'melon_slice.png'
}

// 方块重命名映射 (旧名→新名)
export const BLOCK_RENAME_PAIRS = {
  'gold_block.png': 'gold_block.png',
  'lapis_block.png': 'lapis_block.png',
  'iron_block.png': 'iron_block.png',
  'quartz_block_top.png': 'quartz_block_top.png',
  'quartz_block_side.png': 'quartz_block_side.png',
  'quartz_block_bottom.png': 'quartz_block_bottom.png'
}

// 方块重命名处理函数中 使用的映射 (rename_and_process_blocks)
export const BLOCK_PROCESS_RENAME = {
  'grass_top.png': 'grass_block_top.png',
  'grass_side.png': 'grass_block_side.png',
  'grass_side_overlay.png': 'grass_block_side_overlay.png',
  'hardened_clay.png': 'terracotta.png',
  'log_oak.png': 'oak_log.png',
  'log_oak_top.png': 'oak_log_top.png',
  'log_birch.png': 'birch_log.png',
  'log_birch_top.png': 'birch_log_top.png',
  'log_spruce.png': 'spruce_log.png',
  'log_spruce_top.png': 'spruce_log_top.png',
  'log_jungle.png': 'jungle_log.png',
  'log_jungle_top.png': 'jungle_log_top.png',
  'log_acacia.png': 'acacia_log.png',
  'log_acacia_top.png': 'acacia_log_top.png',
  'log_big_oak.png': 'dark_oak_log.png',
  'log_big_oak_top.png': 'dark_oak_log_top.png',
  'planks_oak.png': 'oak_planks.png',
  'planks_birch.png': 'birch_planks.png',
  'planks_spruce.png': 'spruce_planks.png',
  'planks_jungle.png': 'jungle_planks.png',
  'planks_acacia.png': 'acacia_planks.png',
  'planks_big_oak.png': 'dark_oak_planks.png',
  'leaves_oak.png': 'oak_leaves.png',
  'leaves_birch.png': 'birch_leaves.png',
  'leaves_spruce.png': 'spruce_leaves.png',
  'leaves_jungle.png': 'jungle_leaves.png',
  'leaves_acacia.png': 'acacia_leaves.png',
  'leaves_big_oak.png': 'dark_oak_leaves.png',
  'sapling_oak.png': 'oak_sapling.png',
  'sapling_birch.png': 'birch_sapling.png',
  'sapling_spruce.png': 'spruce_sapling.png',
  'sapling_jungle.png': 'jungle_sapling.png',
  'sapling_acacia.png': 'acacia_sapling.png',
  'sapling_roofed_oak.png': 'dark_oak_sapling.png',
  'door_wood_upper.png': 'oak_door_top.png',
  'door_wood_lower.png': 'oak_door_bottom.png',
  'door_iron_upper.png': 'iron_door_top.png',
  'door_iron_lower.png': 'iron_door_bottom.png',
  'door_birch_upper.png': 'birch_door_top.png',
  'door_birch_lower.png': 'birch_door_bottom.png',
  'door_spruce_upper.png': 'spruce_door_top.png',
  'door_spruce_lower.png': 'spruce_door_bottom.png',
  'door_jungle_upper.png': 'jungle_door_top.png',
  'door_jungle_lower.png': 'jungle_door_bottom.png',
  'door_acacia_upper.png': 'acacia_door_top.png',
  'door_acacia_lower.png': 'acacia_door_bottom.png',
  'door_dark_oak_upper.png': 'dark_oak_door_top.png',
  'door_dark_oak_lower.png': 'dark_oak_door_bottom.png',
  'stonebrick.png': 'stone_bricks.png',
  'cobblestone_mossy.png': 'mossy_cobblestone.png',
  'stone_slab_top.png': 'smooth_stone.png',
  'stone_slab_side.png': 'smooth_stone_slab_side.png',
  'sandstone_top.png': 'sandstone_top.png',
  'sandstone_bottom.png': 'sandstone_bottom.png',
  'sandstone_normal.png': 'sandstone.png',
  'red_sandstone_top.png': 'red_sandstone_top.png',
  'red_sandstone_bottom.png': 'red_sandstone_bottom.png',
  'red_sandstone_normal.png': 'red_sandstone.png',
  'wool_colored_white.png': 'white_wool.png',
  'wool_colored_orange.png': 'orange_wool.png',
  'wool_colored_magenta.png': 'magenta_wool.png',
  'wool_colored_light_blue.png': 'light_blue_wool.png',
  'wool_colored_yellow.png': 'yellow_wool.png',
  'wool_colored_lime.png': 'lime_wool.png',
  'wool_colored_pink.png': 'pink_wool.png',
  'wool_colored_gray.png': 'gray_wool.png',
  'wool_colored_silver.png': 'light_gray_wool.png',
  'wool_colored_cyan.png': 'cyan_wool.png',
  'wool_colored_purple.png': 'purple_wool.png',
  'wool_colored_blue.png': 'blue_wool.png',
  'wool_colored_brown.png': 'brown_wool.png',
  'wool_colored_green.png': 'green_wool.png',
  'wool_colored_red.png': 'red_wool.png',
  'wool_colored_black.png': 'black_wool.png',
  'glass_pane_top.png': 'glass_pane_top.png',
  'torch_on.png': 'torch.png',
  'redstone_torch_on.png': 'redstone_torch.png',
  'trip_wire.png': 'tripwire.png',
  'trip_wire_source.png': 'tripwire_hook.png',
  'farmland_wet.png': 'farmland_moist.png',
  'farmland_dry.png': 'farmland.png',
  'furnace_front_off.png': 'furnace_front.png',
  'furnace_top.png': 'furnace_top.png',
  'furnace_side.png': 'furnace_side.png',
  'dispenser_front_horizontal.png': 'dispenser_front.png',
  'dropper_front_horizontal.png': 'dropper_front.png',
  'piston_top_normal.png': 'piston_top.png',
  'piston_top_sticky.png': 'piston_top_sticky.png',
  'piston_side.png': 'piston_side.png',
  'piston_bottom.png': 'piston_bottom.png',
  'piston_inner.png': 'piston_inner.png',
  'redstone_lamp_off.png': 'redstone_lamp.png',
  'redstone_lamp_on.png': 'redstone_lamp_on.png',
  'flower_rose.png': 'poppy.png',
  'flower_dandelion.png': 'dandelion.png',
  'mushroom_red.png': 'red_mushroom.png',
  'mushroom_brown.png': 'brown_mushroom.png',
  'waterlily.png': 'lily_pad.png',
  'deadbush.png': 'dead_bush.png',
  'tallgrass.png': 'grass.png',
  'fern.png': 'fern.png',
  'vine.png': 'vine.png',
  'web.png': 'cobweb.png',
  'snow.png': 'snow.png',
  'rail_normal.png': 'rail.png',
  'rail_normal_turned.png': 'rail_corner.png',
  'rail_golden.png': 'powered_rail.png',
  'rail_golden_powered.png': 'powered_rail_on.png',
  'rail_detector.png': 'detector_rail.png',
  'rail_detector_powered.png': 'detector_rail_on.png',
  'rail_activator.png': 'activator_rail.png',
  'rail_activator_powered.png': 'activator_rail_on.png',
  'comparator_off.png': 'comparator.png',
  'comparator_on.png': 'comparator_on.png',
  'repeater_off.png': 'repeater.png',
  'repeater_on.png': 'repeater_on.png',
  'noteblock.png': 'note_block.png',
  'jukebox_top.png': 'jukebox_top.png',
  'jukebox_side.png': 'jukebox_side.png',
  'tnt_side.png': 'tnt_side.png',
  'tnt_top.png': 'tnt_top.png',
  'tnt_bottom.png': 'tnt_bottom.png',
  'cake_side.png': 'cake_side.png',
  'cake_top.png': 'cake_top.png',
  'cake_bottom.png': 'cake_bottom.png',
  'cake_inner.png': 'cake_inner.png',
  'command_block.png': 'command_block_front.png',
  'bookshelf.png': 'bookshelf.png',
  'nether_brick.png': 'nether_bricks.png',
  'quartz_ore.png': 'nether_quartz_ore.png',
  'mob_spawner.png': 'spawner.png',
  'enchanting_table_top.png': 'enchanting_table_top.png',
  'enchanting_table_side.png': 'enchanting_table_side.png',
  'enchanting_table_bottom.png': 'enchanting_table_bottom.png',
  'anvil_base.png': 'anvil.png',
  'anvil_top_damaged_0.png': 'anvil.png',
  'anvil_top_damaged_1.png': 'chipped_anvil_top.png',
  'anvil_top_damaged_2.png': 'damaged_anvil_top.png',
  'melon_side.png': 'melon_side.png',
  'melon_top.png': 'melon_top.png',
  'pumpkin_side.png': 'pumpkin_side.png',
  'pumpkin_top.png': 'pumpkin_top.png',
  'pumpkin_face_off.png': 'carved_pumpkin.png',
  'pumpkin_face_on.png': 'jack_o_lantern.png',
  'brewing_stand_base.png': 'brewing_stand_base.png',
  'brewing_stand.png': 'brewing_stand.png',
  'cauldron_side.png': 'cauldron_side.png',
  'cauldron_top.png': 'cauldron_top.png',
  'cauldron_bottom.png': 'cauldron_bottom.png',
  'cauldron_inner.png': 'cauldron_inner.png',
  'beacon.png': 'beacon.png',
  'hopper_inside.png': 'hopper_inside.png',
  'hopper_outside.png': 'hopper_outside.png',
  'hopper_top.png': 'hopper_top.png',
  'dragon_egg.png': 'dragon_egg.png',
  'endframe_top.png': 'end_portal_frame_top.png',
  'endframe_side.png': 'end_portal_frame_side.png',
  'endframe_eye.png': 'end_portal_frame_eye.png',
  'end_stone.png': 'end_stone.png',
  'end_bricks.png': 'end_stone_bricks.png'
}

// 标牌变体 HSV 参数
export const SIGN_VARIANTS = {
  'oak': { hue: 0, brightness: 15 },
  'birch': { hue: 0, brightness: 40 },
  'acacia': { hue: -23, brightness: 10 },
  'dark_oak': { hue: 0, brightness: -15 },
  'jungle': { hue: -10, brightness: 4.6 },
  'mangrove': { hue: -59, brightness: -10 },
  'pale_oak': { hue: 0, brightness: 30, saturation: -100 }
}

// 船变体 HSV 参数
export const BOAT_VARIANTS = {
  'oak': { hue: 0, brightness: 15 },
  'birch': { hue: 0, brightness: 40 },
  'acacia': { hue: -23, brightness: 10 },
  'dark_oak': { hue: 0, brightness: -15 },
  'jungle': { hue: -10, brightness: 4.6 }
  // spruce: 直接从 boat.png 重命名而来
}

// 木板变体 HSV 参数 (generate_redwood_cherry_bamboo_planks)
export const PLANKS_VARIANTS = {
  'mangrove': { hue: -59, brightness: -15 },
  'cherry': { hue: -80, brightness: 40 },
  'bamboo': { hue: 25, brightness: 20 }
}

// 下界合金 HSV 参数
export const NETHERITE_HSV = {
  hue: 0.861,
  satDivisor: 3,
  valDivisor: 3
}

// 下界合金工具映射
export const NETHERITE_TOOL_PAIRS = {
  'diamond_sword.png': 'netherite_sword.png',
  'diamond_pickaxe.png': 'netherite_pickaxe.png',
  'diamond_axe.png': 'netherite_axe.png',
  'diamond_shovel.png': 'netherite_shovel.png',
  'diamond_hoe.png': 'netherite_hoe.png'
}

// 下界合金盔甲层映射
export const NETHERITE_ARMOR_LAYER_PAIRS = {
  'diamond_layer_1.png': 'netherite_layer_1.png',
  'diamond_layer_2.png': 'netherite_layer_2.png'
}

// 盔甲模型映射 (fix_armor_models, format 42→46)
export const ARMOR_MODEL_LAYER1_MAP = {
  'chainmail_layer_1.png': 'chainmail.png',
  'diamond_layer_1.png': 'diamond.png',
  'iron_layer_1.png': 'iron.png',
  'gold_layer_1.png': 'gold.png',
  'leather_layer_1.png': 'leather.png',
  'leather_layer_1_overlay.png': 'leather_overlay.png',
  'netherite_layer_1.png': 'netherite.png'
}

export const ARMOR_MODEL_LAYER2_MAP = {
  'chainmail_layer_2.png': 'chainmail.png',
  'diamond_layer_2.png': 'diamond.png',
  'iron_layer_2.png': 'iron.png',
  'gold_layer_2.png': 'gold.png',
  'leather_layer_2.png': 'leather.png',
  'leather_layer_2_overlay.png': 'leather_overlay.png',
  'netherite_layer_2.png': 'netherite.png'
}

// 反向盔甲模型映射 (reverse_fix_armor_models, format 46→42)
// 注意：不包含 netherite
export const REVERSE_ARMOR_MODEL_LAYER1_MAP = {
  'chainmail.png': 'chainmail_layer_1.png',
  'diamond.png': 'diamond_layer_1.png',
  'iron.png': 'iron_layer_1.png',
  'gold.png': 'gold_layer_1.png',
  'leather.png': 'leather_layer_1.png',
  'leather_overlay.png': 'leather_layer_1_overlay.png'
}

export const REVERSE_ARMOR_MODEL_LAYER2_MAP = {
  'chainmail.png': 'chainmail_layer_2.png',
  'diamond.png': 'diamond_layer_2.png',
  'iron.png': 'iron_layer_2.png',
  'gold.png': 'gold_layer_2.png',
  'leather.png': 'leather_layer_2.png',
  'leather_overlay.png': 'leather_layer_2_overlay.png'
}

// 马UI 插槽文件映射 (fix2_horse_ui)
export const HORSE_SLOT_FILES = {
  'armor_slot.png': 'armor_slot.png',
  'saddle_slot.png': 'saddle_slot.png',
  'llama_armor_slot.png': 'llama_armor_slot.png',
  'chest_slots.png': 'chest_slots.png'
}

// 鱼桶变体
export const FISH_BUCKET_VARIANTS = [
  'axolotl_bucket',
  'cod_bucket',
  'pufferfish_bucket',
  'salmon_bucket',
  'tropical_fish_bucket',
  'tadpole_bucket'
]

// 弩变体 (从弓的变体生成)
export const CROSSBOW_FROM_BOW = {
  'bow_pulling_0.png': 'crossbow_pulling_0.png',
  'bow_pulling_1.png': 'crossbow_pulling_1.png',
  'bow_pulling_2.png': 'crossbow_pulling_2.png'
}

// mob effect 图像网格 (fix_ui_survival)
export const MOB_EFFECT_IMAGES = [
  ['speed.png', 'slowness.png', 'haste.png', 'mining_fatigue.png', 'strength.png', 'weakness.png', 'poison.png', 'regeneration.png'],
  ['invisibility.png', 'hunger.png', 'jump_boost.png', 'nausea.png', 'night_vision.png', 'blindness.png', 'resistance.png', 'fire_resistance.png'],
  ['water_breathing.png', 'wither.png', 'absorption.png']
]

// 粒子图片拆分映射
export const PARTICLE_SPLIT_MAP = {
  'generic_0.png': [0, 0],
  'generic_1.png': [0, 1],
  'generic_2.png': [0, 2],
  'generic_3.png': [0, 3],
  'generic_4.png': [0, 4],
  'generic_5.png': [0, 5],
  'generic_6.png': [0, 6],
  'generic_7.png': [0, 7],
  'splash_0.png': [1, 3],
  'splash_1.png': [1, 4],
  'splash_2.png': [1, 5],
  'splash_3.png': [1, 6],
  'bubble.png': [2, 0],
  'fishing_hook.png': [2, 1],
  'flame.png': [3, 0],
  'lava.png': [3, 1],
  'note.png': [4, 0],
  'critical_hit.png': [4, 1],
  'enchanted_hit.png': [4, 2],
  'heart.png': [5, 0],
  'angry.png': [5, 1],
  'glint.png': [5, 2],
  'drip_hang.png': [7, 0],
  'drip_fall.png': [7, 1],
  'drip_land.png': [7, 2],
  'effect_0.png': [8, 0],
  'effect_1.png': [8, 1],
  'effect_2.png': [8, 2],
  'effect_3.png': [8, 3],
  'effect_4.png': [8, 4],
  'effect_5.png': [8, 5],
  'effect_6.png': [8, 6],
  'effect_7.png': [8, 7],
  'spell_0.png': [9, 0],
  'spell_1.png': [9, 1],
  'spell_2.png': [9, 2],
  'spell_3.png': [9, 3],
  'spell_4.png': [9, 4],
  'spell_5.png': [9, 5],
  'spell_6.png': [9, 6],
  'spell_7.png': [9, 7],
  'spark_0.png': [10, 0],
  'spark_1.png': [10, 1],
  'spark_2.png': [10, 2],
  'spark_3.png': [10, 3],
  'spark_4.png': [10, 4],
  'spark_5.png': [10, 5],
  'spark_6.png': [10, 6],
  'spark_7.png': [10, 7]
}

// 粒子反向合并映射 (reverse_fix_particles) — 位置格式: [row, col]
export const PARTICLE_MERGE_MAP = {
  'generic_0.png': [0, 0],
  'generic_1.png': [0, 1],
  'generic_2.png': [0, 2],
  'generic_3.png': [0, 3],
  'generic_4.png': [0, 4],
  'generic_5.png': [0, 5],
  'generic_6.png': [0, 6],
  'generic_7.png': [1, 5],
  'splash_0.png': [1, 4],
  'splash_1.png': [1, 6],
  'splash_2.png': [1, 5],
  'splash_3.png': [1, 6],
  'bubble.png': [4, 1],
  'fishing_hook.png': [4, 2],
  'flame.png': [4, 5],
  'lava.png': [4, 6],
  'note.png': [4, 5],
  'critical_hit.png': [4, 1],
  'enchanted_hit.png': [4, 2],
  'heart.png': [5, 0],
  'angry.png': [5, 1],
  'glint.png': [5, 2],
  'drip_hang.png': [7, 0],
  'drip_fall.png': [7, 1],
  'drip_land.png': [7, 2],
  'effect_0.png': [8, 0],
  'effect_1.png': [8, 1],
  'effect_2.png': [8, 2],
  'effect_3.png': [8, 3],
  'effect_4.png': [8, 4],
  'effect_5.png': [8, 5],
  'effect_6.png': [8, 6],
  'effect_7.png': [8, 7],
  'spell_0.png': [9, 0],
  'spell_1.png': [9, 1],
  'spell_2.png': [9, 2],
  'spell_3.png': [9, 3],
  'spell_4.png': [9, 4],
  'spell_5.png': [9, 5],
  'spell_6.png': [9, 6],
  'spell_7.png': [9, 7],
  'spark_0.png': [10, 0],
  'spark_1.png': [10, 1],
  'spark_2.png': [10, 2],
  'spark_3.png': [10, 3],
  'spark_4.png': [10, 4],
  'spark_5.png': [10, 5],
  'spark_6.png': [10, 6],
  'spark_7.png': [10, 7]
}

// ========== GUI 精灵图切割定义 ==========

// process1_* 容器裁剪区域 (format 15→18, cut_gui)
export const CONTAINER_CROP_REGIONS = {
  anvil: {
    source: 'gui/container/anvil.png',
    targetDir: 'gui/sprites/container/anvil',
    baseSize: [256, 256],
    regions: [
      { crop: [176, 0, 204, 21], saveName: 'error.png' },
      { crop: [0, 166, 110, 182], saveName: 'text_field.png' },
      { crop: [0, 182, 110, 198], saveName: 'text_field_disabled.png' }
    ]
  },
  beacon: {
    source: 'gui/container/beacon.png',
    targetDir: 'gui/sprites/container/beacon',
    baseSize: [256, 256],
    regions: [
      { crop: [0, 219, 22, 241], saveName: 'button.png' },
      { crop: [22, 219, 44, 241], saveName: 'button_selected.png' },
      { crop: [44, 219, 66, 241], saveName: 'button_disabled.png' },
      { crop: [66, 219, 88, 241], saveName: 'button_highlighted.png' },
      { crop: [90, 220, 108, 238], saveName: 'confirm.png' },
      { crop: [112, 220, 130, 238], saveName: 'cancel.png' }
    ]
  },
  blast_furnace: {
    source: 'gui/container/blast_furnace.png',
    targetDir: 'gui/sprites/container/blast_furnace',
    baseSize: [256, 256],
    regions: [
      { crop: [176, 0, 190, 14], saveName: 'lit_progress.png' },
      { crop: [176, 14, 200, 31], saveName: 'burn_progress.png' }
    ]
  },
  brewing_stand: {
    source: 'gui/container/brewing_stand.png',
    targetDir: 'gui/sprites/container/brewing_stand',
    baseSize: [256, 256],
    regions: [
      { crop: [176, 0, 185, 28], saveName: 'brew_progress.png' },
      { crop: [185, 14, 197, 29], saveName: 'bubbles.png' },
      { crop: [176, 29, 194, 33], saveName: 'fuel_length.png' }
    ]
  },
  cartography_table: {
    source: 'gui/container/cartography_table.png',
    targetDir: 'gui/sprites/container/cartography_table',
    baseSize: [256, 256],
    regions: [
      { crop: [176, 132, 226, 198], saveName: 'duplicated_map.png' },
      { crop: [176, 66, 242, 132], saveName: 'scaled_map.png' },
      { crop: [176, 0, 242, 66], saveName: 'map.png' },
      { crop: [52, 214, 62, 228], saveName: 'locked.png' },
      { crop: [226, 132, 254, 153], saveName: 'error.png' }
    ]
  },
  enchanting_table: {
    source: 'gui/container/enchanting_table.png',
    targetDir: 'gui/sprites/container/enchanting_table',
    baseSize: [256, 256],
    regions: [
      { crop: [0, 166, 108, 185], saveName: 'enchantment_slot.png' },
      { crop: [0, 185, 108, 204], saveName: 'enchantment_slot_disabled.png' },
      { crop: [0, 204, 108, 223], saveName: 'enchantment_slot_highlighted.png' },
      { crop: [0, 223, 16, 239], saveName: 'level_1.png' },
      { crop: [16, 223, 32, 239], saveName: 'level_2.png' },
      { crop: [32, 223, 48, 239], saveName: 'level_3.png' },
      { crop: [0, 239, 16, 255], saveName: 'level_1_disabled.png' },
      { crop: [16, 239, 32, 255], saveName: 'level_2_disabled.png' },
      { crop: [32, 239, 48, 255], saveName: 'level_3_disabled.png' }
    ]
  },
  furnace: {
    source: 'gui/container/furnace.png',
    targetDir: 'gui/sprites/container/furnace',
    baseSize: [256, 256],
    regions: [
      { crop: [176, 0, 190, 14], saveName: 'lit_progress.png' },
      { crop: [176, 14, 200, 31], saveName: 'burn_progress.png' }
    ]
  },
  smoker: {
    source: 'gui/container/smoker.png',
    targetDir: 'gui/sprites/container/smoker',
    baseSize: [256, 256],
    regions: [
      { crop: [176, 0, 190, 14], saveName: 'lit_progress.png' },
      { crop: [176, 14, 200, 31], saveName: 'burn_progress.png' }
    ]
  },
  grindstone: {
    source: 'gui/container/grindstone.png',
    targetDir: 'gui/sprites/container/grindstone',
    baseSize: [256, 256],
    regions: [
      { crop: [176, 0, 204, 21], saveName: 'error.png' }
    ]
  },
  horse: {
    source: 'gui/container/horse.png',
    targetDir: 'gui/sprites/container/horse',
    baseSize: [256, 256],
    regions: [
      { crop: [0, 220, 18, 238], saveName: 'armor_slot.png' },
      { crop: [18, 220, 36, 238], saveName: 'saddle_slot.png' },
      { crop: [36, 220, 54, 238], saveName: 'llama_armor_slot.png' },
      { crop: [0, 166, 90, 220], saveName: 'chest_slots.png' }
    ]
  },
  inventory: {
    source: 'gui/container/inventory.png',
    targetDir: 'gui/sprites/container/inventory',
    baseSize: [256, 256],
    regions: [
      { crop: [0, 166, 120, 198], saveName: 'effect_background_large.png' },
      { crop: [0, 198, 32, 230], saveName: 'effect_background_small.png' }
    ]
  },
  smithing: {
    source: 'gui/container/smithing.png',
    targetDir: 'gui/sprites/container/smithing',
    baseSize: [256, 256],
    regions: [
      { crop: [176, 0, 204, 21], saveName: 'error.png' }
    ]
  },
  loom: {
    source: 'gui/container/loom.png',
    targetDir: 'gui/sprites/container/loom',
    baseSize: [256, 256],
    regions: [
      { crop: [176, 0, 192, 16], saveName: 'banner_slot.png' },
      { crop: [192, 0, 208, 16], saveName: 'dye_slot.png' },
      { crop: [208, 0, 224, 16], saveName: 'pattern_slot.png' },
      { crop: [0, 166, 14, 180], saveName: 'pattern.png' },
      { crop: [0, 180, 14, 194], saveName: 'pattern_selected.png' },
      { crop: [0, 194, 14, 208], saveName: 'pattern_highlighted.png' },
      { crop: [232, 0, 244, 15], saveName: 'scroller.png' },
      { crop: [244, 0, 256, 15], saveName: 'scroller_disabled.png' }
    ]
  },
  stonecutter: {
    source: 'gui/container/stonecutter.png',
    targetDir: 'gui/sprites/container/stonecutter',
    baseSize: [256, 256],
    regions: [
      { crop: [0, 166, 16, 184], saveName: 'recipe.png' },
      { crop: [0, 184, 16, 202], saveName: 'recipe_selected.png' },
      { crop: [0, 202, 16, 220], saveName: 'recipe_highlighted.png' },
      { crop: [176, 0, 188, 15], saveName: 'scroller.png' },
      { crop: [188, 0, 200, 15], saveName: 'scroller_disabled.png' }
    ]
  },
  villager2: {
    source: 'gui/container/villager2.png',
    targetDir: 'gui/sprites/container/villager',
    baseSize: [512, 256],
    regions: [
      { crop: [0, 176, 9, 178], saveName: 'discount_strikethrough.png' },
      { crop: [0, 181, 102, 186], saveName: 'experience_bar_result.png' },
      { crop: [0, 186, 102, 191], saveName: 'experience_bar_background.png' },
      { crop: [0, 191, 102, 196], saveName: 'experience_bar_current.png' },
      { crop: [15, 171, 25, 180], saveName: 'trade_arrow.png' },
      { crop: [25, 171, 35, 180], saveName: 'out_of_stock.png' },
      { crop: [0, 199, 6, 226], saveName: 'scroller.png' },
      { crop: [6, 199, 12, 226], saveName: 'scroller_disabled.png' }
    ]
  }
}

// process_* 大型 GUI 精灵图切割定义 (process_icons_in_dir 等)
// 每个定义包含: source, targetDir, actions (crop_and_split)
export const GUI_SPRITE_ACTIONS = {
  icons: {
    source: 'gui/icons.png',
    targetDir: 'gui/sprites',
    baseSize: [256, 256],
    actions: [
      { type: 'crop_and_split', crop: [0, 0, 182, 5], splitW: 91, splitH: 5, names: ['hud/heart/container.png', 'hud/heart/container_blinking.png'] },
      { type: 'crop_and_split', crop: [0, 5, 9, 14], splitW: 9, splitH: 9, names: ['hud/heart/full.png'] },
      { type: 'crop_and_split', crop: [9, 5, 18, 14], splitW: 9, splitH: 9, names: ['hud/heart/half.png'] },
      { type: 'crop_and_split', crop: [18, 5, 27, 14], splitW: 9, splitH: 9, names: ['hud/heart/absorbing_full.png'] },
      { type: 'crop_and_split', crop: [27, 5, 36, 14], splitW: 9, splitH: 9, names: ['hud/heart/absorbing_half.png'] }
      // 更多的精灵图裁剪操作在运行时根据实际需要处理
    ]
  },
  widgets: {
    source: 'gui/widgets.png',
    targetDir: 'gui/sprites',
    baseSize: [256, 256],
    actions: []
  },
  tabs: {
    source: 'gui/container/creative_inventory/tabs.png',
    targetDir: 'gui/sprites',
    baseSize: [256, 256],
    actions: []
  }
}

// 箱子处理相关常量
export const SINGLE_CHEST_FILES = ['ender.png', 'normal.png', 'trapped.png', 'christmas.png']
export const DOUBLE_CHEST_PREFIXES = ['normal', 'trapped', 'christmas']

// 单箱子 swap_and_mirror 区域 (对 64x64 基础尺寸)
export const SINGLE_CHEST_SWAP_BOXES = [
  [[14, 0, 28, 14], [28, 0, 42, 14]],
  [[14, 14, 28, 19], [42, 14, 56, 19]],
  [[14, 19, 28, 33], [28, 19, 42, 33]],
  [[14, 33, 28, 43], [42, 33, 56, 43]]
]

// 单箱子 flip 区域
export const SINGLE_CHEST_FLIP_BOXES = [
  [14, 0, 28, 14],
  [28, 0, 42, 14],
  [0, 14, 14, 19],
  [28, 14, 42, 19],
  [14, 19, 28, 33],
  [28, 19, 42, 33],
  [0, 33, 14, 43],
  [28, 33, 42, 43]
]

// fix_ui_survival 操作参数
export const SURVIVAL_UI_OPERATIONS = {
  moveRegion: { src: [86, 24, 162, 62], shift: [10, -8] },
  colorFill1: { box: [75, 6, 96, 80], colorSample: [90, 10] },
  colorFill2: { box: [96, 54, 161, 62], colorSample: [90, 10] },
  copyPaste: { src: [152, 26, 172, 46], dst: [75, 60] }
}

// reverse_fix_brewing_stand_ui 参数
export const REVERSE_BREWING_STAND_PARAMS = {
  colorSample: [7, 4],
  fillBox1: [41, 43, 79, 49],
  fillBox2: [14, 14, 55, 43],
  moveBox: [55, 50, 119, 75],
  moveDst: [55, 45],
  fillBox3: [55, 70, 119, 75]
}
