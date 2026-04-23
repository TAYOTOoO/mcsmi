<template>
  <div class="app-container">
    <!-- 搜索 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true">
      <el-form-item label="用户名称">
        <el-input v-model="queryParams.userName" placeholder="请输入用户名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号码">
        <el-input v-model="queryParams.phonenumber" placeholder="请输入手机号码" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="用户状态" clearable>
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
        <el-button type="warning" icon="el-icon-coin" @click="handleInitPoints">初始化积分</el-button>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="userList">
      <el-table-column label="用户ID" align="center" prop="userId" width="80" />
      <el-table-column label="用户名" align="center" prop="userName" width="120" />
      <el-table-column label="昵称" align="center" prop="nickName" width="120" />
      <el-table-column label="手机号码" align="center" prop="phonenumber" width="120" />
      <el-table-column label="总积分" align="center" width="100">
        <template slot-scope="scope">
          <el-tag type="success">{{ scope.row.params.totalPoints || 0 }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="已用积分" align="center" width="100">
        <template slot-scope="scope">
          <el-tag type="warning">{{ scope.row.params.usedPoints || 0 }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="剩余积分" align="center" width="100">
        <template slot-scope="scope">
          <el-tag type="info">{{ scope.row.params.remainingPoints || 0 }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="订阅状态" align="center" width="150">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.subscriptionType === 'diamond'" type="warning">{{ (scope.row.params && scope.row.params.packageName) || '💎 钻石订阅' }}</el-tag>
          <el-tag v-else type="info">普通用户</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="到期时间" align="center" width="160">
        <template slot-scope="scope">
          <span v-if="scope.row.subscriptionType === 'diamond'">
            {{ getExpireTimeDisplay(scope.row.subscriptionExpireTime) }}
          </span>
          <span v-else>-</span>
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
      <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-coin" @click="handleEditPoints(scope.row)">修改积分</el-button>
          <el-button size="mini" type="text" icon="el-icon-star-off" @click="handleEditSubscription(scope.row)">编辑订阅</el-button>
          <el-button size="mini" type="text" icon="el-icon-key" @click="handleResetPwd(scope.row)">重置密码</el-button>
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

    <!-- 修改积分对话框 -->
    <el-dialog title="修改用户积分" :visible.sync="pointsOpen" width="500px" append-to-body>
      <el-form ref="pointsForm" :model="pointsForm" :rules="pointsRules" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="pointsForm.userName" disabled />
        </el-form-item>
        <el-form-item label="当前总积分">
          <el-input v-model="currentTotalPoints" disabled>
            <template slot="append">积分</template>
          </el-input>
        </el-form-item>
        <el-form-item label="新总积分" prop="totalPoints">
          <el-input-number v-model="pointsForm.totalPoints" :min="0" :max="999999" controls-position="right" style="width: 100%;" />
          <div class="tip">设置用户的总积分数量（已使用的积分不会改变）</div>
        </el-form-item>
        <el-form-item label="预计剩余">
          <el-input :value="calculateRemaining" disabled>
            <template slot="append">积分</template>
          </el-input>
          <div class="tip">剩余积分 = 总积分 - 已使用积分</div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitPoints">确 定</el-button>
        <el-button @click="cancelPoints">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog title="重置密码" :visible.sync="resetPwdOpen" width="500px" append-to-body>
      <el-form ref="resetPwdForm" :model="resetPwdForm" :rules="resetPwdRules" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="resetPwdForm.userName" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="resetPwdForm.password" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitResetPwd">确 定</el-button>
        <el-button @click="cancelResetPwd">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 编辑订阅对话框 -->
    <el-dialog title="编辑用户订阅" :visible.sync="subscriptionOpen" width="600px" append-to-body>
      <el-form ref="subscriptionForm" :model="subscriptionForm" :rules="subscriptionRules" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="subscriptionForm.userName" disabled />
        </el-form-item>
        <el-form-item label="订阅类型" prop="subscriptionType">
          <el-select v-model="subscriptionForm.subscriptionType" placeholder="请选择订阅类型" style="width: 100%;" @change="handleSubscriptionTypeChange">
            <el-option label="普通用户" value="normal" />
            <el-option label="钻石订阅用户" value="diamond" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="subscriptionForm.subscriptionType === 'diamond'" label="订阅套餐" prop="packageId">
          <el-select v-model="subscriptionForm.packageId" placeholder="请选择套餐" style="width: 100%;">
            <el-option
              v-for="pkg in subscriptionPackages"
              :key="pkg.packageId"
              :label="pkg.packageName"
              :value="pkg.packageId">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-if="subscriptionForm.subscriptionType === 'diamond'" label="永久订阅">
          <el-checkbox v-model="subscriptionForm.isPermanent" @change="handlePermanentChange">设为永久订阅</el-checkbox>
        </el-form-item>
        <el-form-item v-if="subscriptionForm.subscriptionType === 'diamond' && !subscriptionForm.isPermanent" label="到期时间" prop="expireTime">
          <el-date-picker
            v-model="subscriptionForm.expireTime"
            type="datetime"
            placeholder="选择到期时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%;">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitSubscription">确 定</el-button>
        <el-button @click="cancelSubscription">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listUser, updateUserPoints, updateUserStatus, resetUserPwd, initUserPoints } from '@/api/mc/user'
import { updateUserSubscription } from '@/api/mc/subscription'
import { listSubscriptionPackage } from '@/api/mc/subscriptionPackage'

export default {
  name: 'McUserManage',
  data() {
    return {
      loading: false,
      userList: [],
      total: 0,
      pointsOpen: false,
      resetPwdOpen: false,
      subscriptionOpen: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userName: null,
        phonenumber: null,
        status: null
      },
      pointsForm: {},
      currentTotalPoints: 0,
      currentUsedPoints: 0,
      resetPwdForm: {},
      subscriptionForm: {},
      subscriptionPackages: [],
      pointsRules: {
        totalPoints: [
          { required: true, message: '总积分不能为空', trigger: 'blur' },
          { type: 'number', min: 0, message: '积分不能小于0', trigger: 'blur' }
        ]
      },
      resetPwdRules: {
        password: [
          { required: true, message: '新密码不能为空', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度必须介于 6 和 20 之间', trigger: 'blur' }
        ]
      },
      subscriptionRules: {
        subscriptionType: [
          { required: true, message: '请选择订阅类型', trigger: 'change' }
        ],
        expireTime: [
          { required: true, message: '请选择到期时间', trigger: 'change' }
        ]
      }
    }
  },
  computed: {
    calculateRemaining() {
      if (this.pointsForm.totalPoints != null) {
        return this.pointsForm.totalPoints - this.currentUsedPoints
      }
      return 0
    }
  },
  created() {
    this.getList()
    this.loadSubscriptionPackages()
  },
  methods: {
    getList() {
      this.loading = true
      listUser(this.queryParams).then(response => {
        this.userList = response.rows
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
    handleStatusChange(row) {
      const text = row.status === '0' ? '启用' : '停用'
      this.$confirm('确认要' + text + '用户"' + row.userName + '"吗?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return updateUserStatus({
          userId: row.userId,
          status: row.status
        })
      }).then(() => {
        this.$message.success(text + '成功')
        this.getList()
      }).catch(() => {
        row.status = row.status === '0' ? '1' : '0'
      })
    },
    handleEditPoints(row) {
      this.pointsForm = {
        userId: row.userId,
        userName: row.userName,
        totalPoints: row.params.totalPoints || 0
      }
      this.currentTotalPoints = row.params.totalPoints || 0
      this.currentUsedPoints = row.params.usedPoints || 0
      this.pointsOpen = true
    },
    submitPoints() {
      this.$refs.pointsForm.validate(valid => {
        if (valid) {
          updateUserPoints(this.pointsForm).then(response => {
            this.$message.success('修改成功')
            this.pointsOpen = false
            this.getList()
          })
        }
      })
    },
    cancelPoints() {
      this.pointsOpen = false
      this.resetForm('pointsForm')
    },
    handleResetPwd(row) {
      this.resetPwdForm = {
        userId: row.userId,
        userName: row.userName,
        password: ''
      }
      this.resetPwdOpen = true
    },
    submitResetPwd() {
      this.$refs.resetPwdForm.validate(valid => {
        if (valid) {
          resetUserPwd(this.resetPwdForm).then(response => {
            this.$message.success('密码重置成功')
            this.resetPwdOpen = false
            this.getList()
          })
        }
      })
    },
    cancelResetPwd() {
      this.resetPwdOpen = false
      this.resetForm('resetPwdForm')
    },
    handleInitPoints() {
      this.$confirm('将为所有没有积分记录的用户初始化1000积分，是否继续？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return initUserPoints()
      }).then(response => {
        this.$message.success(response.msg)
        this.getList()
      })
    },
    handleEditSubscription(row) {
      const pkgId = (row.params && row.params.packageId) ? Number(row.params.packageId) : null
      this.subscriptionForm = {
        userId: row.userId,
        userName: row.userName,
        subscriptionType: row.subscriptionType || 'normal',
        packageId: pkgId,
        isPermanent: row.isPermanent === 1,
        expireTime: row.subscriptionExpireTime
      }
      // 如果是钻石用户但没有packageId，默认选第一个套餐
      if (row.subscriptionType === 'diamond' && !pkgId && this.subscriptionPackages.length > 0) {
        this.subscriptionForm.packageId = this.subscriptionPackages[0].packageId
      }
      this.subscriptionOpen = true
    },
    submitSubscription() {
      this.$refs.subscriptionForm.validate(valid => {
        if (valid) {
          const data = {
            userId: this.subscriptionForm.userId,
            subscriptionType: this.subscriptionForm.subscriptionType,
            packageId: this.subscriptionForm.packageId,
            isPermanent: this.subscriptionForm.isPermanent ? 1 : 0,
            // 永久订阅时发送固定的未来时间 2999-12-31 23:59:59
            expireTime: this.subscriptionForm.isPermanent ? '2999-12-31 23:59:59' : this.subscriptionForm.expireTime
          }
          updateUserSubscription(data).then(response => {
            this.$message.success('订阅信息已更新')
            this.subscriptionOpen = false
            this.getList()
          })
        }
      })
    },
    cancelSubscription() {
      this.subscriptionOpen = false
      this.resetForm('subscriptionForm')
    },
    loadSubscriptionPackages() {
      listSubscriptionPackage({status: '0'}).then(response => {
        this.subscriptionPackages = response.rows || []
      })
    },
    handleSubscriptionTypeChange(value) {
      if (value === 'normal') {
        // 普通用户，清空套餐和到期时间
        this.subscriptionForm.packageId = null
        this.subscriptionForm.isPermanent = false
        this.subscriptionForm.expireTime = null
      } else if (value === 'diamond') {
        // 钻石订阅用户，设置默认值
        if (this.subscriptionPackages.length > 0 && !this.subscriptionForm.packageId) {
          this.subscriptionForm.packageId = this.subscriptionPackages[0].packageId
        }
      }
    },
    handlePermanentChange(value) {
      // 勾选永久订阅时，清空到期时间
      if (value) {
        this.subscriptionForm.expireTime = null
        // 清除到期时间的验证错误
        this.$nextTick(() => {
          if (this.$refs.subscriptionForm) {
            this.$refs.subscriptionForm.clearValidate('expireTime')
          }
        })
      }
    },
    getExpireTimeDisplay(expireTime) {
      // 如果到期时间是 2999 年，显示为"永久有效"
      if (expireTime && expireTime.startsWith('2999')) {
        return '永久有效'
      }
      return expireTime || '-'
    },
    resetForm(refName) {
      if (this.$refs[refName]) {
        this.$refs[refName].resetFields()
      }
    }
  }
}
</script>

<style scoped>
.tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
