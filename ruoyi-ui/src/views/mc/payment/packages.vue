<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="套餐名称" prop="packageName">
        <el-input
          v-model="queryParams.packageName"
          placeholder="请输入套餐名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
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
          v-hasPermi="['mc:subscription:add']"
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
          v-hasPermi="['mc:subscription:edit']"
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
          v-hasPermi="['mc:subscription:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="packageList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="套餐ID" align="center" prop="packageId" width="80" />
      <el-table-column label="套餐名称" align="center" prop="packageName" width="150" />
      <el-table-column label="时长（月）" align="center" prop="durationMonths" width="100" />
      <el-table-column label="价格（元）" align="center" prop="amount" width="100">
        <template slot-scope="scope">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ scope.row.amount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="每月赠送积分" align="center" prop="monthlyPoints" width="120" />
      <el-table-column label="总赠送积分" align="center" width="120">
        <template slot-scope="scope">
          <span style="color: #67c23a;">{{ scope.row.monthlyPoints * scope.row.durationMonths }}</span>
        </template>
      </el-table-column>
      <el-table-column label="充值折扣率" align="center" prop="discountRate" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.discountRate" type="warning">{{ (scope.row.discountRate * 10).toFixed(1) }}折</el-tag>
          <span v-else>无折扣</span>
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sort" width="80" />
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
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" width="180" fixed="right" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['mc:subscription:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['mc:subscription:remove']"
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

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="套餐名称" prop="packageName">
          <el-input v-model="form.packageName" placeholder="请输入套餐名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="时长（月）" prop="durationMonths">
          <el-input-number v-model="form.durationMonths" :min="1" :max="999" placeholder="请输入时长" />
        </el-form-item>
        <el-form-item label="价格（元）" prop="amount">
          <el-input-number v-model="form.amount" :min="0.01" :max="99999.99" :precision="2" placeholder="请输入价格" />
        </el-form-item>
        <el-form-item label="每月赠送积分" prop="monthlyPoints">
          <el-input-number v-model="form.monthlyPoints" :min="0" :max="999999" placeholder="请输入每月赠送积分" />
          <div class="form-tip">总赠送积分：{{ (form.monthlyPoints || 0) * (form.durationMonths || 0) }}</div>
        </el-form-item>
        <el-form-item label="充值折扣率" prop="discountRate">
          <el-input-number v-model="form.discountRate" :min="0" :max="1" :step="0.01" :precision="2" placeholder="0.80表示8折" />
          <div class="form-tip">例如：0.80表示充值享受8折优惠</div>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" placeholder="请输入排序" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
import { listSubscriptionPackage, getSubscriptionPackage, addSubscriptionPackage, updateSubscriptionPackage, delSubscriptionPackage } from "@/api/mc/subscriptionPackage";

export default {
  name: "SubscriptionPackage",
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
      // 订阅套餐表格数据
      packageList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        packageName: null,
        status: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        packageName: [
          { required: true, message: "套餐名称不能为空", trigger: "blur" }
        ],
        durationMonths: [
          { required: true, message: "时长不能为空", trigger: "blur" }
        ],
        amount: [
          { required: true, message: "价格不能为空", trigger: "blur" }
        ],
        monthlyPoints: [
          { required: true, message: "每月赠送积分不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询订阅套餐列表 */
    getList() {
      this.loading = true;
      listSubscriptionPackage(this.queryParams).then(response => {
        this.packageList = response.rows;
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
        packageId: null,
        packageName: null,
        durationMonths: 1,
        amount: null,
        monthlyPoints: 0,
        discountRate: null,
        sort: 0,
        status: "0",
        remark: null
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
      this.ids = selection.map(item => item.packageId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加订阅套餐";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const packageId = row.packageId || this.ids
      getSubscriptionPackage(packageId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改订阅套餐";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.packageId != null) {
            updateSubscriptionPackage(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addSubscriptionPackage(this.form).then(response => {
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
      const packageIds = row.packageId || this.ids;
      this.$modal.confirm('是否确认删除订阅套餐编号为"' + packageIds + '"的数据项？').then(function() {
        return delSubscriptionPackage(packageIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 状态修改 */
    handleStatusChange(row) {
      let text = row.status === "0" ? "启用" : "停用";
      this.$modal.confirm('确认要"' + text + '""' + row.packageName + '"套餐吗？').then(function() {
        return updateSubscriptionPackage(row);
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(function() {
        row.status = row.status === "0" ? "1" : "0";
      });
    }
  }
};
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
</style>
