<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="card-header">
        <span>VIP功能管理</span>
        <el-button style="float: right" type="primary" size="small" @click="handleSave" :loading="saving">
          保存配置
        </el-button>
      </div>

      <div v-loading="loading">
        <el-alert
          title="VIP功能开关管理"
          description="管理钻石订阅用户专属功能的启用/禁用状态。关闭后对应功能将对所有VIP用户不可见。"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px"
        />

        <el-form label-width="280px" label-position="left">
          <el-divider content-position="left">图片处理功能</el-divider>

          <el-form-item label="智能边缘修复">
            <el-switch v-model="form.smart_edge_repair" />
            <span class="config-desc">用内层像素颜色覆盖边缘半透明像素，解决抠底后白边问题</span>
          </el-form-item>

          <el-form-item label="自动识别底色抠底">
            <el-switch v-model="form.auto_bg_detect" />
            <span class="config-desc">自动检测图片边缘主色调，一键抠除背景色</span>
          </el-form-item>

          <el-form-item label="颜色容差调节">
            <el-switch v-model="form.color_tolerance" />
            <span class="config-desc">允许VIP用户通过滑块调节画笔工具的颜色容差（1-100）</span>
          </el-form-item>

          <el-form-item label="Alpha二值化">
            <el-switch v-model="form.alpha_binarize" />
            <span class="config-desc">一键将所有半透明像素转为完全透明或完全不透明</span>
          </el-form-item>

          <el-divider content-position="left">编辑器功能</el-divider>

          <el-form-item label="历史材质库">
            <el-switch v-model="form.history_library" />
            <span class="config-desc">在材质编辑器中浏览和复用历史生成的素材</span>
          </el-form-item>

          <el-form-item label="预处理参数预设">
            <el-switch v-model="form.presets" />
            <span class="config-desc">允许保存和切换多套预处理参数配置</span>
          </el-form-item>

          <el-divider content-position="left">入口功能</el-divider>

          <el-form-item label="VIP图片分割工厂入口">
            <el-switch v-model="form.factory_entry" />
            <span class="config-desc">独立的VIP图片分割工厂页面，支持拖拽上传和历史任务选择</span>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script>
import { listConfig, addConfig, updateConfig } from '@/api/mc/config'

const VIP_FEATURES = [
  'smart_edge_repair',
  'auto_bg_detect',
  'color_tolerance',
  'alpha_binarize',
  'history_library',
  'presets',
  'factory_entry'
]

export default {
  name: 'VipFeatureManage',
  data() {
    return {
      loading: false,
      saving: false,
      form: {
        smart_edge_repair: true,
        auto_bg_detect: true,
        color_tolerance: true,
        alpha_binarize: true,
        history_library: true,
        presets: true,
        factory_entry: true
      },
      // 存储每个配置项的configId，用于更新
      configMap: {}
    }
  },
  mounted() {
    this.loadConfigs()
  },
  methods: {
    async loadConfigs() {
      this.loading = true
      try {
        const res = await listConfig({ pageNum: 1, pageSize: 100 })
        if (res.code === 200 && res.rows) {
          res.rows.forEach(config => {
            if (config.configKey && config.configKey.startsWith('vip.feature.')) {
              const featureName = config.configKey.replace('vip.feature.', '')
              if (VIP_FEATURES.includes(featureName)) {
                this.form[featureName] = config.configValue === '1' || config.configValue === 'true'
                this.configMap[featureName] = config
              }
            }
          })
        }
      } catch (e) {
        this.$message.error('加载VIP功能配置失败')
      } finally {
        this.loading = false
      }
    },
    async handleSave() {
      this.saving = true
      try {
        for (const feature of VIP_FEATURES) {
          const value = this.form[feature] ? '1' : '0'
          const existing = this.configMap[feature]

          if (existing) {
            await updateConfig({
              configId: existing.configId,
              configKey: existing.configKey,
              configValue: value,
              configName: existing.configName,
              configType: existing.configType
            })
          } else {
            await addConfig({
              configKey: `vip.feature.${feature}`,
              configValue: value,
              configName: `VIP-${feature}`,
              configType: '3',
              remark: 'VIP功能开关'
            })
          }
        }
        this.$message.success('VIP功能配置已保存')
        await this.loadConfigs()
      } catch (e) {
        this.$message.error('保存失败：' + (e.message || e))
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.config-desc {
  margin-left: 15px;
  color: #909399;
  font-size: 13px;
}
</style>
