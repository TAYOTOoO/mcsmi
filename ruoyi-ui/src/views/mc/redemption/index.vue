<template>
  <div class="app-container">
    <!-- 查询表单 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="批次名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入批次名称"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable size="small">
          <el-option label="未使用" :value="1" />
          <el-option label="已使用" :value="2" />
          <el-option label="已过期" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-check"
          size="mini"
          @click="handleSelectAll"
        >全选</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-close"
          size="mini"
          @click="handleCancelAll"
        >取消全选</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleGenerate"
          v-hasPermi="['mc:redemption:generate']"
        >批量生成</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['mc:redemption:edit']"
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
          v-hasPermi="['mc:redemption:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          :disabled="multiple"
          @click="handleExport"
          v-hasPermi="['mc:redemption:export']"
        >导出TXT</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-alert
      v-if="ids.length > 0"
      :title="`已选中 ${ids.length} 个兑换码`"
      type="info"
      :closable="false"
      style="margin-bottom: 10px"
    />
    <el-table ref="redemptionTable" v-loading="loading" :data="redemptionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="批次名称" align="center" prop="name" width="150" />
      <el-table-column label="兑换码" align="center" prop="key" width="320" show-overflow-tooltip>
        <template slot-scope="scope">
          <span style="font-family: monospace;">{{ scope.row.key }}</span>
        </template>
      </el-table-column>
      <el-table-column label="额度" align="center" prop="quota" width="100" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status == 1" type="success">未使用</el-tag>
          <el-tag v-else-if="scope.row.status == 2" type="info">已使用</el-tag>
          <el-tag v-else-if="scope.row.status == 3" type="danger">已过期</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createdTime" width="180">
        <template slot-scope="scope">
          {{ parseTime(scope.row.createdTime) }}
        </template>
      </el-table-column>
      <el-table-column label="过期时间" align="center" prop="expiredTime" width="180">
        <template slot-scope="scope">
          {{ parseTime(scope.row.expiredTime) }}
        </template>
      </el-table-column>
      <el-table-column label="兑换用户ID" align="center" prop="usedUserId" width="120" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['mc:redemption:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['mc:redemption:remove']"
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

    <!-- 批量生成对话框 -->
    <el-dialog title="批量生成兑换码" :visible.sync="generateOpen" width="500px" append-to-body>
      <el-form ref="generateForm" :model="generateForm" :rules="generateRules" label-width="100px">
        <el-form-item label="批次名称" prop="name">
          <el-input v-model="generateForm.name" placeholder="请输入批次名称" />
        </el-form-item>
        <el-form-item label="生成数量" prop="count">
          <el-input-number v-model="generateForm.count" :min="1" :max="1000" controls-position="right" style="width: 100%;" />
          <span style="color: #999; font-size: 12px;">最多可生成1000个兑换码</span>
        </el-form-item>
        <el-form-item label="额度" prop="quota">
          <el-input-number v-model="generateForm.quota" :min="1" :max="100000" controls-position="right" style="width: 100%;" />
          <span style="color: #999; font-size: 12px;">每个兑换码的积分额度</span>
        </el-form-item>
        <el-form-item label="过期时间" prop="expiredTime">
          <el-date-picker
            v-model="generateForm.expiredTime"
            type="datetime"
            placeholder="选择过期时间"
            value-format="timestamp"
            style="width: 100%;"
          />
          <span style="color: #999; font-size: 12px;">不选择则永久有效</span>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="generateOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitGenerate">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 生成结果预览对话框 -->
    <el-dialog title="生成成功 - 兑换码列表" :visible.sync="exportPreviewOpen" width="800px" append-to-body>
      <el-alert
        title="提示"
        type="success"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <template slot>
          已成功生成 <strong>{{ generatedKeys.length }}</strong> 个兑换码，可以复制或导出为TXT文件
        </template>
      </el-alert>
      <el-input
        type="textarea"
        :rows="15"
        v-model="generatedKeysText"
        readonly
        style="font-family: monospace; font-size: 13px;"
      />
      <div slot="footer" class="dialog-footer">
        <el-button icon="el-icon-document-copy" @click="copyKeys">复制全部</el-button>
        <el-button type="primary" icon="el-icon-download" @click="downloadTxt">下载TXT</el-button>
      </div>
    </el-dialog>

    <!-- 修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="批次名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入批次名称" />
        </el-form-item>
        <el-form-item label="额度" prop="quota">
          <el-input-number v-model="form.quota" :min="1" :max="100000" controls-position="right" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="过期时间" prop="expiredTime">
          <el-date-picker
            v-model="form.expiredTime"
            type="datetime"
            placeholder="选择过期时间"
            value-format="timestamp"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listRedemption, getRedemption, addRedemption, updateRedemption, delRedemption, generateRedemptions, exportTxt, getAllRedemptionIds } from "@/api/mc/redemption";

export default {
  name: "Redemption",
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
      // 兑换码表格数据
      redemptionList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否显示生成对话框
      generateOpen: false,
      // 是否显示导出预览
      exportPreviewOpen: false,
      // 生成的兑换码列表
      generatedKeys: [],
      // 生成的兑换码文本
      generatedKeysText: '',
      // 是否正在执行全选操作
      isSelectingAll: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        status: null
      },
      // 批量生成表单
      generateForm: {
        name: '',
        count: 100,
        quota: 1000,
        expiredTime: null
      },
      // 批量生成表单校验
      generateRules: {
        name: [
          { required: true, message: "批次名称不能为空", trigger: "blur" }
        ],
        count: [
          { required: true, message: "生成数量不能为空", trigger: "blur" }
        ],
        quota: [
          { required: true, message: "额度不能为空", trigger: "blur" }
        ]
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [
          { required: true, message: "批次名称不能为空", trigger: "blur" }
        ],
        quota: [
          { required: true, message: "额度不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询兑换码列表 */
    getList() {
      this.loading = true;
      listRedemption(this.queryParams).then(response => {
        this.redemptionList = response.rows;
        this.total = response.total;
        this.loading = false;

        // 如果有已选中的ID，恢复选中状态
        this.$nextTick(() => {
          if (this.$refs.redemptionTable && this.ids.length > 0) {
            this.isSelectingAll = true; // 防止恢复选中状态时触发handleSelectionChange
            this.redemptionList.forEach(row => {
              if (this.ids.includes(row.id)) {
                this.$refs.redemptionTable.toggleRowSelection(row, true);
              }
            });
            setTimeout(() => {
              this.isSelectingAll = false;
            }, 100);
          }
        });
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
        id: null,
        name: null,
        quota: null,
        expiredTime: null
      };
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
      // 如果正在执行全选操作，不覆盖ids
      if (this.isSelectingAll) {
        return;
      }
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 全选所有兑换码 */
    handleSelectAll() {
      this.loading = true;
      this.isSelectingAll = true; // 设置标志，防止handleSelectionChange覆盖ids

      // 查询所有兑换码（使用当前筛选条件）
      getAllRedemptionIds(this.queryParams).then(response => {
        this.ids = response.rows.map(item => item.id);
        this.multiple = false;
        this.single = this.ids.length !== 1;
        this.loading = false;

        // 同时选中当前页可见的行
        this.$nextTick(() => {
          if (this.$refs.redemptionTable) {
            this.redemptionList.forEach(row => {
              if (this.ids.includes(row.id)) {
                this.$refs.redemptionTable.toggleRowSelection(row, true);
              }
            });
          }
          // 选中操作完成后，重置标志
          setTimeout(() => {
            this.isSelectingAll = false;
          }, 100);
        });

        this.$modal.msgSuccess(`已选中全部 ${this.ids.length} 个兑换码`);
      }).catch(() => {
        this.loading = false;
        this.isSelectingAll = false;
      });
    },
    /** 取消全选 */
    handleCancelAll() {
      this.isSelectingAll = false; // 重置标志
      this.ids = [];
      this.multiple = true;
      this.single = true;
      this.$refs.redemptionTable && this.$refs.redemptionTable.clearSelection();
      this.$modal.msgSuccess("已取消所有选择");
    },
    /** 批量生成按钮 */
    handleGenerate() {
      this.generateOpen = true;
      this.generateForm = {
        name: '',
        count: 100,
        quota: 1000,
        expiredTime: null
      };
      this.resetForm("generateForm");
    },
    /** 提交批量生成 */
    submitGenerate() {
      this.$refs["generateForm"].validate(valid => {
        if (valid) {
          generateRedemptions(this.generateForm).then(response => {
            this.generatedKeys = response.data;
            this.generatedKeysText = this.generatedKeys.join('\n');
            this.generateOpen = false;
            this.exportPreviewOpen = true;
            this.getList();
            this.$modal.msgSuccess("生成成功");
          });
        }
      });
    },
    /** 复制全部兑换码 */
    copyKeys() {
      const textarea = document.createElement('textarea');
      textarea.value = this.generatedKeysText;
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand('copy');
      document.body.removeChild(textarea);
      this.$modal.msgSuccess("复制成功");
    },
    /** 下载TXT文件 */
    downloadTxt() {
      const blob = new Blob([this.generatedKeysText], { type: 'text/plain;charset=utf-8' });
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = 'redemption_codes_' + new Date().getTime() + '.txt';
      link.click();
      URL.revokeObjectURL(link.href);
      this.$modal.msgSuccess("下载成功");
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids[0]
      getRedemption(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改兑换码";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateRedemption(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addRedemption(this.form).then(response => {
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
      const ids = row.id ? [row.id] : this.ids;
      this.$modal.confirm('是否确认删除选中的兑换码？').then(function() {
        return delRedemption(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出TXT */
    handleExport() {
      if (this.ids.length === 0) {
        this.$modal.msgWarning("请选择要导出的兑换码");
        return;
      }
      exportTxt(this.ids).then(response => {
        const blob = new Blob([response], { type: 'text/plain;charset=utf-8' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = 'redemption_codes_' + new Date().getTime() + '.txt';
        link.click();
        URL.revokeObjectURL(link.href);
        this.$modal.msgSuccess("导出成功");
      });
    },
    /** 时间戳转换 */
    parseTime(timestamp) {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      return date.toLocaleString('zh-CN', { hour12: false });
    }
  }
};
</script>
