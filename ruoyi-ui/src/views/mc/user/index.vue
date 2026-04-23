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
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-unlock" :disabled="single" @click="handleBatchEnable">批量启用</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-lock" :disabled="single" @click="handleBatchDisable">批量停用</el-button>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
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
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="250">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleDetail(scope.row)">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-coin" @click="handleEditPoints(scope.row)">修改积分</el-button>
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

    <!-- 用户详情对话框 -->
    <el-dialog title="用户详情" :visible.sync="detailOpen" width="800px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户ID">{{ detailData.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detailData.userName }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detailData.nickName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailData.phonenumber }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detailData.email || '未绑定' }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          <span v-if="detailData.sex === '0'">男</span>
          <span v-else-if="detailData.sex === '1'">女</span>
          <span v-else>未知</span>
        </el-descriptions-item>
        <el-descriptions-item label="账号状态">
          <el-tag :type="detailData.status === '0' ? 'success' : 'danger'">
            {{ detailData.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>积分信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="stat-card">
            <div class="stat-label">总积分</div>
            <div class="stat-value" style="color: #67c23a;">{{ detailData.params && detailData.params.totalPoints || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card">
            <div class="stat-label">已使用</div>
            <div class="stat-value" style="color: #e6a23c;">{{ detailData.params && detailData.params.usedPoints || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card">
            <div class="stat-label">剩余积分</div>
            <div class="stat-value" style="color: #409eff;">{{ detailData.params && detailData.params.remainingPoints || 0 }}</div>
          </div>
        </el-col>
      </el-row>

      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listUser, updateUserPoints, updateUserStatus, resetUserPwd, initUserPoints, batchUpdateStatus } from '@/api/mc/user'

export default {
  name: 'McUserManage',
  data() {
    return {
      loading: false,
      userList: [],
      total: 0,
      pointsOpen: false,
      resetPwdOpen: false,
      detailOpen: false,
      ids: [],
      single: true,
      multiple: true,
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
      detailData: {},
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
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.userId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleDetail(row) {
      this.detailData = Object.assign({}, row)
      this.detailOpen = true
    },
    handleBatchEnable() {
      if (this.ids.length === 0) {
        this.$message.warning('请选择要启用的用户')
        return
      }
      this.$confirm('确认要启用选中的' + this.ids.length + '个用户吗?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return batchUpdateStatus({ userIds: this.ids, status: '0' })
      }).then(() => {
        this.$message.success('启用成功')
        this.getList()
      })
    },
    handleBatchDisable() {
      if (this.ids.length === 0) {
        this.$message.warning('请选择要停用的用户')
        return
      }
      this.$confirm('确认要停用选中的' + this.ids.length + '个用户吗?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return batchUpdateStatus({ userIds: this.ids, status: '1' })
      }).then(() => {
        this.$message.success('停用成功')
        this.getList()
      })
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

.stat-card {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 15px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
}
</style>
