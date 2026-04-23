<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="订单号" prop="outTradeNo">
        <el-input
          v-model="queryParams.outTradeNo"
          placeholder="请输入商户订单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="queryParams.username"
          placeholder="请输入用户名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="订单类型" prop="orderType">
        <el-select v-model="queryParams.orderType" placeholder="请选择订单类型" clearable>
          <el-option label="积分充值" value="recharge" />
          <el-option label="订阅套餐" value="subscription" />
        </el-select>
      </el-form-item>
      <el-form-item label="订单状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择订单状态" clearable>
          <el-option label="待支付" value="0" />
          <el-option label="已支付" value="1" />
          <el-option label="已取消" value="2" />
          <el-option label="已退款" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['mc:paymentOrder:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['mc:paymentOrder:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="orderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="订单ID" align="center" prop="orderId" width="80" />
      <el-table-column label="商户订单号" align="center" prop="outTradeNo" width="180" :show-overflow-tooltip="true" />
      <el-table-column label="用户名" align="center" prop="username" width="120" />
      <el-table-column label="订单类型" align="center" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.orderType === 'subscription'" type="warning">订阅套餐</el-tag>
          <el-tag v-else type="success">积分充值</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="商品名称" align="center" prop="productName" width="150" :show-overflow-tooltip="true" />
      <el-table-column label="支付金额" align="center" prop="amount" width="100">
        <template slot-scope="scope">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ scope.row.amount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="积分/时长" align="center" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.orderType === 'subscription'" type="info">{{ getDurationText(scope.row.durationMonths) }}</el-tag>
          <el-tag v-else type="success">{{ scope.row.points }}积分</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" align="center" prop="paymentType" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.paymentType === 'alipay'" type="primary">支付宝</el-tag>
          <el-tag v-else-if="scope.row.paymentType === 'wxpay'" type="success">微信</el-tag>
          <el-tag v-else-if="scope.row.paymentType === 'qqpay'" type="info">QQ钱包</el-tag>
          <el-tag v-else type="warning">{{ scope.row.paymentType || '未指定' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="订单状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === '0'" type="warning">待支付</el-tag>
          <el-tag v-else-if="scope.row.status === '1'" type="success">已支付</el-tag>
          <el-tag v-else-if="scope.row.status === '2'" type="info">已取消</el-tag>
          <el-tag v-else-if="scope.row.status === '3'" type="danger">已退款</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付时间" align="center" prop="payTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.payTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-hasPermi="['mc:paymentOrder:query']"
          >详情</el-button>
          <el-button
            v-if="scope.row.status === '0' && scope.row.orderType !== 'subscription'"
            size="mini"
            type="text"
            icon="el-icon-refresh"
            @click="handleReconcile(scope.row)"
            v-hasPermi="['mc:paymentOrder:edit']"
          >补单</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['mc:paymentOrder:remove']"
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

    <!-- 订单详情对话框 -->
    <el-dialog title="订单详情" :visible.sync="detailOpen" width="700px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单ID">{{ detail.orderId }}</el-descriptions-item>
        <el-descriptions-item label="商户订单号">{{ detail.outTradeNo }}</el-descriptions-item>
        <el-descriptions-item label="支付平台订单号">{{ detail.tradeNo || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detail.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detail.username }}</el-descriptions-item>
        <el-descriptions-item label="订单类型">
          <el-tag v-if="detail.orderType === 'subscription'" type="warning">订阅套餐</el-tag>
          <el-tag v-else type="success">积分充值</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ detail.productName }}</el-descriptions-item>
        <el-descriptions-item label="支付金额">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ detail.amount }}</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.orderType === 'recharge'" label="充值积分">{{ detail.points }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.orderType === 'subscription'" label="订阅时长">{{ getDurationText(detail.durationMonths) }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">
          <el-tag v-if="detail.paymentType === 'alipay'" type="primary">支付宝</el-tag>
          <el-tag v-else-if="detail.paymentType === 'wxpay'" type="success">微信</el-tag>
          <el-tag v-else-if="detail.paymentType === 'qqpay'" type="info">QQ钱包</el-tag>
          <el-tag v-else type="warning">{{ detail.paymentType || '未指定' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag v-if="detail.status === '0'" type="warning">待支付</el-tag>
          <el-tag v-else-if="detail.status === '1'" type="success">已支付</el-tag>
          <el-tag v-else-if="detail.status === '2'" type="info">已取消</el-tag>
          <el-tag v-else-if="detail.status === '3'" type="danger">已退款</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="通知状态">
          <el-tag :type="detail.notifyStatus === '1' ? 'success' : 'info'">
            {{ detail.notifyStatus === '1' ? '已通知' : '未通知' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ parseTime(detail.payTime) || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="通知时间">{{ parseTime(detail.notifyTime) || '暂无' }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.orderType === 'subscription'" label="订阅开始时间">{{ parseTime(detail.startTime) || '暂无' }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.orderType === 'subscription'" label="订阅到期时间">{{ parseTime(detail.expireTime) || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="业务扩展参数" :span="2">{{ detail.param || '无' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPaymentOrder, getPaymentOrder, delPaymentOrder, exportPaymentOrder, reconcileOrder } from "@/api/mc/payment";

export default {
  name: "PaymentOrder",
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
      // 支付订单表格数据
      orderList: [],
      // 日期范围
      dateRange: [],
      // 详情对话框
      detailOpen: false,
      detail: {},
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        outTradeNo: null,
        username: null,
        orderType: null,
        status: null
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询支付订单列表 */
    getList() {
      this.loading = true;
      listPaymentOrder(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.orderList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.orderId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 查看详情按钮操作 */
    handleView(row) {
      const orderId = row.orderId;
      getPaymentOrder(orderId).then(response => {
        this.detail = response.data;
        this.detailOpen = true;
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const orderIds = row.orderId || this.ids;
      this.$modal.confirm('是否确认删除支付订单编号为"' + orderIds + '"的数据项？').then(function() {
        return delPaymentOrder(orderIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('mc/paymentOrder/export', {
        ...this.queryParams
      }, `payment_order_${new Date().getTime()}.xlsx`)
    },
    /** 格式化订阅时长 */
    getDurationText(months) {
      if (!months) return '-';
      const map = { 1: '1个月', 3: '3个月', 6: '6个月', 12: '1年' };
      return map[months] || `${months}个月`;
    },
    /** 补单按钮操作 */
    handleReconcile(row) {
      this.$modal.confirm('将向易支付查询订单"' + row.outTradeNo + '"的实际支付状态，如已支付将自动补充积分。是否继续？').then(() => {
        const loading = this.$loading({ lock: true, text: '正在查询易支付订单状态...', background: 'rgba(0, 0, 0, 0.7)' });
        return reconcileOrder(row.outTradeNo).then(response => {
          loading.close();
          this.$modal.msgSuccess(response.msg);
          this.getList();
        }).catch(err => {
          loading.close();
          // error message is shown automatically by request interceptor
        });
      }).catch(() => {});
    }
  }
};
</script>
