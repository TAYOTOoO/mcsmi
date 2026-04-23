<template>
  <div class="app-container">
    <!-- 搜索 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true">
      <el-form-item label="公告标题">
        <el-input v-model="queryParams.title" placeholder="请输入公告标题" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="公告类型">
        <el-select v-model="queryParams.type" placeholder="请选择" clearable>
          <el-option label="通知" value="1" />
          <el-option label="公告" value="2" />
          <el-option label="更新" value="3" />
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
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增公告</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" icon="el-icon-delete" :disabled="multiple" @click="handleDelete">批量删除</el-button>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="announcementList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="公告ID" align="center" prop="announcementId" width="80" />
      <el-table-column label="公告标题" align="center" prop="title" :show-overflow-tooltip="true" width="200" />
      <el-table-column label="公告内容" align="center" prop="content" :show-overflow-tooltip="true" width="250" />
      <el-table-column label="公告类型" align="center" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.type === '1'" type="info">通知</el-tag>
          <el-tag v-else-if="scope.row.type === '2'" type="primary">公告</el-tag>
          <el-tag v-else-if="scope.row.type === '3'" type="success">更新</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="开始时间" align="center" prop="startTime" width="150">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime, '{y}-{m}-{d}') || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间" align="center" prop="endTime" width="150">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime, '{y}-{m}-{d}') || '-' }}</span>
        </template>
      </el-table-column>
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
      <el-table-column label="创建时间" align="center" prop="createTime" width="150">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
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
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="200" />
        </el-form-item>

        <el-form-item label="公告内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告内容" />
        </el-form-item>

        <el-form-item label="公告图片">
          <el-upload
            class="upload-demo"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :show-file-list="false"
            accept="image/*"
          >
            <el-button size="small" type="primary">点击上传图片</el-button>
            <div slot="tip" class="el-upload__tip">只能上传jpg/png格式文件，且不超过2MB</div>
          </el-upload>
          <div v-if="form.imageUrl" class="image-preview">
            <el-image
              :src="getImageUrl(form.imageUrl)"
              :preview-src-list="[getImageUrl(form.imageUrl)]"
              fit="contain"
              style="max-width: 300px; max-height: 200px;"
            ></el-image>
            <el-button type="danger" size="mini" @click="removeImage" style="margin-top: 10px;">移除图片</el-button>
          </div>
        </el-form-item>

        <el-form-item label="公告类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio label="1">通知</el-radio>
            <el-radio label="2">公告</el-radio>
            <el-radio label="3">更新</el-radio>
          </el-radio-group>
          <div class="tip">类型说明：通知-一般消息，公告-重要通告，更新-系统更新</div>
        </el-form-item>

        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%"
          />
          <div class="tip">不选择时间表示立即生效</div>
        </el-form-item>

        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%"
          />
          <div class="tip">不选择时间表示永久有效</div>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
          <div class="tip">停用的公告不会在用户端显示</div>
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
import { listAnnouncement, getAnnouncement, addAnnouncement, updateAnnouncement, delAnnouncement } from '@/api/mc/announcement'

export default {
  name: 'McAnnouncementManage',
  data() {
    return {
      loading: false,
      announcementList: [],
      ids: [],
      single: true,
      multiple: true,
      total: 0,
      open: false,
      title: '',
      uploadUrl: process.env.VUE_APP_BASE_API + "/common/upload",
      uploadHeaders: { Authorization: "Bearer " + this.getToken() },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: null,
        type: null,
        status: null
      },
      form: {},
      rules: {
        title: [{ required: true, message: '公告标题不能为空', trigger: 'blur' }],
        content: [{ required: true, message: '公告内容不能为空', trigger: 'blur' }],
        type: [{ required: true, message: '请选择公告类型', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listAnnouncement(this.queryParams).then(response => {
        this.announcementList = response.rows
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
      this.ids = selection.map(item => item.announcementId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '添加公告'
    },
    handleUpdate(row) {
      this.reset()
      const announcementId = row.announcementId || this.ids
      getAnnouncement(announcementId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改公告'
      })
    },
    handleDelete(row) {
      const announcementIds = row.announcementId ? [row.announcementId] : this.ids
      this.$confirm('是否确认删除选中的公告?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delAnnouncement(announcementIds)
      }).then(() => {
        this.getList()
        this.$message.success('删除成功')
      })
    },
    handleStatusChange(row) {
      updateAnnouncement(row).then(() => {
        this.$message.success('状态修改成功')
      }).catch(() => {
        row.status = row.status === '0' ? '1' : '0'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.announcementId != null) {
            updateAnnouncement(this.form).then(() => {
              this.$message.success('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addAnnouncement(this.form).then(() => {
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
        announcementId: null,
        title: null,
        content: null,
        type: '1',
        status: '0',
        startTime: null,
        endTime: null,
        remark: null
      }
      this.resetForm('form')
    },
    /** 获取Token */
    getToken() {
      return this.$store.getters.token
    },
    /** 文件上传前验证 */
    beforeUpload(file) {
      const isImage = file.type.indexOf('image/') === 0
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isImage) {
        this.$message.error('只能上传图片文件!')
        return false
      }
      if (!isLt2M) {
        this.$message.error('上传图片大小不能超过 2MB!')
        return false
      }
      return true
    },
    /** 上传成功回调 */
    handleUploadSuccess(response, file) {
      if (response.code === 200) {
        // 只保存相对路径，不保存完整URL，这样在开发和生产环境都能正常访问
        this.form.imageUrl = response.fileName
        this.$message.success('图片上传成功')
      } else {
        this.$message.error(response.msg || '图片上传失败')
      }
    },
    /** 上传失败回调 */
    handleUploadError(err, file) {
      this.$message.error('图片上传失败: ' + err)
    },
    /** 移除图片 */
    removeImage() {
      this.form.imageUrl = ''  // 改为空字符串，确保能触发后端更新
    },
    /** 获取图片完整URL */
    getImageUrl(path) {
      if (!path) return ''
      // 如果已经是完整URL，直接返回
      if (path.startsWith('http')) return path
      // 如果是相对路径，拼接API前缀
      return process.env.VUE_APP_BASE_API + path
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

.image-preview {
  margin-top: 10px;
  padding: 10px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  text-align: center;
}
</style>
