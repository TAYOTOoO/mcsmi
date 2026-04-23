<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="配置名称" prop="configName">
        <el-input
          v-model="queryParams.configName"
          placeholder="请输入配置名称"
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
          v-hasPermi="['mc:paymentConfig:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['mc:paymentConfig:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="配置ID" align="center" prop="configId" width="80" />
      <el-table-column label="配置名称" align="center" prop="configName" />
      <el-table-column label="商户ID" align="center" prop="merchantId" width="120" />
      <el-table-column label="支付API地址" align="center" prop="apiUrl" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">
            {{ scope.row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="是否默认" align="center" prop="isDefault" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isDefault === '1' ? 'success' : 'info'">
            {{ scope.row.isDefault === '1' ? '默认' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="300">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['mc:paymentConfig:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['mc:paymentConfig:remove']"
          >删除</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-coin"
            @click="handleTestPayment(scope.row)"
            v-hasPermi="['mc:paymentConfig:edit']"
          >测试支付</el-button>
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

    <!-- 添加或修改支付配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="form.configName" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="支付API地址" prop="apiUrl">
          <el-input v-model="form.apiUrl" placeholder="请输入支付API地址，如：https://pay.example.com/submit.php" />
        </el-form-item>
        <el-form-item label="商户ID" prop="merchantId">
          <el-input v-model="form.merchantId" placeholder="请输入商户ID（PID）" />
        </el-form-item>
        <el-form-item label="商户密钥" prop="merchantKey">
          <el-input v-model="form.merchantKey" type="textarea" :rows="3" placeholder="请输入商户密钥（KEY）" />
        </el-form-item>
        <el-form-item label="异步通知地址" prop="notifyUrl">
          <el-input v-model="form.notifyUrl" placeholder="服务器异步通知地址，如：http://your-domain.com/mc/paymentOrder/notify" />
        </el-form-item>
        <el-form-item label="同步返回地址" prop="returnUrl">
          <el-input v-model="form.returnUrl" placeholder="页面跳转同步通知地址，如：http://your-domain.com/recharge/success" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否默认" prop="isDefault">
          <el-radio-group v-model="form.isDefault">
            <el-radio label="1">是</el-radio>
            <el-radio label="0">否</el-radio>
          </el-radio-group>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            设置为默认后，其他配置的默认状态将自动取消
          </div>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 测试支付对话框 -->
    <el-dialog title="测试支付" :visible.sync="testPaymentOpen" width="600px" append-to-body>
      <el-form ref="testForm" :model="testForm" :rules="testRules" label-width="100px">
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="testForm.productName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="支付金额" prop="amount">
          <el-input v-model="testForm.amount" placeholder="请输入支付金额（元）">
            <template slot="append">元</template>
          </el-input>
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentType">
          <el-select v-model="testForm.paymentType" placeholder="请选择支付方式">
            <el-option label="不指定（跳转收银台）" value="" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="微信支付" value="wxpay" />
            <el-option label="QQ钱包" value="qqpay" />
          </el-select>
        </el-form-item>
        <el-form-item label="积分数量" prop="points">
          <el-input v-model="testForm.points" placeholder="请输入充值积分数">
            <template slot="append">积分</template>
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitTestPayment" :loading="testPaymentLoading">
          {{ testPaymentLoading ? '正在跳转...' : '立即支付' }}
        </el-button>
        <el-button @click="testPaymentOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPaymentConfig, getPaymentConfig, delPaymentConfig, addPaymentConfig, updatePaymentConfig, testPayment } from "@/api/mc/payment";

export default {
  name: "PaymentConfig",
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
      // 支付配置表格数据
      configList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        configName: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        configName: [
          { required: true, message: "配置名称不能为空", trigger: "blur" }
        ],
        apiUrl: [
          { required: true, message: "支付API地址不能为空", trigger: "blur" }
        ],
        merchantId: [
          { required: true, message: "商户ID不能为空", trigger: "blur" }
        ],
        merchantKey: [
          { required: true, message: "商户密钥不能为空", trigger: "blur" }
        ]
      },
      // 测试支付对话框
      testPaymentOpen: false,
      testPaymentLoading: false,
      currentConfigId: null,
      testForm: {
        productName: '测试充值',
        amount: '0.01',
        paymentType: '',
        points: 100
      },
      testRules: {
        productName: [
          { required: true, message: "商品名称不能为空", trigger: "blur" }
        ],
        amount: [
          { required: true, message: "支付金额不能为空", trigger: "blur" },
          { pattern: /^(0|[1-9]\d*)(\.\d{1,2})?$/, message: "请输入正确的金额格式", trigger: "blur" }
        ],
        points: [
          { required: true, message: "积分数量不能为空", trigger: "blur" },
          { pattern: /^[1-9]\d*$/, message: "请输入正确的积分数量", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询支付配置列表 */
    getList() {
      this.loading = true;
      listPaymentConfig(this.queryParams).then(response => {
        this.configList = response.rows;
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
        configId: null,
        configName: null,
        apiUrl: null,
        merchantId: null,
        merchantKey: null,
        notifyUrl: null,
        returnUrl: null,
        status: "0",
        isDefault: "0",
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
      this.ids = selection.map(item => item.configId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加支付配置";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const configId = row.configId || this.ids
      getPaymentConfig(configId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改支付配置";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.configId != null) {
            updatePaymentConfig(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addPaymentConfig(this.form).then(response => {
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
      const configIds = row.configId || this.ids;
      this.$modal.confirm('是否确认删除支付配置编号为"' + configIds + '"的数据项？').then(function() {
        return delPaymentConfig(configIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 测试支付按钮操作 */
    handleTestPayment(row) {
      this.currentConfigId = row.configId;
      this.testForm = {
        productName: '测试充值',
        amount: '0.01',
        paymentType: '',
        points: 100,
        configId: row.configId
      };
      this.testPaymentOpen = true;
    },
    /** 提交测试支付 */
    submitTestPayment() {
      this.$refs["testForm"].validate(valid => {
        if (valid) {
          this.testPaymentLoading = true;
          this.testForm.configId = this.currentConfigId;
          testPayment(this.testForm).then(response => {
            if (response.code === 200) {
              // 创建临时容器并插入HTML
              const div = document.createElement('div');
              div.style.display = 'none';
              document.body.appendChild(div);
              div.innerHTML = response.data;

              // 查找并提交表单
              const form = div.querySelector('form');
              if (form) {
                this.$modal.msgSuccess("正在跳转到支付页面...");
                this.testPaymentOpen = false;
                this.testPaymentLoading = false;

                // 延迟提交确保对话框关闭动画完成
                setTimeout(() => {
                  form.submit();
                }, 500);
              } else {
                this.$modal.msgError("支付表单生成失败");
                this.testPaymentLoading = false;
              }
            } else {
              this.$modal.msgError(response.msg || "创建测试订单失败");
              this.testPaymentLoading = false;
            }
          }).catch(() => {
            this.testPaymentLoading = false;
          });
        }
      });
    }
  }
};
</script>
