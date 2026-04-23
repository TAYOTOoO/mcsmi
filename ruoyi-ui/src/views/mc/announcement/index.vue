<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="公告标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入公告标题"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择类型" clearable size="small">
          <el-option label="通知" value="1" />
          <el-option label="公告" value="2" />
          <el-option label="更新" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable size="small">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['mc:announcement:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['mc:announcement:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['mc:announcement:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="announcementList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="announcementId" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="类型" align="center" prop="type">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.type == 1" type="info">通知</el-tag>
          <el-tag v-else-if="scope.row.type == 2" type="primary">公告</el-tag>
          <el-tag v-else-if="scope.row.type == 3" type="success">更新</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="开始时间" align="center" prop="startTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间" align="center" prop="endTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['mc:announcement:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['mc:announcement:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改公告对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio label="1">通知</el-radio>
            <el-radio label="2">公告</el-radio>
            <el-radio label="3">更新</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" placeholder="请输入内容" :rows="10" />
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
        <el-form-item label="有效期">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd HH:mm:ss"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
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
import { listAnnouncement, getAnnouncement, delAnnouncement, addAnnouncement, updateAnnouncement } from "@/api/mc/announcement";

export default {
  name: "Announcement",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 公告表格数据
      announcementList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 日期范围
      dateRange: [],
      // 上传地址
      uploadUrl: process.env.VUE_APP_BASE_API + "/common/upload",
      // 上传请求头
      uploadHeaders: { Authorization: "Bearer " + this.getToken() },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: null,
        type: null,
        status: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        title: [
          { required: true, message: "公告标题不能为空", trigger: "blur" }
        ],
        type: [
          { required: true, message: "公告类型不能为空", trigger: "change" }
        ],
        content: [
          { required: true, message: "公告内容不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询公告列表 */
    getList() {
      this.loading = true;
      listAnnouncement(this.queryParams).then(response => {
        this.announcementList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        announcementId: null,
        title: null,
        content: null,
        type: "1",
        status: "0",
        startTime: null,
        endTime: null
      };
      this.dateRange = [];
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.announcementId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加公告";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const announcementId = row.announcementId || this.ids
      getAnnouncement(announcementId).then(response => {
        this.form = response.data;
        if (this.form.startTime && this.form.endTime) {
          this.dateRange = [this.form.startTime, this.form.endTime]
        }
        this.open = true;
        this.title = "修改公告";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.dateRange && this.dateRange.length === 2) {
            this.form.startTime = this.dateRange[0];
            this.form.endTime = this.dateRange[1];
          }
          if (this.form.announcementId != null) {
            updateAnnouncement(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addAnnouncement(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const announcementIds = row.announcementId || this.ids;
      this.$modal.confirm('是否确认删除公告编号为"' + announcementIds + '"的数据项？').then(function() {
        return delAnnouncement(announcementIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleStatusChange(row) {
      let text = row.status === "0" ? "启用" : "停用";
      this.$modal.confirm('确认要"' + text + '""' + row.title + '"公告吗？').then(function() {
        return updateAnnouncement(row);
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(function() {
        row.status = row.status === "0" ? "1" : "0";
      });
    },
    /** 获取Token */
    getToken() {
      return this.$store.getters.token;
    },
    /** 文件上传前验证 */
    beforeUpload(file) {
      const isImage = file.type.indexOf('image/') === 0;
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isImage) {
        this.$modal.msgError('只能上传图片文件!');
        return false;
      }
      if (!isLt2M) {
        this.$modal.msgError('上传图片大小不能超过 2MB!');
        return false;
      }
      return true;
    },
    /** 上传成功回调 */
    handleUploadSuccess(response, file) {
      if (response.code === 200) {
        // 只保存相对路径，不保存完整URL，这样在开发和生产环境都能正常访问
        this.form.imageUrl = response.fileName;
        this.$modal.msgSuccess('图片上传成功');
      } else {
        this.$modal.msgError(response.msg || '图片上传失败');
      }
    },
    /** 上传失败回调 */
    handleUploadError(err, file) {
      this.$modal.msgError('图片上传失败: ' + err);
    },
    /** 移除图片 */
    removeImage() {
      this.form.imageUrl = '';  // 改为空字符串，确保能触发后端更新
    },
    /** 获取图片完整URL */
    getImageUrl(path) {
      if (!path) return '';
      // 如果已经是完整URL，直接返回
      if (path.startsWith('http')) return path;
      // 如果是相对路径，拼接API前缀
      return process.env.VUE_APP_BASE_API + path;
    }
  }
};
</script>

<style scoped>
.image-preview {
  margin-top: 10px;
  padding: 10px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  text-align: center;
}
</style>
