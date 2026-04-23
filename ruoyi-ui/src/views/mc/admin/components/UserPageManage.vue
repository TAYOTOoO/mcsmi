<template>
  <div class="app-container">
    <!-- 搜索 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true">
      <el-form-item label="配置键">
        <el-input v-model="queryParams.configKey" placeholder="请输入配置键" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="页面名称">
        <el-select v-model="queryParams.pageName" placeholder="请选择" clearable>
          <el-option label="首页" value="home" />
          <el-option label="生成页面" value="generate" />
          <el-option label="任务页面" value="tasks" />
          <el-option label="充值页面" value="recharge" />
          <el-option label="编辑器" value="editor" />
          <el-option label="个人中心" value="profile" />
          <el-option label="系统全局" value="system" />
        </el-select>
      </el-form-item>
      <el-form-item label="配置类型">
        <el-select v-model="queryParams.configType" placeholder="请选择" clearable>
          <el-option label="文本" value="text" />
          <el-option label="数字" value="number" />
          <el-option label="图片" value="image" />
          <el-option label="JSON" value="json" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable>
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增配置</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" icon="el-icon-delete" :disabled="multiple" @click="handleDelete">批量删除</el-button>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="配置ID" align="center" prop="configId" width="80" />
      <el-table-column label="配置键" align="center" prop="configKey" width="200" :show-overflow-tooltip="true" />
      <el-table-column label="配置值" align="center" prop="configValue" width="200" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span v-if="scope.row.configType === 'image'">
            <el-image
              v-if="scope.row.configValue"
              :src="scope.row.configValue"
              style="width: 50px; height: 50px; object-fit: cover;"
              :preview-src-list="[scope.row.configValue]"
            />
            <span v-else>-</span>
          </span>
          <span v-else>{{ scope.row.configValue || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="配置类型" align="center" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.configType === 'text'" type="primary">文本</el-tag>
          <el-tag v-else-if="scope.row.configType === 'number'" type="success">数字</el-tag>
          <el-tag v-else-if="scope.row.configType === 'image'" type="warning">图片</el-tag>
          <el-tag v-else-if="scope.row.configType === 'json'" type="info">JSON</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="页面名称" align="center" prop="pageName" width="120">
        <template slot-scope="scope">
          <span v-if="scope.row.pageName === 'home'">首页</span>
          <span v-else-if="scope.row.pageName === 'generate'">生成页面</span>
          <span v-else-if="scope.row.pageName === 'tasks'">任务页面</span>
          <span v-else-if="scope.row.pageName === 'recharge'">充值页面</span>
          <span v-else-if="scope.row.pageName === 'editor'">编辑器</span>
          <span v-else-if="scope.row.pageName === 'profile'">个人中心</span>
          <span v-else-if="scope.row.pageName === 'system'">系统全局</span>
          <span v-else>{{ scope.row.pageName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="分组名称" align="center" prop="groupName" width="120" />
      <el-table-column label="排序" align="center" prop="sortOrder" width="80" />
      <el-table-column label="配置说明" align="center" prop="description" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加/修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="form.configKey" placeholder="如: home.hero.title" :disabled="form.configId != null" />
          <div class="tip">唯一标识，建议使用点分隔的层级结构，如: home.hero.title</div>
        </el-form-item>

        <el-form-item label="配置类型" prop="configType">
          <el-radio-group v-model="form.configType">
            <el-radio label="text">文本</el-radio>
            <el-radio label="number">数字</el-radio>
            <el-radio label="image">图片</el-radio>
            <el-radio label="json">JSON</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="配置值" prop="configValue">
          <!-- 文本类型 -->
          <el-input
            v-if="form.configType === 'text'"
            v-model="form.configValue"
            type="textarea"
            :rows="3"
            placeholder="请输入配置值"
          />
          <!-- 数字类型 -->
          <el-input
            v-else-if="form.configType === 'number'"
            v-model="form.configValue"
            placeholder="请输入数字"
          />
          <!-- JSON类型 -->
          <el-input
            v-else-if="form.configType === 'json'"
            v-model="form.configValue"
            type="textarea"
            :rows="5"
            placeholder='请输入JSON格式的配置，例如: {"key": "value"}'
          />
          <!-- 图片类型 -->
          <div v-else-if="form.configType === 'image'">
            <el-upload
              class="upload-demo"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :before-upload="beforeUpload"
            >
              <el-button size="small" type="primary">点击上传图片</el-button>
            </el-upload>
            <div v-if="form.configValue" style="margin-top: 10px;">
              <el-image
                :src="form.configValue"
                style="width: 150px; height: 150px; object-fit: cover;"
                :preview-src-list="[form.configValue]"
              />
              <el-button size="mini" type="danger" @click="form.configValue = ''" style="display: block; margin-top: 5px;">删除图片</el-button>
            </div>
            <div class="tip">上传后图片URL会自动填充</div>
          </div>
        </el-form-item>

        <el-form-item label="页面名称" prop="pageName">
          <el-select v-model="form.pageName" placeholder="请选择">
            <el-option label="首页" value="home" />
            <el-option label="生成页面" value="generate" />
            <el-option label="任务页面" value="tasks" />
            <el-option label="充值页面" value="recharge" />
            <el-option label="编辑器" value="editor" />
            <el-option label="个人中心" value="profile" />
            <el-option label="系统全局" value="system" />
          </el-select>
        </el-form-item>

        <el-form-item label="分组名称" prop="groupName">
          <el-input v-model="form.groupName" placeholder="如: hero, stats, features 等" />
          <div class="tip">用于页面内分组，方便管理和查询</div>
        </el-form-item>

        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
          <div class="tip">数字越小越靠前</div>
        </el-form-item>

        <el-form-item label="配置说明" prop="description">
          <el-input v-model="form.description" placeholder="请输入配置说明" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listUserPageConfig, getUserPageConfig, addUserPageConfig, updateUserPageConfig, delUserPageConfig } from '@/api/mc/userpage'
import { getToken } from '@/utils/auth'

export default {
  name: 'McUserPageManage',
  data() {
    return {
      loading: false,
      configList: [],
      ids: [],
      single: true,
      multiple: true,
      total: 0,
      open: false,
      title: '',
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload',
      uploadHeaders: { Authorization: 'Bearer ' + getToken() },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        configKey: null,
        configType: null,
        pageName: null,
        status: null
      },
      form: {},
      rules: {
        configKey: [{ required: true, message: '配置键不能为空', trigger: 'blur' }],
        configType: [{ required: true, message: '请选择配置类型', trigger: 'change' }],
        configValue: [{ required: true, message: '配置值不能为空', trigger: 'blur' }],
        pageName: [{ required: true, message: '请选择页面名称', trigger: 'change' }],
        sortOrder: [{ required: true, message: '排序不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listUserPageConfig(this.queryParams).then(response => {
        this.configList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.configId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '添加用户端页面配置'
    },
    handleUpdate(row) {
      this.reset()
      const configId = row.configId || this.ids
      getUserPageConfig(configId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改用户端页面配置'
      })
    },
    handleDelete(row) {
      const configIds = row.configId ? [row.configId] : this.ids
      this.$confirm('是否确认删除选中的配置项?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delUserPageConfig(configIds)
      }).then(() => {
        this.getList()
        this.$message.success('删除成功')
      })
    },
    handleStatusChange(row) {
      updateUserPageConfig(row).then(() => {
        this.$message.success('状态修改成功')
      }).catch(() => {
        row.status = row.status === '0' ? '1' : '0'
      })
    },
    handleUploadSuccess(response, file) {
      if (response.code === 200) {
        this.form.configValue = response.url
        this.$message.success('图片上传成功')
      } else {
        this.$message.error('图片上传失败: ' + response.msg)
      }
    },
    beforeUpload(file) {
      const isImage = file.type.indexOf('image/') === 0
      const isLt5M = file.size / 1024 / 1024 < 5

      if (!isImage) {
        this.$message.error('只能上传图片文件!')
      }
      if (!isLt5M) {
        this.$message.error('图片大小不能超过 5MB!')
      }
      return isImage && isLt5M
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          // 验证JSON格式
          if (this.form.configType === 'json' && this.form.configValue) {
            try {
              JSON.parse(this.form.configValue)
            } catch (e) {
              this.$message.error('配置值必须是有效的JSON格式')
              return
            }
          }

          if (this.form.configId != null) {
            updateUserPageConfig(this.form).then(() => {
              this.$message.success('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addUserPageConfig(this.form).then(() => {
              this.$message.success('新增成功')
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        configId: null,
        configKey: null,
        configValue: null,
        configType: 'text',
        pageName: null,
        groupName: null,
        sortOrder: 0,
        description: null,
        status: '0',
        remark: null
      }
      this.resetForm('form')
    }
  }
}
</script>

<style scoped>
.tip {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
  line-height: 1.5;
}
</style>
