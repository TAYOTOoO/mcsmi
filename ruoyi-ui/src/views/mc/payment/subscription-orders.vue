<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="订单号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="用户名" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入用户名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="订单状态" prop="paymentStatus">
        <el-select v-model="queryParams.paymentStatus" placeholder="请选择订单状态" clearable>
          <el-option label="待支付" :value="0" />
          <el-option label="已支付" :value="1" />
          <el-option label="已取消" :value="2" />
          <el-option label="已退款" :value="3" />
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
          v-hasPermi="['mc:subscriptionOrder:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['mc:subscriptionOrder:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="orderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="订单ID" align="center" prop="orderId" width="80" />
      <el-table-column label="订单号" align="center" prop="orderNo" width="180" :show-overflow-tooltip="true" />
      <el-table-column label="用户名" align="center" prop="userName" width="120" />
      <el-table-column label="订阅套餐" align="center" prop="packageName" width="150" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <el-tag type="warning">{{ scope.row.packageName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付金额" align="center" prop="actualAmount" width="100">
        <template slot-scope="scope">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ scope.row.actualAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="订阅时长" align="center" prop="durationMonths" width="100">
        <template slot-scope="scope">
          <el-tag type="info">{{ getDurationText(scope.row.durationMonths) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" align="center" prop="paymentMethod" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.paymentMethod === 'alipay'" type="primary">支付宝</el-tag>
          <el-tag v-else-if="scope.row.paymentMethod === 'wxpay'" type="success">微信</el-tag>
          <el-tag v-else-if="scope.row.paymentMethod === 'qqpay'" type="info">QQ钱包</el-tag>
          <el-tag v-else type="warning">{{ scope.row.paymentMethod || '未指定' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="订单状态" align="center" prop="paymentStatus" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.paymentStatus === 0" type="warning">待支付</el-tag>
          <el-tag v-else-if="scope.row.paymentStatus === 1" type="success">已支付</el-tag>
          <el-tag v-else-if="scope.row.paymentStatus === 2" type="info">已取消</el-tag>
          <el-tag v-else-if="scope.row.paymentStatus === 3" type="danger">已退款</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付时间" align="center" prop="paymentTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.paymentTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-hasPermi="['mc:subscriptionOrder:query']"
          >详情</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['mc:subscriptionOrder:remove']"
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
    <el-dialog title="订阅订单详情" :visible.sync="detailOpen" width="700px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单ID">{{ detail.orderId }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="支付平台订单号">{{ detail.tradeNo || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detail.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detail.userName }}</el-descriptions-item>
        <el-descriptions-item label="订阅套餐">
          <el-tag type="warning">{{ detail.packageName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="原价">¥{{ detail.originalAmount }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ detail.actualAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="订阅时长">
          <el-tag type="info">{{ getDurationText(detail.durationMonths) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">
          <el-tag v-if="detail.paymentMethod === 'alipay'" type="primary">支付宝</el-tag>
          <el-tag v-else-if="detail.paymentMethod === 'wxpay'" type="success">微信</el-tag>
          <el-tag v-else-if="detail.paymentMethod === 'qqpay'" type="info">QQ钱包</el-tag>
          <el-tag v-else type="warning">{{ detail.paymentMethod || '未指定' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag v-if="detail.paymentStatus === 0" type="warning">待支付</el-tag>
          <el-tag v-else-if="detail.paymentStatus === 1" type="success">已支付</el-tag>
          <el-tag v-else-if="detail.paymentStatus === 2" type="info">已取消</el-tag>
          <el-tag v-else-if="detail.paymentStatus === 3" type="danger">已退款</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ parseTime(detail.paymentTime) || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ parseTime(detail.startTime) || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="到期时间">{{ parseTime(detail.expireTime) || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listSubscriptionOrder, getSubscriptionOrder, delSubscriptionOrder } from "@/api/mc/subscriptionOrder";

export default {
  name: "SubscriptionOrder",
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
      // 订阅订单表格数据
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
        orderNo: null,
        userName: null,
        paymentStatus: null
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询订阅订单列表 */
    getList() {
      this.loading = true;
      listSubscriptionOrder(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
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
      getSubscriptionOrder(orderId).then(response => {
        this.detail = response.data;
        this.detailOpen = true;
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const orderIds = row.orderId || this.ids;
      this.$modal.confirm('是否确认删除订阅订单编号为"' + orderIds + '"的数据项？').then(function() {
        return delSubscriptionOrder(orderIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('mc/subscriptionOrder/export', {
        ...this.queryParams
      }, `subscription_order_${new Date().getTime()}.xlsx`)
    },
    /** 格式化订阅时长 */
    getDurationText(months) {
      if (!months) return '-';
      const map = { 1: '1个月', 3: '3个月', 6: '6个月', 12: '1年' };
      return map[months] || `${months}个月`;
    }
  }
};
</script>
