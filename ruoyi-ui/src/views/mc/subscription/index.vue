<template>
  <div class="app-container">
    <el-tabs v-model="activeName" type="border-card">
      <!-- 订阅套餐管理 Tab -->
      <el-tab-pane label="订阅套餐管理" name="package">
        <el-form :model="packageQueryParams" ref="packageQueryForm" :inline="true" v-show="showSearch" label-width="68px">
          <el-form-item label="套餐名称" prop="packageName">
            <el-input
              v-model="packageQueryParams.packageName"
              placeholder="请输入套餐名称"
              clearable
              size="small"
              @keyup.enter.native="handlePackageQuery"
            />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="packageQueryParams.status" placeholder="请选择状态" clearable size="small">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handlePackageQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetPackageQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button
              type="primary"
              plain
              icon="el-icon-plus"
              size="mini"
              @click="handlePackageAdd"
              v-hasPermi="['mc:subscription:add']"
            >新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="success"
              plain
              icon="el-icon-edit"
              size="mini"
              :disabled="packageSingle"
              @click="handlePackageUpdate"
              v-hasPermi="['mc:subscription:edit']"
            >修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="danger"
              plain
              icon="el-icon-delete"
              size="mini"
              :disabled="packageMultiple"
              @click="handlePackageDelete"
              v-hasPermi="['mc:subscription:remove']"
            >删除</el-button>
          </el-col>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getPackageList"></right-toolbar>
        </el-row>

        <el-table v-loading="packageLoading" :data="packageList" @selection-change="handlePackageSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="套餐ID" align="center" prop="packageId" width="80" />
          <el-table-column label="套餐名称" align="center" prop="packageName" :show-overflow-tooltip="true" />
          <el-table-column label="时长(月)" align="center" prop="durationMonths" width="100" />
          <el-table-column label="价格" align="center" prop="price" width="120">
            <template slot-scope="scope">
              ¥{{ scope.row.price }}
            </template>
          </el-table-column>
          <el-table-column label="每月积分" align="center" prop="pointsPerMonth" width="100" />
          <el-table-column label="折扣率" align="center" prop="discountRate" width="100">
            <template slot-scope="scope">
              {{ scope.row.discountRate }}%
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" prop="status" width="100">
            <template slot-scope="scope">
              <el-switch
                v-model="scope.row.status"
                active-value="0"
                inactive-value="1"
                @change="handlePackageStatusChange(scope.row)"
              ></el-switch>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" align="center" prop="createTime" width="160">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
            <template slot-scope="scope">
              <el-button
                size="mini"
                type="text"
                icon="el-icon-edit"
                @click="handlePackageUpdate(scope.row)"
                v-hasPermi="['mc:subscription:edit']"
              >修改</el-button>
              <el-button
                size="mini"
                type="text"
                icon="el-icon-delete"
                @click="handlePackageDelete(scope.row)"
                v-hasPermi="['mc:subscription:remove']"
              >删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination
          v-show="packageTotal > 0"
          :total="packageTotal"
          :page.sync="packageQueryParams.pageNum"
          :limit.sync="packageQueryParams.pageSize"
          @pagination="getPackageList"
        />
      </el-tab-pane>

      <!-- 订阅订单管理 Tab -->
      <el-tab-pane label="订阅订单管理" name="order">
        <el-form :model="orderQueryParams" ref="orderQueryForm" :inline="true" v-show="showSearch" label-width="68px">
          <el-form-item label="用户名" prop="userName">
            <el-input
              v-model="orderQueryParams.userName"
              placeholder="请输入用户名"
              clearable
              size="small"
              @keyup.enter.native="handleOrderQuery"
            />
          </el-form-item>
          <el-form-item label="订单号" prop="orderNo">
            <el-input
              v-model="orderQueryParams.orderNo"
              placeholder="请输入订单号"
              clearable
              size="small"
              @keyup.enter.native="handleOrderQuery"
            />
          </el-form-item>
          <el-form-item label="支付状态" prop="payStatus">
            <el-select v-model="orderQueryParams.payStatus" placeholder="请选择支付状态" clearable size="small">
              <el-option label="待支付" value="0" />
              <el-option label="已支付" value="1" />
              <el-option label="已取消" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleOrderQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetOrderQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getOrderList"></right-toolbar>
        </el-row>

        <el-table v-loading="orderLoading" :data="orderList">
          <el-table-column label="订单ID" align="center" prop="orderId" width="80" />
          <el-table-column label="订单号" align="center" prop="orderNo" width="200" :show-overflow-tooltip="true" />
          <el-table-column label="用户名" align="center" prop="userName" width="120" />
          <el-table-column label="套餐名称" align="center" prop="packageName" :show-overflow-tooltip="true" />
          <el-table-column label="实付金额" align="center" prop="paidAmount" width="120">
            <template slot-scope="scope">
              ¥{{ scope.row.paidAmount }}
            </template>
          </el-table-column>
          <el-table-column label="支付状态" align="center" prop="payStatus" width="100">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.payStatus == 0" type="warning">待支付</el-tag>
              <el-tag v-else-if="scope.row.payStatus == 1" type="success">已支付</el-tag>
              <el-tag v-else-if="scope.row.payStatus == 2" type="info">已取消</el-tag>
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
          <el-table-column label="操作" align="center" width="100" class-name="small-padding fixed-width">
            <template slot-scope="scope">
              <el-button
                size="mini"
                type="text"
                icon="el-icon-view"
                @click="handleOrderView(scope.row)"
              >查看</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination
          v-show="orderTotal > 0"
          :total="orderTotal"
          :page.sync="orderQueryParams.pageNum"
          :limit.sync="orderQueryParams.pageSize"
          @pagination="getOrderList"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 添加或修改套餐对话框 -->
    <el-dialog :title="packageTitle" :visible.sync="packageOpen" width="600px" append-to-body>
      <el-form ref="packageForm" :model="packageForm" :rules="packageRules" label-width="100px">
        <el-form-item label="套餐名称" prop="packageName">
          <el-input v-model="packageForm.packageName" placeholder="请输入套餐名称" />
        </el-form-item>
        <el-form-item label="时长(月)" prop="durationMonths">
          <el-input-number v-model="packageForm.durationMonths" :min="1" :max="120" placeholder="请输入时长" style="width: 100%" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="packageForm.price" :min="0" :precision="2" :step="0.01" placeholder="请输入价格" style="width: 100%" />
        </el-form-item>
        <el-form-item label="每月积分" prop="pointsPerMonth">
          <el-input-number v-model="packageForm.pointsPerMonth" :min="0" :step="100" placeholder="请输入每月积分" style="width: 100%" />
        </el-form-item>
        <el-form-item label="折扣率" prop="discountRate">
          <el-input-number v-model="packageForm.discountRate" :min="0" :max="100" :precision="1" placeholder="请输入折扣率" style="width: 100%" />
          <span style="color: #999; font-size: 12px; margin-left: 10px;">100%为无折扣,90%为9折</span>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="packageForm.description" type="textarea" :rows="3" placeholder="请输入套餐描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="packageForm.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitPackageForm">确 定</el-button>
        <el-button @click="cancelPackage">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 订单详情对话框 -->
    <el-dialog title="订单详情" :visible.sync="orderDetailOpen" width="600px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ orderDetail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ orderDetail.userName }}</el-descriptions-item>
        <el-descriptions-item label="套餐名称">{{ orderDetail.packageName }}</el-descriptions-item>
        <el-descriptions-item label="时长(月)">{{ orderDetail.durationMonths }}</el-descriptions-item>
        <el-descriptions-item label="原价">¥{{ orderDetail.originalPrice }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">¥{{ orderDetail.paidAmount }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag v-if="orderDetail.payStatus == 0" type="warning">待支付</el-tag>
          <el-tag v-else-if="orderDetail.payStatus == 1" type="success">已支付</el-tag>
          <el-tag v-else-if="orderDetail.payStatus == 2" type="info">已取消</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ orderDetail.paymentMethod }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(orderDetail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ parseTime(orderDetail.payTime) }}</el-descriptions-item>
        <el-descriptions-item label="订阅开始时间">{{ parseTime(orderDetail.subscriptionStartTime) }}</el-descriptions-item>
        <el-descriptions-item label="订阅结束时间">{{ parseTime(orderDetail.subscriptionEndTime) }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="orderDetailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPackage, getPackage, addPackage, updatePackage, delPackage, listOrder, getOrder } from "@/api/mc/subscription"

export default {
  name: "Subscription",
  data() {
    return {
      // 当前激活的Tab
      activeName: "package",
      // 遮罩层
      packageLoading: true,
      orderLoading: true,
      // 选中数组
      packageIds: [],
      // 非单个禁用
      packageSingle: true,
      // 非多个禁用
      packageMultiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      packageTotal: 0,
      orderTotal: 0,
      // 表格数据
      packageList: [],
      orderList: [],
      // 弹出层标题
      packageTitle: "",
      // 是否显示弹出层
      packageOpen: false,
      orderDetailOpen: false,
      // 查询参数
      packageQueryParams: {
        pageNum: 1,
        pageSize: 10,
        packageName: undefined,
        status: undefined
      },
      orderQueryParams: {
        pageNum: 1,
        pageSize: 10,
        userName: undefined,
        orderNo: undefined,
        payStatus: undefined
      },
      // 表单参数
      packageForm: {},
      orderDetail: {},
      // 表单校验
      packageRules: {
        packageName: [
          { required: true, message: "套餐名称不能为空", trigger: "blur" }
        ],
        durationMonths: [
          { required: true, message: "时长不能为空", trigger: "blur" }
        ],
        price: [
          { required: true, message: "价格不能为空", trigger: "blur" }
        ],
        pointsPerMonth: [
          { required: true, message: "每月积分不能为空", trigger: "blur" }
        ],
        discountRate: [
          { required: true, message: "折扣率不能为空", trigger: "blur" }
        ]
      }
    }
  },
  watch: {
    activeName(val) {
      if (val === 'package') {
        this.getPackageList()
      } else if (val === 'order') {
        this.getOrderList()
      }
    }
  },
  created() {
    this.getPackageList()
  },
  methods: {
    /** 查询套餐列表 */
    getPackageList() {
      this.packageLoading = true
      listPackage(this.packageQueryParams).then(response => {
        this.packageList = response.rows
        this.packageTotal = response.total
        this.packageLoading = false
      })
    },
    /** 查询订单列表 */
    getOrderList() {
      this.orderLoading = true
      listOrder(this.orderQueryParams).then(response => {
        this.orderList = response.rows
        this.orderTotal = response.total
        this.orderLoading = false
      })
    },
    // 取消按钮
    cancelPackage() {
      this.packageOpen = false
      this.resetPackageForm()
    },
    // 表单重置
    resetPackageForm() {
      this.packageForm = {
        packageId: undefined,
        packageName: undefined,
        durationMonths: 1,
        price: undefined,
        pointsPerMonth: undefined,
        discountRate: 100,
        description: undefined,
        status: "0"
      }
      this.resetForm("packageForm")
    },
    /** 搜索按钮操作 */
    handlePackageQuery() {
      this.packageQueryParams.pageNum = 1
      this.getPackageList()
    },
    handleOrderQuery() {
      this.orderQueryParams.pageNum = 1
      this.getOrderList()
    },
    /** 重置按钮操作 */
    resetPackageQuery() {
      this.resetForm("packageQueryForm")
      this.handlePackageQuery()
    },
    resetOrderQuery() {
      this.resetForm("orderQueryForm")
      this.handleOrderQuery()
    },
    // 多选框选中数据
    handlePackageSelectionChange(selection) {
      this.packageIds = selection.map(item => item.packageId)
      this.packageSingle = selection.length !== 1
      this.packageMultiple = !selection.length
    },
    // 套餐状态修改
    handlePackageStatusChange(row) {
      let text = row.status === "0" ? "启用" : "停用"
      this.$modal.confirm('确认要"' + text + '""' + row.packageName + '"套餐吗？').then(function() {
        return updatePackage(row)
      }).then(() => {
        this.$modal.msgSuccess(text + "成功")
      }).catch(function() {
        row.status = row.status === "0" ? "1" : "0"
      })
    },
    /** 新增按钮操作 */
    handlePackageAdd() {
      this.resetPackageForm()
      this.packageOpen = true
      this.packageTitle = "添加订阅套餐"
    },
    /** 修改按钮操作 */
    handlePackageUpdate(row) {
      this.resetPackageForm()
      const packageId = row.packageId || this.packageIds
      getPackage(packageId).then(response => {
        this.packageForm = response.data
        this.packageOpen = true
        this.packageTitle = "修改订阅套餐"
      })
    },
    /** 提交按钮 */
    submitPackageForm() {
      this.$refs["packageForm"].validate(valid => {
        if (valid) {
          if (this.packageForm.packageId != undefined) {
            updatePackage(this.packageForm).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.packageOpen = false
              this.getPackageList()
            })
          } else {
            addPackage(this.packageForm).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.packageOpen = false
              this.getPackageList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handlePackageDelete(row) {
      const packageIds = row.packageId || this.packageIds
      this.$modal.confirm('是否确认删除订阅套餐编号为"' + packageIds + '"的数据项？').then(function() {
        return delPackage(packageIds)
      }).then(() => {
        this.getPackageList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 查看订单详情 */
    handleOrderView(row) {
      getOrder(row.orderId).then(response => {
        this.orderDetail = response.data
        this.orderDetailOpen = true
      })
    }
  }
}
</script>
