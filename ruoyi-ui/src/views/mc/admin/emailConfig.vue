<template>
  <div class="app-container">
    <el-card class="box-card" shadow="never">
      <div slot="header" class="clearfix">
        <span class="card-title">📧 邮件配置</span>
        <el-button style="float: right;" type="primary" size="small" @click="handleSave">保存配置</el-button>
      </div>

      <el-form ref="form" :model="form" :rules="rules" label-width="140px">
        <el-divider content-position="left">注册验证配置</el-divider>

        <el-form-item label="邮箱验证开关">
          <el-switch v-model="form.emailVerifyEnabled" active-text="开启" inactive-text="关闭"></el-switch>
          <span class="form-tip">关闭后，用户注册时不需要邮箱验证</span>
        </el-form-item>

        <el-divider content-position="left">SMTP服务器配置</el-divider>

        <el-form-item label="SMTP服务器" prop="host">
          <el-input v-model="form.host" placeholder="例如：smtp.qq.com">
            <template slot="append">
              <el-select v-model="quickSelect" placeholder="快捷选择" style="width: 150px" @change="handleQuickSelect">
                <el-option label="QQ邮箱" value="qq"></el-option>
                <el-option label="163邮箱" value="163"></el-option>
                <el-option label="Gmail" value="gmail"></el-option>
              </el-select>
            </template>
          </el-input>
          <span class="form-tip">邮件服务器地址</span>
        </el-form-item>

        <el-form-item label="SMTP端口" prop="port">
          <el-input-number v-model="form.port" :min="1" :max="65535" style="width: 200px;"></el-input-number>
          <span class="form-tip">SSL通常使用465，TLS使用587</span>
        </el-form-item>

        <el-form-item label="启用SSL" prop="sslEnable">
          <el-switch v-model="form.sslEnable" active-text="启用" inactive-text="禁用"></el-switch>
          <span class="form-tip">建议启用SSL加密连接</span>
        </el-form-item>

        <el-divider content-position="left">发件人信息</el-divider>

        <el-form-item label="发件人邮箱" prop="username">
          <el-input v-model="form.username" placeholder="your-email@qq.com">
            <template slot="prepend">
              <i class="el-icon-message"></i>
            </template>
          </el-input>
          <span class="form-tip">用于发送邮件的邮箱地址</span>
        </el-form-item>

        <el-form-item label="邮箱授权码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入SMTP授权码">
            <template slot="prepend">
              <i class="el-icon-key"></i>
            </template>
          </el-input>
          <span class="form-tip">邮箱的SMTP授权码（不是登录密码）
            <el-link type="primary" :underline="false" @click="showAuthCodeHelp">如何获取？</el-link>
          </span>
        </el-form-item>

        <el-form-item label="发件人名称" prop="fromName">
          <el-input v-model="form.fromName" placeholder="材质工坊">
            <template slot="prepend">
              <i class="el-icon-user"></i>
            </template>
          </el-input>
          <span class="form-tip">邮件显示的发件人名称，为空则使用邮箱地址</span>
        </el-form-item>

        <el-divider content-position="left">测试邮件</el-divider>

        <el-form-item label="测试收件人">
          <el-input v-model="testEmail" placeholder="请输入测试收件人邮箱" style="width: 400px;">
            <template slot="append">
              <el-button icon="el-icon-s-promotion" :loading="sendingTest" @click="handleTest">发送测试邮件</el-button>
            </template>
          </el-input>
          <span class="form-tip">配置保存后，可以发送测试邮件验证配置是否正确</span>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog title="如何获取邮箱授权码" :visible.sync="authCodeHelpVisible" width="600px">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="QQ邮箱" name="qq">
          <ol class="help-list">
            <li>登录 <el-link type="primary" href="https://mail.qq.com" target="_blank">QQ邮箱</el-link></li>
            <li>点击顶部 <strong>设置</strong> → <strong>账户</strong></li>
            <li>找到 <strong>POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务</strong></li>
            <li>开启 <strong>POP3/SMTP服务</strong> 或 <strong>IMAP/SMTP服务</strong></li>
            <li>点击 <strong>生成授权码</strong>，按提示完成验证</li>
            <li>复制生成的授权码（16位字符），填入上方"邮箱授权码"框</li>
          </ol>
          <el-alert type="warning" :closable="false">
            注意：授权码不是QQ密码，是一串16位的随机字符
          </el-alert>
        </el-tab-pane>

        <el-tab-pane label="163邮箱" name="163">
          <ol class="help-list">
            <li>登录 <el-link type="primary" href="https://mail.163.com" target="_blank">163邮箱</el-link></li>
            <li>点击顶部 <strong>设置</strong> → <strong>POP3/SMTP/IMAP</strong></li>
            <li>开启 <strong>IMAP/SMTP服务</strong> 或 <strong>POP3/SMTP服务</strong></li>
            <li>按照提示用手机发送短信验证</li>
            <li>验证成功后，系统会显示授权码</li>
            <li>复制授权码，填入上方"邮箱授权码"框</li>
          </ol>
        </el-tab-pane>

        <el-tab-pane label="Gmail" name="gmail">
          <ol class="help-list">
            <li>访问 <el-link type="primary" href="https://myaccount.google.com" target="_blank">Google账号</el-link></li>
            <li>左侧菜单选择 <strong>安全性</strong></li>
            <li>在"登录Google"部分，选择 <strong>两步验证</strong>（需要先开启）</li>
            <li>滚动到底部，点击 <strong>应用专用密码</strong></li>
            <li>选择应用类型"邮件"，设备类型"其他"，输入"材质工坊"</li>
            <li>点击生成，复制16位密码，填入上方"邮箱授权码"框</li>
          </ol>
          <el-alert type="info" :closable="false">
            注意：需要先开启两步验证才能生成应用专用密码
          </el-alert>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script>
import { listConfig, updateConfig, addConfig } from '@/api/mc/config'
import request from '@/utils/request'

export default {
  name: 'EmailConfig',
  data() {
    return {
      form: {
        emailVerifyEnabled: true,
        host: 'smtp.qq.com',
        port: 465,
        username: '',
        password: '',
        fromName: '材质工坊',
        sslEnable: true
      },
      quickSelect: '',
      testEmail: '',
      sendingTest: false,
      authCodeHelpVisible: false,
      activeTab: 'qq',
      rules: {
        host: [{ required: true, message: '请输入SMTP服务器地址', trigger: 'blur' }],
        port: [{ required: true, message: '请输入端口号', trigger: 'blur' }],
        username: [
          { required: true, message: '请输入发件人邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        password: [{ required: true, message: '请输入邮箱授权码', trigger: 'blur' }]
      },
      configMap: {}
    }
  },
  created() {
    this.loadConfig()
  },
  methods: {
    async loadConfig() {
      try {
        // 加载邮件配置
        const response = await listConfig({
          pageNum: 1,
          pageSize: 100,
          configKey: 'mail.'
        })

        const configs = response.rows || []
        configs.forEach(config => {
          this.configMap[config.configKey] = config
          const key = config.configKey.replace('mail.smtp.', '')
          if (key === 'host') this.form.host = config.configValue || 'smtp.qq.com'
          else if (key === 'port') this.form.port = parseInt(config.configValue) || 465
          else if (key === 'username') this.form.username = config.configValue || ''
          else if (key === 'password') this.form.password = config.configValue || ''
          else if (key === 'from') this.form.fromName = config.configValue || '材质工坊'
          else if (key === 'ssl.enable') this.form.sslEnable = config.configValue === 'true'
        })

        // 加载邮箱验证开关配置
        const verifyResponse = await listConfig({
          pageNum: 1,
          pageSize: 10,
          configKey: 'system.register.emailVerify'
        })
        const verifyConfig = verifyResponse.rows?.[0]
        if (verifyConfig) {
          this.configMap[verifyConfig.configKey] = verifyConfig
          this.form.emailVerifyEnabled = verifyConfig.configValue === 'true'
        }
      } catch (error) {
        this.$modal.msgError('加载配置失败')
      }
    },

    async handleSave() {
      this.$refs.form.validate(async valid => {
        if (!valid) return

        try {
          const configs = [
            { key: 'mail.smtp.host', value: this.form.host },
            { key: 'mail.smtp.port', value: String(this.form.port) },
            { key: 'mail.smtp.username', value: this.form.username },
            { key: 'mail.smtp.password', value: this.form.password },
            { key: 'mail.smtp.from', value: this.form.fromName },
            { key: 'mail.smtp.ssl.enable', value: String(this.form.sslEnable) },
            { key: 'system.register.emailVerify', value: String(this.form.emailVerifyEnabled) }
          ]

          for (const config of configs) {
            const existing = this.configMap[config.key]
            if (existing) {
              await updateConfig({
                configId: existing.configId,
                configKey: config.key,
                configValue: config.value,
                configName: existing.configName,
                configType: existing.configType
              })
            } else {
              await addConfig({
                configKey: config.key,
                configValue: config.value,
                configName: config.key,
                configType: '2'
              })
            }
          }

          this.$modal.msgSuccess('配置保存成功')
          this.loadConfig()
        } catch (error) {
          this.$modal.msgError('配置保存失败')
        }
      })
    },

    async handleTest() {
      if (!this.testEmail) {
        this.$modal.msgWarning('请输入测试收件人邮箱')
        return
      }

      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.testEmail)) {
        this.$modal.msgWarning('请输入正确的邮箱地址')
        return
      }

      this.sendingTest = true
      try {
        await request({
          url: '/mc/system/config/testEmail',
          method: 'post',
          data: { email: this.testEmail }
        })
        this.$modal.msgSuccess('测试邮件已发送，请检查邮箱')
      } catch (error) {
        this.$modal.msgError(error.msg || '测试邮件发送失败')
      } finally {
        this.sendingTest = false
      }
    },

    handleQuickSelect(value) {
      const configs = {
        qq: { host: 'smtp.qq.com', port: 465, sslEnable: true },
        '163': { host: 'smtp.163.com', port: 465, sslEnable: true },
        gmail: { host: 'smtp.gmail.com', port: 587, sslEnable: false }
      }
      const config = configs[value]
      if (config) {
        this.form.host = config.host
        this.form.port = config.port
        this.form.sslEnable = config.sslEnable
      }
      this.quickSelect = ''
    },

    showAuthCodeHelp() {
      this.authCodeHelpVisible = true
      if (this.form.host.includes('qq')) this.activeTab = 'qq'
      else if (this.form.host.includes('163')) this.activeTab = '163'
      else if (this.form.host.includes('gmail')) this.activeTab = 'gmail'
    }
  }
}
</script>

<style scoped lang="scss">
.card-title {
  font-size: 18px;
  font-weight: bold;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}

.help-list {
  line-height: 2;
  padding-left: 20px;

  li {
    margin-bottom: 10px;
  }

  strong {
    color: #409EFF;
  }
}

.el-divider--horizontal {
  margin: 30px 0 20px;
}
</style>
