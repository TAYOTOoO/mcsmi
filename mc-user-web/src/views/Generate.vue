<template>
  <div class="page-wrapper">
    <div class="top-border"></div>
    <div class="content-body">
      <div class="side-border left"></div>
      <main>
        <!-- 页头 -->
        <header class="page-header">
          <div class="mc-box logo">材质工坊</div>
          <nav class="main-nav">
            <ul>
              <li><router-link to="/" class="mc-box">首页</router-link></li>
              <li><router-link to="/generate" class="mc-box active">开始生成</router-link></li>
              <li><router-link to="/my-tasks" class="mc-box">我的任务</router-link></li>
              <li><router-link to="/texture-pack-editor" class="mc-box">材质编辑</router-link></li>
              <li><router-link to="/recharge" class="mc-box">积分充值</router-link></li>
            </ul>
          </nav>
          
          <div class="user-actions">
            <template v-if="username">
              <span class="mc-box username-display" @click="goToProfile" title="个人中心">
                {{ username }}
                <span v-if="isDiamond" class="diamond-icon">💎</span>
              </span>
              <button class="mc-box logout-btn" @click="handleLogout">退出</button>
            </template>
            <template v-else>
              <button class="mc-box login-btn" @click="goToLogin">登录</button>
            </template>
          </div>
        </header>

        <!-- 主要内容区域：生成表单 -->
        <section class="generate-section">
          <div class="section-header mc-box">
            <h2>AI材质包生成</h2>
          </div>

          <div class="generate-form-container mc-box">
            <!-- 风格描述 -->
            <div class="form-group">
              <div class="form-label-row">
                <label class="form-label">
                  <img src="/assets/images/new-textures/default_sapling.png" alt="" class="label-icon" />
                  风格描述
                </label>
                <button class="mc-box btn-random-style" @click="randomStyle" type="button">
                  <img src="/assets/images/new-textures/default_mese_crystal_fragment.png" alt="" />
                  随机灵感
                </button>
              </div>
              <textarea
                v-model="formData.styleDescription"
                class="mc-textarea"
                rows="6"
                placeholder="请详细描述你想要的材质包风格，例如：&#10;&#10;• 赛博朋克风格，霓虹灯效果，带有科技感的线条&#10;• 中世纪奇幻，魔法元素，古老的纹理&#10;• 可爱卡通，明亮的色彩，圆润的外观"
                maxlength="500"
              />
              <div class="char-count">{{ formData.styleDescription.length }} / 500</div>
            </div>

            <!-- 生成类型 -->
            <div class="form-group">
              <label class="form-label">
                <img src="/assets/images/new-textures/default_diamond.png" alt="" class="label-icon" />
                生成内容
              </label>
              <div class="generation-type-options">
                <label class="mc-box type-option" :class="{ active: formData.generationType === 'item' }">
                  <input type="radio" v-model="formData.generationType" value="item" />
                  <span class="option-content">
                    <span class="option-title">仅物品材质</span>
                    <span class="option-desc">生成50个常用物品 - 100积分</span>
                  </span>
                </label>
                <label class="mc-box type-option" :class="{ active: formData.generationType === 'ui' }">
                  <input type="radio" v-model="formData.generationType" value="ui" />
                  <span class="option-content">
                    <span class="option-title">仅UI材质</span>
                    <span class="option-desc">生成游戏界面(widgets.png) - 200积分</span>
                  </span>
                </label>
              </div>
            </div>

            <!-- 材质模板 -->
            <div class="form-group">
              <label class="form-label">
                <img src="/assets/images/new-textures/flowers_tulip.png" alt="" class="label-icon" />
                材质模板
              </label>
              <select v-model="formData.templateId" class="mc-select">
                <option v-for="tpl in templateList" :key="tpl.templateId" :value="tpl.templateId">{{ tpl.templateName }}</option>
              </select>
            </div>

            <!-- 生成按钮 -->
            <div class="form-actions">
              <button class="mc-box btn-generate" :disabled="generating" @click="handleSubmit">
                <img v-if="!generating" src="/assets/images/new-textures/default_mese_crystal.png" alt="" />
                <img v-else src="/assets/images/new-textures/default_apple.png" alt="" />
                {{ generating ? '生成中，请稍候...' : '开始生成材质包' }}
              </button>
              <div class="form-tip">生成过程需要1-2分钟，请耐心等待</div>
            </div>
          </div>
        </section>
      </main>
      <div class="side-border right"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addGeneration, getActiveTemplates } from '@/api/generation'
import { logout } from '@/api/auth'
import { useSubscription } from '@/composables/useSubscription'

const router = useRouter()
const generating = ref(false)
const username = ref('')
const templateList = ref([])

// 订阅信息
const { isDiamond } = useSubscription()

// 加载模板列表（根据材质类型）
const loadTemplates = async () => {
  try {
    // 根据生成类型确定材质类型：item=1(物品), ui=2(UI)
    const materialType = formData.generationType === 'item' ? 1 : 2
    const res = await getActiveTemplates({ materialType })
    templateList.value = res.data || []
    // 默认选择第一个模板
    if (templateList.value.length > 0) {
      formData.templateId = templateList.value[0].templateId
    } else {
      formData.templateId = null
    }
  } catch (e) {
    console.error('加载模板列表失败', e)
  }
}

onMounted(async () => {
  username.value = localStorage.getItem('username') || ''
  await loadTemplates()
})

const formData = reactive({
  styleDescription: '',
  versionId: 1,
  templateId: null,
  generationType: 'item'
})

// 监听生成类型变化，重新加载对应的模板
watch(() => formData.generationType, () => {
  loadTemplates()
})

const examples = [
  // 历史与神话 (30个)
  { id: 1, tag: '北欧神话', description: '北欧神话与维京符文风格，粗犷的金属、符文雕刻、野兽毛皮，带有雷神之锤和奥丁之眼图案' },
  { id: 2, tag: '希腊神话', description: '希腊神话与奥林匹斯圣器，大理石纹理、黄金装饰、闪电与月桂叶图案，神殿柱式雕刻' },
  { id: 3, tag: '埃及法老', description: '埃及法老的诅咒风格，圣甲虫、象形文字、黄金与黑曜石，金字塔与木乃伊元素' },
  { id: 4, tag: '罗马军团', description: '罗马军团的荣耀，青铜盔甲、铁质武器、皮革装备与鹰旗标志，罗马方阵美学' },
  { id: 5, tag: '封建日本', description: '封建日本武士与鬼怪，武士刀、漆器工艺、般若面具，樱花与竹林背景元素' },
  { id: 6, tag: '阿兹特克', description: '阿兹特克太阳石与丛林之神，绿松石、羽蛇神图腾、黑曜石锯齿，玛雅金字塔纹样' },
  { id: 7, tag: '亚瑟王骑士', description: '中世纪亚瑟王骑士，锁子甲纹理、纹章图案、圣剑光芒与圆桌会议徽记' },
  { id: 8, tag: '凯尔特德鲁伊', description: '凯尔特德鲁伊与神秘绳结，古木纹理、藤蔓缠绕、三叶结与月光石光泽' },
  { id: 9, tag: '波斯帝国', description: '波斯帝国的华丽奢靡，繁复地毯图案、鎏金装饰、孔雀羽毛与宝石镶嵌' },
  { id: 10, tag: '狂野西部', description: '狂野西部歹徒与淘金热，左轮手枪、牛仔皮革、炸药桶与铁路枕木' },
  { id: 11, tag: '海盗时代', description: '海盗时代与沉没宝藏，船锚、骷髅旗、朗姆酒瓶、海图与宝箱' },
  { id: 12, tag: '斯拉夫民间', description: '斯拉夫民间传说与雅加婆婆，白桦林、民间刺绣、巫术符号与套娃图案' },
  { id: 13, tag: '印度神话', description: '印度神话与天神兵器，莲花纹饰、曼陀罗、孔雀王朝的奢华装饰' },
  { id: 14, tag: '忍者暗影', description: '忍者与忍术的暗影艺术，手里剑、锁镰、烟雾弹、竹林与月夜' },
  { id: 15, tag: '蒙古部落', description: '蒙古部落的征服之路，复合弓、皮革盔甲、草原雄鹰与蒙古包' },
  { id: 16, tag: '史前洞穴', description: '史前洞穴居民，兽骨工具、燧石、粗糙的壁画与原始部落符号' },
  { id: 17, tag: '维多利亚', description: '维多利亚时代与工业革命，齿轮机械、黄铜装饰、礼帽与蒸汽机' },
  { id: 18, tag: '咆哮二十年代', description: '咆哮的二十年代与装饰艺术，黑金配色、几何线条、爵士时代元素' },
  { id: 19, tag: '斯巴达勇士', description: '斯巴达勇士的方阵，标志性的头盔、科林斯式青铜盔甲与盾牌' },
  { id: 20, tag: '冷战间谍', description: '冷战间谍与特工科技，消音器、微型胶卷、复古高科技设备' },
  { id: 21, tag: '十字军骑士', description: '十字军骑士的圣战，白袍红十字、链枷武器、圣物与圣杯' },
  { id: 22, tag: '中美洲邪教', description: '中美洲黑曜石邪教，血祭匕首、羽毛头饰、金字塔浮雕与祭坛' },
  { id: 23, tag: '祖鲁王国', description: '祖鲁王国的部落战争，兽皮盾、短矛、部落彩绘与图腾' },
  { id: 24, tag: '奥斯曼帝国', description: '奥斯曼帝国的禁卫军，新月弯刀、华丽的头巾、伊斯兰纹饰' },
  { id: 25, tag: '波利尼西亚', description: '波利尼西亚航海家与提基神，鲨鱼牙、火山岩、部落纹身图案' },
  { id: 26, tag: '角斗场', description: '角斗场冠军，网斗士的三叉戟、带刺的拳套、罗马竞技场' },
  { id: 27, tag: '古爱尔兰', description: '古爱尔兰神话，库丘林的传说、妖精光芒与魔法森林' },
  { id: 28, tag: '伏都教', description: '伏都教与河湾巫术，巫毒娃娃、头骨装饰、沼泽苔藓与神秘咒符' },
  { id: 29, tag: '阿拉伯之夜', description: '一千零一夜与灯神魔法，神灯、弯刀、沙漠与绿洲的对比' },
  { id: 30, tag: '大航海时代', description: '大航海时代的探险家，六分仪、古地图、火绳枪与帆船' },

  // 科幻与未来 (30个)
  { id: 31, tag: '赛博朋克', description: '赛博朋克霓虹反乌托邦，霓虹灯管、义体改造、高周波刀刃，紫蓝色调' },
  { id: 32, tag: '末日废土', description: '末日后废土拾荒者，辐射标志、管道武器、拼凑的装甲与锈蚀金属' },
  { id: 33, tag: '太空歌剧', description: '太空歌剧之银河帝国，能量剑、激光枪、白色士兵装甲与星舰' },
  { id: 34, tag: 'NASA朋克', description: '硬核科幻与NASA朋克，航天服、探测器、写实风格的科技装备' },
  { id: 35, tag: '异形入侵', description: '异形生物入侵，生物力学、酸性血液、骨骼外壳与异形巢穴' },
  { id: 36, tag: '机甲驾驶', description: '机甲驾驶员与巨型机器人，高达风格、驾驶舱、能量核心发光' },
  { id: 37, tag: '蒸汽朋克', description: '蒸汽朋克空贼，黄铜齿轮、蒸汽背包、飞行护目镜与维多利亚美学' },
  { id: 38, tag: '创战纪', description: '虚拟现实与创战纪，发光线条、数据流、数字化武器与霓虹网格' },
  { id: 39, tag: '生物机械', description: '生物机械恐怖，血肉与金属的扭曲融合，有机材质与电路交织' },
  { id: 40, tag: '乌托邦未来', description: '乌托邦未来的极简主义，纯白色调、流线型、能量护盾与清洁科技' },
  { id: 41, tag: '星河战队', description: '星河战队式军旅，动力装甲、脉冲步枪、虫族战争美学' },
  { id: 42, tag: '复古未来', description: '50年代的复古未来主义，原子能、飞碟、射线枪与乌托邦想象' },
  { id: 43, tag: '纳米科技', description: '纳米技术蜂群，可变形武器、液态金属、微型机器人群' },
  { id: 44, tag: '数据损坏', description: '故障与数据损坏美学，像素错位、代码乱码、数字噪点与赛博错误' },
  { id: 45, tag: '发条自动人偶', description: '发条自动人偶，精密齿轮、弹簧机关、机械心脏与铜制外壳' },
  { id: 46, tag: '星际之门', description: '星际之门与远古外星人，能量传送门、外星象形文字与水晶科技' },
  { id: 47, tag: '沙丘行星', description: '沙丘的沙漠行星，蒸馏服、沙虫纹理、香料发光与弗雷曼文化' },
  { id: 48, tag: '疯狂麦克斯', description: '疯狂的麦克斯：狂暴之路，V8引擎、战争机器、废土宗教图腾' },
  { id: 49, tag: '虚空实体', description: '虚空宇宙实体，反物质、黑洞扭曲、宇宙恐怖与空间撕裂' },
  { id: 50, tag: '合成波', description: '合成波与复古电驰，80年代跑车、日落网格、棕榈树剪影' },
  { id: 51, tag: '光圈科技', description: '光圈科技风格实验室，传送门枪、白色面板、AI核心与测试室' },
  { id: 52, tag: '柴油朋克', description: '柴油朋克世界大战，粗糙的焊接、柴油机、步行坦克与工业美学' },
  { id: 53, tag: '赏金猎人', description: '银河赏金猎人，曼达洛人风格、多种武器小工具、喷气背包' },
  { id: 54, tag: '时间旅行', description: '时间旅行者工具包，悖论装置、来自不同时代的物品混合' },
  { id: 55, tag: '火星殖民', description: '火星殖民者装备，橙色土壤、殖民地标志、地形改造工具' },
  { id: 56, tag: '仿生人叛乱', description: '仿生人叛乱，蓝色血液、外露的电路、未来派武器与电子眼' },
  { id: 57, tag: '赛博忍者', description: '赛博忍者部落，能量武士刀、光学迷彩、高科技手里剑' },
  { id: 58, tag: '银河守护者', description: '群星的银河守护者，巨型结构、联邦舰队美学、星云背景' },
  { id: 59, tag: '深空矿工', description: '深空矿工，等离子切割机、外骨骼、小行星矿物与太空站' },
  { id: 60, tag: '后人类', description: '后人类生物工程生命，基因改造、可控进化、合成生命体' },

  // 奇幻与魔法 (30个)
  { id: 61, tag: '高等精灵', description: '高等精灵的优雅，流线型设计、秘银材质、月亮与星辰符号' },
  { id: 62, tag: '矮人符文', description: '矮人符文与锻炉，厚重几何、黄金符文、熔炉火光与铁砧' },
  { id: 63, tag: '兽人军阀', description: '兽人军阀的残暴，尖刺装甲、战旗图腾、兽骨与粗铁武器' },
  { id: 64, tag: '死灵法师', description: '死灵法师的亡灵军团，骨骼堆砌、灵魂火焰、缝合的血肉' },
  { id: 65, tag: '奥术法师', description: '奥术法师的宏伟图书馆，卷轴、法杖、浮动水晶与魔法阵' },
  { id: 66, tag: '黑暗之魂', description: '黑暗之魂的灰烬王国，褪色材质、破碎盔甲、沉重骑士风格' },
  { id: 67, tag: '血源诅咒', description: '血源诅咒的哥特恐怖，锯肉刀、猎人套装、古神触手与维多利亚哥特' },
  { id: 68, tag: '元素魔法', description: '元素魔法掌控者，火焰燃烧、冰霜结晶、风暴雷电与岩石纹理' },
  { id: 69, tag: '童话森林', description: '童话与魔法森林，糖果屋、巨大蘑菇、精灵之光与魔法生物' },
  { id: 70, tag: '天使与恶魔', description: '天使与恶魔的军备，光羽圣洁金属 vs 炼狱之火黑曜石对比' },
  { id: 71, tag: '炼金术士', description: '炼金术士的实验室，烧瓶、蒸馏器、贤者之石与化学符号' },
  { id: 72, tag: '龙骑士', description: '龙骑士的王权，龙鳞纹理、龙牙装饰、燃烧的徽记与龙息' },
  { id: 73, tag: '黑暗精灵', description: '黑暗精灵的幽暗地域，蛛网纹理、紫色与黑色、细剑与毒素' },
  { id: 74, tag: '天空王国', description: '天空王国与浮空岛，风元素、羽毛装饰、轻盈的水晶与云朵' },
  { id: 75, tag: '亚特兰蒂斯', description: '亚特兰蒂斯的沉没之城，珊瑚、水流纹理、三叉戟与海洋生物' },
  { id: 76, tag: '真菌王国', description: '蕈人的真菌王国，发光蘑菇、孢子云、菌丝网络与腐朽木' },
  { id: 77, tag: '鲜血魔法', description: '鲜血魔法教徒，血液符文、献祭匕首、深红色调与血池' },
  { id: 78, tag: '影法师', description: '影法师的幻术，烟雾缭绕、幻影分身、漆黑匕首与阴影魔法' },
  { id: 79, tag: '水晶魔像', description: '水晶与宝石的魔像术，水晶簇、宝石核心、棱角分明的几何体' },
  { id: 80, tag: '魔兽世界', description: '魔兽世界部落vs联盟，标志性的阵营美学、狮鹫与飞龙' },
  { id: 81, tag: '巫师猎魔人', description: '巫师的怪物猎人，银剑、炼金药水、猎魔人徽章与变种印记' },
  { id: 82, tag: '哥布林工匠', description: '哥布林工匠的废品，简陋拼凑、爆炸性装置、齿轮与螺丝' },
  { id: 83, tag: '圣骑士', description: '圣骑士的圣光，战锤、圣契卷轴、金色光环与神圣纹章' },
  { id: 84, tag: '盗贼公会', description: '盗贼公会的秘密，淬毒匕首、开锁器、夜色斗篷与盗贼印记' },
  { id: 85, tag: '召唤师', description: '召唤师的契约，召唤法阵、异界生物、灵魂束缚与魔法书' },
  { id: 86, tag: '野蛮人', description: '野蛮人的原始之怒，巨斧、战纹彩绘、兽皮与部落图腾' },
  { id: 87, tag: '夺心魔', description: '伊利斯次代夺心魔，灵能波纹、触手、紫色皮肤与异界科技' },
  { id: 88, tag: '天界万神殿', description: '天界万神殿的神选者，星辰光芒、星座图案、神圣几何纹路' },
  { id: 89, tag: '诅咒海盗', description: '被诅咒的海盗船员，船幽灵、藤壶附着、鬼火与沉船' },
  { id: 90, tag: '时空法师', description: '时空法师的时间装备，沙漏、钟表齿轮、扭曲的时空纹理' },

  // 艺术与风格化 (25个)
  { id: 91, tag: '卡通渲染', description: '卡通渲染与漫画书，粗黑描边、平涂色块、半色调网点与动漫美学' },
  { id: 92, tag: '蒸汽波', description: '蒸汽波美学，粉蓝渐变、石膏像、Windows 95图标与怀旧科技' },
  { id: 93, tag: '极简主义', description: '极简主义与洁净，简化图标、单色配色、大量留白与现代设计' },
  { id: 94, tag: '剪纸世界', description: '剪纸与折纸世界，纸的纹理、折痕阴影、剪纸艺术与立体感' },
  { id: 95, tag: '黏土动画', description: '黏土动画与定格动画，指纹纹理、柔软的质感、手工制作感' },
  { id: 96, tag: '8位复古', description: '8位与复古街机，极低的像素、有限的调色板、红白机美学' },
  { id: 97, tag: '彩色玻璃', description: '彩色玻璃与大教堂，玻璃质感、彩色碎片、黑色边框与光线透射' },
  { id: 98, tag: '印象派', description: '印象派油画风格，可见的笔触、模糊的轮廓、光影变化' },
  { id: 99, tag: '蓝图示意', description: '蓝图与示意图，蓝色背景、白色线条、工程标注与技术图纸' },
  { id: 100, tag: '水墨书法', description: '活化的水墨与书法，墨滴晕染、飞白效果、书法笔触与禅意' },
  { id: 101, tag: '装饰艺术', description: '装饰艺术的黄金时代，强烈的几何形状、金色与黑色、对称美学' },
  { id: 102, tag: '新艺术运动', description: '新艺术运动的有机线条，流畅的曲线、花卉与昆虫图案、自然主义' },
  { id: 103, tag: '波普艺术', description: '波普艺术与安迪沃霍尔，鲜艳的重复图案、丝网印刷效果、大众文化' },
  { id: 104, tag: '涂鸦街头', description: '涂鸦与街头艺术，喷漆效果、模板字体、街头标签与叛逆文化' },
  { id: 105, tag: '素描本', description: '闹鬼的素描本，铅笔线条、潦草的阴影、炭笔涂抹与手绘感' },
  { id: 106, tag: '夜光霓虹', description: '夜光与霓虹，黑暗背景下的发光轮廓、荧光色彩、电子美学' },
  { id: 107, tag: '热成像', description: '红外与热成像，扭曲的色谱、热点高亮、军事科技美学' },
  { id: 108, tag: '乐高积木', description: '塑料玩具与乐高，光滑的塑料质感、颗粒拼接、鲜艳色块' },
  { id: 109, tag: '全息虹彩', description: '全息与虹彩，随角度变化的色彩、半透明、彩虹光谱效果' },
  { id: 110, tag: '土著点画', description: '澳洲土著点画，密集的圆点、大地色系、部落故事图案' },
  { id: 111, tag: '浮世绘', description: '浮世绘木版画，日式传统版画风格、线条感、平面装饰性' },
  { id: 112, tag: '包豪斯', description: '包豪斯功能主义，简洁的几何、三原色、功能决定形式' },
  { id: 113, tag: '超现实主义', description: '超现实主义与达利，融化的时钟、梦境逻辑、荒诞组合' },
  { id: 114, tag: '故障艺术', description: '故障艺术，数字干扰、色彩分离、VHS老旧磁带效果与像素错乱' },
  { id: 115, tag: '编织布料', description: '编织与针织布料，毛线纹理、布料质感、针脚与温暖手工感' },

  // 自然与恐怖 (25个)
  { id: 116, tag: '火山熔炉', description: '火山熔炉与岩浆，燃烧的裂缝、黑曜石、硫磺烟雾与熔岩流' },
  { id: 117, tag: '深海巨怪', description: '深海巨怪的恐怖，黑暗深渊、生物光、巨大的触手与未知生物' },
  { id: 118, tag: '冰川苔原', description: '冰川苔原与永冻土，冰锥、霜冻纹理、猛犸象牙与极地风暴' },
  { id: 119, tag: '克苏鲁', description: '洛夫克拉夫特的宇宙恐怖，非欧几里得几何、不可名状之物、疯狂符号' },
  { id: 120, tag: '丧尸末日', description: '丧尸末日幸存者，临时武器、血迹斑斑、破烂的衣物与求生装备' },
  { id: 121, tag: '吸血鬼', description: '哥特吸血鬼贵族，华丽的棺材、蝙蝠、深红天鹅绒与维多利亚哥特' },
  { id: 122, tag: '狼人氏族', description: '狼人森林氏族，爪痕、满月、原始图腾与野性力量' },
  { id: 123, tag: '瘟疫医生', description: '瘟疫医生与黑死病，鸟嘴面具、药草、瘴气与中世纪恐怖' },
  { id: 124, tag: '活体森林', description: '活体森林的复仇，扭曲的树根、荆棘、树人与自然之怒' },
  { id: 125, tag: '水晶洞穴', description: '水晶洞穴的光辉，发光晶簇、共生生物、地下奇观' },
  { id: 126, tag: '沙漠游牧', description: '沙漠游牧民的生存，沙岩、风化的骨骼、绿洲与骆驼商队' },
  { id: 127, tag: '热带雨林', description: '热带雨林的隐藏危险，毒蛙、食人花、藤蔓丛林与猛兽' },
  { id: 128, tag: '寂静岭', description: '寂静岭的表里世界，铁锈腐蚀、工业噪音、浓雾与心理恐怖' },
  { id: 129, tag: '恐怖嘉年华', description: '恐怖嘉年华，邪恶小丑、生锈的游乐设施、畸形马戏团' },
  { id: 130, tag: '闹鬼庄园', description: '维多利亚闹鬼庄园，蜘蛛网、幽灵、褪色的肖像画与诡异氛围' },
  { id: 131, tag: '外星生物', description: '外星行星的动植物，奇特的色彩、生物光、未知的形态与异星生态' },
  { id: 132, tag: '风暴领主', description: '风暴领主的雷霆，闪电纹路、旋风、乌云与雷电之力' },
  { id: 133, tag: '沼泽女巫', description: '沼泽女巫的药剂，冒泡的大锅、青蛙腿、萤火虫与沼气' },
  { id: 134, tag: '巨型昆虫', description: '巨型昆虫巢穴，几丁质外壳、蜂巢结构、黏液与虫茧' },
  { id: 135, tag: '夺身者', description: '夺身者入侵，豆荚、复制人、偏执感与外星寄生' },
  { id: 136, tag: '辐射变种', description: '辐射废土变种人，盖革计数器、变异的生物、核辐射光芒' },
  { id: 137, tag: '地心深处', description: '地心深处的构造之怒，地震裂缝、熔岩、原始矿石与地热' },
  { id: 138, tag: '极光', description: '天体北极光，飘逸的光幕、星尘、宇宙射线与极光之美' },
  { id: 139, tag: '石化森林', description: '石化森林的寂静，变成石头的树木、永恒的静止、化石纹理' },
  { id: 140, tag: '共生寄生', description: '共生寄生体，活体武器、脉动的组织、生物与宿主融合' },

  // 现代与趣味 (60个)
  { id: 141, tag: '现代军事', description: '现代军事与特种部队，战术装备、迷彩、夜视仪与军用科技' },
  { id: 142, tag: '都市潮牌', description: '都市潮牌与Hypebeast，街头时尚、限量联名、潮流Logo与球鞋文化' },
  { id: 143, tag: '糖果王国', description: '糖果王国，彩虹色、糖果包装纸、巧克力与棒棒糖' },
  { id: 144, tag: '快餐狂热', description: '快餐狂热，汉堡、薯条、可乐、快餐包装与黄红配色' },
  { id: 145, tag: '学校用品', description: '学校与课堂用品，铅笔、橡皮、尺子、教科书与作业本' },
  { id: 146, tag: '医疗急救', description: '医院与医疗急救，红十字、手术刀、绷带与急救箱' },
  { id: 147, tag: '赌场', description: '赌场与豪赌客，扑克牌、骰子、筹码与霓虹灯招牌' },
  { id: 148, tag: '厨师厨房', description: '厨师厨房与烹饪艺术，菜刀、炒锅、香料与米其林美食' },
  { id: 149, tag: '音乐节', description: '音乐节与DJ设备，音响、转盘、荧光棒与电音文化' },
  { id: 150, tag: '体育冠军', description: '各项体育冠军，奖杯、球类、运动鞋与体育精神' },
  { id: 151, tag: '建筑工地', description: '建筑工地与重型机械，推土机、安全帽、警示条与钢筋' },
  { id: 152, tag: '消防员', description: '消防员救援，消防车、水枪、防火服与英雄主义' },
  { id: 153, tag: '警察特警', description: '警察局与特警，警徽、手铐、警棍与执法装备' },
  { id: 154, tag: '邮局', description: '邮局与信件递送，邮票、信封、邮筒与邮政绿' },
  { id: 155, tag: '游戏厅', description: '游戏厅与复古游戏，街机、摇杆、像素游戏与代币' },
  { id: 156, tag: '园艺种植', description: '园艺与种植，花洒、园艺工具、盆栽与绿色植物' },
  { id: 157, tag: '艺术家', description: '艺术家工作室与绘画，画笔、调色板、画布与颜料' },
  { id: 158, tag: '好莱坞', description: '电影制片厂与好莱坞，摄影机、场记板、胶片与明星' },
  { id: 159, tag: '摇滚乐队', description: '摇滚乐队与重金属，电吉他、音箱、骷髅与叛逆' },
  { id: 160, tag: '滑板公园', description: '滑板公园与极限运动，滑板、涂鸦墙、护具与街头文化' },
  { id: 161, tag: '科学实验', description: '科学家的实验室，烧杯、显微镜、元素周期表与实验' },
  { id: 162, tag: '沙滩冲浪', description: '沙滩派对与冲浪，冲浪板、椰子树、比基尼与夏日' },
  { id: 163, tag: '圣诞节', description: '圣诞与节日欢乐，圣诞树、礼物盒、雪人与铃铛' },
  { id: 164, tag: '万圣节', description: '万圣节与鬼魅乐趣，南瓜灯、糖果、女巫帽与捣蛋' },
  { id: 165, tag: '上班族', description: '上班族的单调日常，电脑、咖啡、领带与格子间' },
  { id: 166, tag: '渔夫', description: '渔夫的渔具箱，鱼钩、浮标、鱼竿与渔网' },
  { id: 167, tag: '机械师', description: '机械师的车库，扳手、机油、轮胎与汽车改装' },
  { id: 168, tag: '伐木工', description: '伐木工的小屋，斧头、锯子、木柴与森林' },
  { id: 169, tag: '超市', description: '超市与杂货，购物车、条形码、货架与收银台' },
  { id: 170, tag: '游乐园', description: '游乐园与过山车，摩天轮、棉花糖、旋转木马与欢笑' },
  { id: 171, tag: '银行抢劫', description: '银行抢劫与金库，保险柜、钞票、黑色西装与面罩' },
  { id: 172, tag: '赛车', description: '赛车与赛车运动，方程式、轮胎痕、赛道与速度' },
  { id: 173, tag: 'DND套组', description: '龙与地下城玩家套组，20面骰、角色卡、地图与龙形骰盅' },
  { id: 174, tag: '90年代卡通', description: '90年代卡通频道风格，复古动画、怀旧角色与童年回忆' },
  { id: 175, tag: '乐高积木', description: '乐高与积木，塑料拼接、颗粒感、彩色方块与创意' },
  { id: 176, tag: '棋盘游戏', description: '国际象棋与棋盘游戏，棋子、棋盘格、策略与智力' },
  { id: 177, tag: '魔术表演', description: '魔术师的舞台表演，扑克牌、魔术帽、兔子与魔法棒' },
  { id: 178, tag: '黑色侦探', description: '黑色侦探与神秘，放大镜、烟斗、风衣与悬疑' },
  { id: 179, tag: '农贸市场', description: '农贸市场与农业，新鲜蔬菜、农具、稻草与乡村' },
  { id: 180, tag: '图书馆', description: '图书馆与古代典籍，书架、羊皮卷、墨水与知识' },
  { id: 181, tag: '太空任务', description: '宇航员的太空任务，太空舱、失重、地球与月球' },
  { id: 182, tag: '动物园', description: '动物园与野生动物，笼子、动物脚印、斑马纹与丛林' },
  { id: 183, tag: '马戏团', description: '马戏团，小丑、杂技、大帐篷与空中飞人' },
  { id: 184, tag: '健身房', description: '健身与健身房器材，哑铃、跑步机、蛋白粉与肌肉' },
  { id: 185, tag: '主播设备', description: '油管主/主播的设备，麦克风、摄像头、绿幕与弹幕' },
  { id: 186, tag: '装机硬件', description: '电脑硬件与装机，显卡、主板、RGB灯效与电竞' },
  { id: 187, tag: '冰淇淋店', description: '冰淇淋店，甜筒、勺子、彩色冰淇淋与甜蜜' },
  { id: 188, tag: '宝可梦', description: '宝可梦训练师装备，精灵球、图鉴、徽章与皮卡丘' },
  { id: 189, tag: '动森', description: '动物森友会的悠闲生活，捕虫网、钓鱼竿、DIY与可爱' },
  { id: 190, tag: '星露谷', description: '星露谷物语的农场生活，锄头、喷壶、作物与田园' },
  { id: 191, tag: 'AmongUs', description: 'Among Us船员装备，太空服、任务、电线与内鬼' },
  { id: 192, tag: '糖豆人', description: '糖豆人的多彩混乱，果冻质感、障碍赛、皇冠与欢乐' },
  { id: 193, tag: 'MC地下城', description: '我的世界地下城风格，方块美学、地牢、战利品与冒险' },
  { id: 194, tag: '泰拉瑞亚', description: '泰拉瑞亚的进阶，像素风、武器升级、Boss与探险' },
  { id: 195, tag: '空洞骑士', description: '空洞骑士的圣巢，黑白美学、虫族、幽灵与克制' },
  { id: 196, tag: '传说之下', description: '传说之下里的地下世界，像素、音乐、灵魂之心与温情' },
  { id: 197, tag: '茶杯头', description: '茶杯头的30年代动画，橡皮管动画、手绘背景与老式' },
  { id: 198, tag: '传送门', description: '传送门的测试室，橙蓝传送门、方块、AI与谜题' },
  { id: 199, tag: '军团要塞2', description: '军团要塞2的职业战争，卡通风格、职业特色与团队' },
  { id: 200, tag: '原神', description: '原神的提瓦特大陆，元素、角色、开放世界与二次元美学' },

  // 中国文化特色 (20个)
  { id: 201, tag: '西游记', description: '西游记与齐天大圣，金箍棒、筋斗云、紧箍咒、芭蕉扇与西天取经' },
  { id: 202, tag: '封神演义', description: '封神演义与道家法术，打神鞭、番天印、混元金斗、阴阳镜与道教仙术' },
  { id: 203, tag: '金庸武侠', description: '金庸的经典武侠江湖，屠龙刀、倚天剑、打狗棒、九阴真经与侠义精神' },
  { id: 204, tag: '仙剑奇侠传', description: '仙剑奇侠传的宿命爱恋，魔剑、天蛇杖、圣灵珠、酒神咒与仙侠爱情' },
  { id: 205, tag: '三国英雄', description: '三国英雄与兵法谋略，青龙偃月刀、丈八蛇矛、方天画戟、诸葛连弩与忠义' },
  { id: 206, tag: '兵马俑', description: '秦朝的兵马俑军阵，青铜剑、秦弩、黑色盔甲、长城砖与大一统威严' },
  { id: 207, tag: '大唐盛世', description: '大唐盛世的丝路繁华，唐刀、明光铠、三彩陶器、飞天壁画与奢华丝绸' },
  { id: 208, tag: '宋朝水墨', description: '宋朝的水墨丹青，黑白灰水墨画风格、留白意境、文人审美与诗意' },
  { id: 209, tag: '紫禁城', description: '紫禁城的皇家威严，龙袍、玉玺、景泰蓝、琉璃瓦与朱红宫墙' },
  { id: 210, tag: '童年零食', description: '童年零食与经典玩具，辣条、大白兔、AD钙奶、弹珠、铁皮青蛙与集体回忆' },
  { id: 211, tag: '中国新年', description: '中国新年的喜庆，鞭炮、红包、春联、灯笼、舞狮与团圆饺子' },
  { id: 212, tag: '校园时光', description: '课堂与校园时光，钢笔、三角尺、作文本、红墨水、五三教辅与青春' },
  { id: 213, tag: '赛博修仙', description: '中国特色的赛博朋克，霓虹牌匾、仿生兵马俑、数据流书法与未来旗袍' },
  { id: 214, tag: '中华美食', description: '灶王爷与中华美食，菜刀、擀面杖、炒锅、青花瓷碗、包子烤鸭火锅' },
  { id: 215, tag: '僵尸道士', description: '僵尸与道士的鬼怪世界，桃木剑、黄纸符、八卦镜、墨斗线与香港僵尸' },
  { id: 216, tag: '解放军', description: '中国军旅风格，95式步枪、工兵铲、解放鞋、军绿色与红色五角星' },
  { id: 217, tag: '土酷复古', description: '土酷与复古中国，红双喜搪瓷杯、凤凰自行车、80年代霓虹与的确良' },
  { id: 218, tag: '国宝文物', description: '国宝与博物馆文物，越王勾践剑、后母戊鼎、金缕玉衣与青铜瓷器' },
  { id: 219, tag: '麻将棋牌', description: '麻将与中国棋类，麻将发财、幺鸡、象棋马、围棋子与国民娱乐' },
  { id: 220, tag: '修仙网文', description: '修仙网文世界，飞剑、储物袋、炼丹炉、灵石、功法玉简与渡劫天雷' }
]

const useExample = (example) => {
  formData.styleDescription = example.description
  ElMessage.success('已应用示例风格')
}

const randomStyle = () => {
  const randomIndex = Math.floor(Math.random() * examples.length)
  const randomExample = examples[randomIndex]
  formData.styleDescription = randomExample.description
  ElMessage.success(`已抽取：${randomExample.tag}`)
}

const goToLogin = () => {
  router.push('/login')
}

const goToProfile = () => {
  router.push('/profile')
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    try {
      await logout()
    } catch (error) {
      // 继续
    }

    localStorage.removeItem('token')
    localStorage.removeItem('username')
    username.value = ''

    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 取消
  }
}

const handleSubmit = async () => {
  if (!formData.styleDescription) {
    ElMessage.warning('请输入风格描述')
    return
  }

  if (formData.styleDescription.length < 10) {
    ElMessage.warning('风格描述至少10个字符')
    return
  }

  if (!formData.templateId) {
    ElMessage.warning('请选择一个材质模板')
    return
  }

  try {
    await ElMessageBox.confirm(
      '确认生成材质包？生成过程需要1-2分钟。',
      '确认生成',
      {
        confirmButtonText: '确认生成',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    generating.value = true
    
    // 从选中的模板获取materialType
    const selectedTemplate = templateList.value.find(t => t.templateId === formData.templateId)
    const submitData = {
      ...formData,
      materialType: selectedTemplate ? selectedTemplate.materialType : (formData.generationType === 'item' ? 1 : 2)
    }
    
    await addGeneration(submitData)

    ElMessage.success('已提交生成任务，请在"我的任务"中查看进度')

    setTimeout(() => {
      router.push('/my-tasks')
    }, 2000)

  } catch (error) {
    if (error !== 'cancel') {
      // 如果错误信息包含"积分不足"，直接显示
      if (error.message && error.message.includes('积分不足')) {
        ElMessage.error(error.message)
      } else {
        ElMessage.error('非典型错误请联系管理员')
      }
    }
  } finally {
    generating.value = false
  }
}
</script>

<style scoped>
/* === 性能优化：防止滚动闪屏 === */
* {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 优化滚动性能 */
html {
  overflow-y: auto;
  overflow-x: hidden;
}

/* --- 整体布局 (与主页一致) --- */
.page-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 20px 20px 20px;
  transform-origin: top center;
  margin-bottom: 200px;
  transform: scale(1.5);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  will-change: transform;
  contain: paint;
}

.top-border {
  background-image: url('/assets/images/new-textures/default_obsidian_brick.png');
  background-repeat: repeat-x;
  background-size: 64px 64px;
  height: 48px;
  width: 100%;
  max-width: 996px;
  border: 4px solid var(--mc-border-black);
  border-bottom: 0;
  flex-shrink: 0;
  image-rendering: pixelated;
  transform: translateZ(0);
}

.content-body {
  display: flex;
  justify-content: center;
  width: 100%;
  max-width: 1004px;
}

.side-border {
  background-image: url('/assets/images/new-textures/default_obsidian_block.png');
  background-repeat: repeat-y;
  background-size: 64px 64px;
  width: 48px;
  flex-shrink: 0;
  border: 4px solid var(--mc-border-black);
  border-top: 0;
  border-bottom: 0;
  image-rendering: pixelated;
  transform: translateZ(0);
}
.side-border.left { border-right: 4px solid var(--mc-border-black); }
.side-border.right { border-left: 4px solid var(--mc-border-black); }

main {
  max-width: 900px;
  width: 100%;
  /* 内部深色背景材质 (松木+遮罩) */
  background-image: linear-gradient(rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.4)), url('/assets/images/new-textures/default_pine_wood.png'); 
  background-size: auto, 64px 64px;
  border: 4px solid var(--mc-border-black);
  border-top: 0;
  padding: 0 20px 20px 20px;
  image-rendering: pixelated;
}

/* --- 通用UI组件 --- */
.mc-box {
  background-color: rgba(40, 40, 40, 0.85);
  backdrop-filter: blur(4px);
  border: 2px solid var(--mc-border-black);
  box-shadow: inset 2px 2px 0px var(--mc-border-light), inset -2px -2px 0px var(--mc-border-dark);
  padding: 8px 12px;
  text-align: center;
  display: inline-block;
  color: var(--mc-text-color);
  text-decoration: none;
  font-family: inherit;
  cursor: pointer;
}
a.mc-box:hover, button.mc-box:hover, .example-item:hover {
  background-color: rgba(60, 60, 60, 0.9);
  box-shadow: inset 2px 2px 0px #666, inset -2px -2px 0px #222;
}
button.mc-box { 
  font-size: 14px;
  will-change: background-color, box-shadow;
  transform: translateZ(0);
}

/* --- 页头 Header --- */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  flex-wrap: wrap;
  gap: 10px;
}
.logo { font-size: 24px; }
.main-nav ul { display: flex; gap: 10px; list-style: none; padding: 0; margin: 0; }
.main-nav a.mc-box { padding: 6px 10px; font-size: 14px; }
.main-nav a.mc-box.active {
  background-color: #585858;
  box-shadow: inset 2px 2px 0px #3a3a3a, inset -2px -2px 0px #a0a0a0;
  color: #ffffa0;
}

.user-actions { display: flex; align-items: center; gap: 10px; }
.username-display {
  font-size: 12px;
  padding: 6px;
  cursor: pointer;
  transition: all 0.2s;
}
.username-display:hover {
  background: #4a4a4a;
  transform: translateY(-2px);
}

.diamond-icon {
  margin-left: 4px;
  animation: sparkle 2s infinite;
}

@keyframes sparkle {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.1); }
}

.login-btn, .logout-btn { padding: 6px 10px; font-size: 14px; }

/* --- 生成页面样式 --- */
.generate-section, .examples-section { margin-bottom: 24px; }

.section-header {
  width: 100%;
  margin-bottom: 16px;
  background-image: linear-gradient(rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.3)), url('/assets/images/new-textures/default_obsidian_brick.png');
  background-size: auto, 64px 64px;
  text-align: left;
  padding: 12px 20px;
}
.section-header h2 { 
  font-size: 24px; 
  margin: 0; 
  text-shadow: 2px 2px 0 #3a3a3a, -2px -2px 0 #3a3a3a, 2px -2px 0 #3a3a3a, -2px 2px 0 #3a3a3a;
}

.generate-form-container {
  width: 100%;
  padding: 24px;
  background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('/assets/images/new-textures/default_wood.png');
  background-size: auto, 64px 64px;
  text-align: left;
  cursor: default; /* Form container isn't clickable */
}
.generate-form-container:hover {
  background-color: rgba(40, 40, 40, 0.85); /* Keep same background */
  box-shadow: inset 2px 2px 0px var(--mc-border-light), inset -2px -2px 0px var(--mc-border-dark); /* Disable hover effect */
}

.form-group { margin-bottom: 20px; }

.form-label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  color: #fff;
  margin-bottom: 10px;
  text-shadow: 2px 2px 0 #000;
}
.label-icon { width: 24px; height: 24px; image-rendering: pixelated; }

.mc-textarea, .mc-select {
  width: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  border: 2px solid #a0a0a0;
  border-top-color: #000;
  border-left-color: #000;
  color: #fff;
  padding: 10px;
  font-family: inherit;
  font-size: 14px;
  outline: none;
}
.mc-textarea:focus, .mc-select:focus {
  background-color: rgba(0, 0, 0, 0.7);
  border-bottom-color: #fff;
  border-right-color: #fff;
}

.mc-select { cursor: pointer; }
.mc-select option { background-color: #333; }

.char-count { text-align: right; font-size: 12px; color: #ccc; margin-top: 4px; text-shadow: 1px 1px 0 #000; }

.form-label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.btn-random-style {
  font-size: 14px;
  padding: 6px 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(0, 0, 0, 0.3);
  cursor: pointer;
  border: 2px solid #555;
}
.btn-random-style img { width: 20px; height: 20px; image-rendering: pixelated; }
.btn-random-style:hover { 
  background: rgba(41, 169, 162, 0.3);
  border-color: #29a9a2;
}

.generation-type-options {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.type-option {
  flex: 1;
  padding: 12px;
  cursor: pointer;
  background: rgba(0, 0, 0, 0.3);
  border: 2px solid #555;
  transition: all 0.2s;
}

.type-option input[type="radio"] {
  display: none;
}

.type-option.active {
  background: rgba(41, 169, 162, 0.3);
  border-color: #29a9a2;
}

.type-option:hover {
  background: rgba(41, 169, 162, 0.2);
  border-color: #29a9a2;
}

.option-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.option-title {
  font-size: 16px;
  font-weight: bold;
  color: #fff;
}

.option-desc {
  font-size: 12px;
  color: #ccc;
}

.form-actions { margin-top: 30px; text-align: center; }

.btn-generate {
  width: 100%;
  font-size: 20px;
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: linear-gradient(to right, #29a9a2, #47d6cd);
  will-change: opacity;
  transform: translateZ(0);
}
.btn-generate img { width: 32px; height: 32px; image-rendering: pixelated; }

.form-tip { margin-top: 10px; font-size: 12px; color: #ddd; text-shadow: 1px 1px 0 #000; }

/* --- 示例风格 --- */
.examples-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.example-item {
  background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('/assets/images/new-textures/default_obsidian_brick.png');
  background-size: auto, 64px 64px;
  text-align: left;
  padding: 16px;
  transform: translateZ(0);
  will-change: background-color, box-shadow;
}

.example-tag {
  display: inline-block;
  background-color: #e6ac00;
  border: 2px solid #000;
  padding: 4px 8px;
  font-size: 12px;
  color: #000;
  margin-bottom: 8px;
  box-shadow: inset 2px 2px 0 rgba(255,255,255,0.4);
  text-shadow: none; /* Fix ghosting by removing inherited text shadow */
}

.example-text {
  font-size: 12px;
  line-height: 1.5;
  margin: 0;
  color: #eee;
  text-shadow: 1px 1px 0 #000;
}
</style>