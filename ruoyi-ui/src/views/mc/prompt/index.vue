<template>
  <div class="app-container">
    <!-- 搜索 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true">
      <el-form-item label="模板名称">
        <el-input v-model="queryParams.templateName" placeholder="请输入模板名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="是否启用">
        <el-select v-model="queryParams.isActive" placeholder="请选择" clearable>
          <el-option label="已启用" value="1" />
          <el-option label="未启用" value="0" />
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
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增模板</el-button>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="templateList">
      <el-table-column label="模板ID" align="center" prop="templateId" width="80" />
      <el-table-column label="模板名称" align="center" prop="templateName" width="200" />
      <el-table-column label="模板类型" align="center" width="120">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.templateType === 'standard'" type="success">标准模板</el-tag>
          <el-tag v-else type="primary">自定义模板</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="是否启用" align="center" width="100">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.isActive"
            active-value="1"
            inactive-value="0"
            @change="handleActiveChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === '0'" type="success">正常</el-tag>
          <el-tag v-else type="danger">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="描述" align="center" prop="description" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" width="200">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)">查看</el-button>
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
    <el-dialog :title="title" :visible.sync="open" width="900px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="form.templateName" placeholder="请输入模板名称" />
        </el-form-item>

        <el-form-item label="模板类型" prop="templateType">
          <el-radio-group v-model="form.templateType">
            <el-radio label="standard">标准模板</el-radio>
            <el-radio label="custom">自定义模板</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="模板内容" prop="templateContent">
          <el-input
            v-model="form.templateContent"
            type="textarea"
            :rows="15"
            placeholder="请输入模板内容，使用 [] 作为用户输入的占位符&#10;&#10;示例：&#10;Generate Minecraft [] style textures with the following properties:&#10;- Theme: []&#10;- Style: High quality, detailed"
          />
          <div class="tip">
            <strong>使用说明：</strong><br/>
            • 在需要插入用户输入的地方使用 <code>[]</code> 占位符<br/>
            • 用户提交的风格描述会自动替换所有 <code>[]</code> 占位符<br/>
            • 支持多处使用 <code>[]</code> 占位符<br/>
            • 示例：用户输入"中国风"，<code>[]</code> 会被替换为"中国风"
          </div>
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入模板描述" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">禁用</el-radio>
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

    <!-- 查看对话框 -->
    <el-dialog title="查看模板" :visible.sync="viewOpen" width="900px" append-to-body>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="模板名称">{{ viewData.templateName }}</el-descriptions-item>
        <el-descriptions-item label="模板类型">
          <el-tag v-if="viewData.templateType === 'standard'" type="success">标准模板</el-tag>
          <el-tag v-else type="primary">自定义模板</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="是否启用">
          <el-tag v-if="viewData.isActive === '1'" type="success">已启用</el-tag>
          <el-tag v-else type="info">未启用</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="viewData.status === '0'" type="success">正常</el-tag>
          <el-tag v-else type="danger">禁用</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述">{{ viewData.description }}</el-descriptions-item>
        <el-descriptions-item label="模板内容">
          <pre style="white-space: pre-wrap; word-wrap: break-word; max-height: 400px; overflow-y: auto;">{{ viewData.templateContent }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ viewData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ viewData.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ viewData.remark }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="viewOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPromptTemplate, getPromptTemplate, addPromptTemplate, updatePromptTemplate, delPromptTemplate, activatePromptTemplate } from '@/api/mc/prompt'

export default {
  name: 'McPromptTemplate',
  data() {
    return {
      loading: false,
      templateList: [],
      open: false,
      viewOpen: false,
      viewData: {},
      title: '',
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        templateName: null,
        isActive: null
      },
      form: {},
      rules: {
        templateName: [{ required: true, message: '模板名称不能为空', trigger: 'blur' }],
        templateType: [{ required: true, message: '请选择模板类型', trigger: 'change' }],
        templateContent: [{ required: true, message: '模板内容不能为空', trigger: 'blur' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listPromptTemplate(this.queryParams).then(response => {
        this.templateList = response.rows
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
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '添加预提示词模板'
    },
    handleView(row) {
      this.viewData = Object.assign({}, row)
      this.viewOpen = true
    },
    handleUpdate(row) {
      this.reset()
      getPromptTemplate(row.templateId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改预提示词模板'
      })
    },
    handleDelete(row) {
      this.$confirm('是否确认删除该预提示词模板?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delPromptTemplate(row.templateId)
      }).then(() => {
        this.getList()
        this.$message.success('删除成功')
      })
    },
    handleActiveChange(row) {
      const text = row.isActive === '1' ? '启用' : '禁用'
      this.$confirm('确认要' + text + '该模板吗？启用后其他模板将自动禁用。', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return activatePromptTemplate(row.templateId)
      }).then(() => {
        this.$message.success(text + '成功')
        this.getList()
      }).catch(() => {
        row.isActive = row.isActive === '1' ? '0' : '1'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.templateId != null) {
            updatePromptTemplate(this.form).then(response => {
              this.$message.success('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addPromptTemplate(this.form).then(response => {
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
        templateId: null,
        templateName: null,
        templateType: 'standard',
        templateContent: null,
        isActive: '0',
        status: '0',
        description: null,
        remark: null
      }
      this.resetForm('form')
    }
  }
}
</script>

<style scoped>
.tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 5px;
}

.tip strong {
  color: #E6A23C;
}

.tip code {
  background-color: #f5f7fa;
  padding: 2px 6px;
  border-radius: 3px;
  color: #E6A23C;
  font-weight: bold;
}

pre {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
}
</style>
