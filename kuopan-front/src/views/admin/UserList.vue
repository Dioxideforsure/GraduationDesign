<template>
  <div class="user-list">
    <div class="top-op">
      <el-button type="primary" @click="openAddUserDialog">
        <span class="iconfont icon-add"></span> 新增用户
      </el-button>
      <div class="search-panel">
        <el-input clearable v-model="searchName" placeholder="搜索用户昵称" @keyup.enter="loadDataList">
          <template #suffix><i class="iconfont icon-search" @click="loadDataList"></i></template>
        </el-input>
      </div>
    </div>

    <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="avatar" label="头像" width="70">
        <template #default="scope">
          <div class="avatar-box">{{ scope.row.nickName ? scope.row.nickName.substring(0,1) : 'U' }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="nickName" label="昵称" width="130" />
      <el-table-column prop="email" label="邮箱" width="200" />

      <el-table-column prop="role" label="身份" width="100">
        <template #default="scope">
          <el-tag :type="getRoleTag(scope.row.role)" effect="light">
            {{ getRoleName(scope.row.role) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="空间使用" width="180">
        <template #default="scope">
          {{ Utils.size2Str(scope.row.useSpace || 0) }} / {{ Utils.size2Str(scope.row.totalSpace) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <span :style="{color: scope.row.status === 1 ? '#67c23a' : '#f56c6c'}">
            {{ scope.row.status === 1 ? '正常' : '禁用' }}
          </span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="220" align="center">
        <template #default="scope">
          <template v-if="scope.row.role !== 0">
            <el-button type="primary" link @click="updateSpace(scope.row)">分配空间</el-button>
            <el-button :type="scope.row.status === 1 ? 'warning' : 'success'" link @click="changeStatus(scope.row)">
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="delUser(scope.row)">删除</el-button>
          </template>
          <span v-else style="color: #c0c4cc; font-size: 13px;">
            <el-icon><Lock /></el-icon> 系统级保护
          </span>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="addUserDialogVisible" title="新增系统用户" width="450px" @close="resetForm">
      <el-form :model="addUserForm" :rules="rules" ref="addUserFormRef" label-width="80px">
        <el-form-item label="用户昵称" prop="nickName">
          <el-input v-model="addUserForm.nickName" placeholder="请输入昵称" maxlength="20" />
        </el-form-item>
        <el-form-item label="登录邮箱" prop="email">
          <el-input v-model="addUserForm.email" placeholder="请输入唯一邮箱" />
        </el-form-item>
        <el-form-item label="初始密码" prop="password">
          <el-input v-model="addUserForm.password" placeholder="请输入密码" type="password" show-password />
        </el-form-item>
        <el-form-item label="分配身份" prop="role">
          <el-select v-model="addUserForm.role" placeholder="请选择身份级别" style="width: 100%;">
            <el-option label="老师 (Teacher)" :value="1" />
            <el-option label="学生 (Student)" :value="2" />
            <el-option label="访客 (Guest)" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addUserDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAddUser">确认添加</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="dialogVisible" title="分配网盘空间" width="400px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="分配空间大小">
          <el-input v-model.number="formData.space" placeholder="请输入空间大小">
            <template #append>MB</template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitSpace">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, getCurrentInstance } from "vue";
import { ElMessageBox } from 'element-plus';
import { Lock } from '@element-plus/icons-vue';
import Utils from "@/utils/Utils.js";

const { proxy } = getCurrentInstance();

const loading = ref(false);
const tableData = ref([]);
const searchName = ref("");

// 分配空间相关
const dialogVisible = ref(false);
const formData = ref({ userId: '', space: 0 });

// 新增用户相关
const addUserDialogVisible = ref(false);
const addUserFormRef = ref();
const addUserForm = ref({ nickName: '', email: '', password: '', role: 2 });
const rules = {
  nickName: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] }
  ],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }]
};

// 角色映射逻辑
const getRoleName = (role) => {
  const map = { 0: "管理员", 1: "老师", 2: "学生", 3: "访客" };
  return map[role] || "未知";
};
const getRoleTag = (role) => {
  const map = { 0: "danger", 1: "success", 2: "primary", 3: "info" };
  return map[role] || "info";
};

// 1. 加载列表
const loadDataList = async () => {
  loading.value = true;
  let result = await proxy.Request({
    url: "/admin/loadUserList",
    params: { nickNameFuzzy: searchName.value }
  });
  loading.value = false;
  if (result && result.data) {
    tableData.value = result.data.list || result.data;
  }
};

// 2. 新增用户打开与提交
const openAddUserDialog = () => {
  addUserDialogVisible.value = true;
};
const resetForm = () => {
  if (addUserFormRef.value) addUserFormRef.value.resetFields();
  addUserForm.value = { nickName: '', email: '', password: '', role: 2 };
};
const submitAddUser = () => {
  addUserFormRef.value.validate(async (valid) => {
    if (!valid) return;
    let result = await proxy.Request({
      url: "/admin/addUser",
      params: addUserForm.value
    });
    if (result) {
      proxy.Message.success("添加用户成功");
      addUserDialogVisible.value = false;
      loadDataList();
    }
  });
};

// 3. 删除用户
const delUser = (row) => {
  ElMessageBox.confirm(`确定要永久删除用户【${row.nickName}】吗？`, '高危操作警告', {
    confirmButtonText: '强制删除',
    cancelButtonText: '取消',
    type: 'error',
  }).then(async () => {
    let result = await proxy.Request({
      url: "/admin/delUser",
      params: { userId: row.userId }
    });
    if (result) {
      proxy.Message.success("删除成功");
      loadDataList();
    }
  }).catch(() => {});
};

// 4. 修改状态 (禁用/启用)
const changeStatus = async (row) => {
  let targetStatus = row.status === 1 ? 0 : 1;
  let result = await proxy.Request({
    url: "/admin/updateUserStatus",
    params: { userId: row.userId, status: targetStatus }
  });
  if (result) {
    proxy.Message.success("状态修改成功");
    loadDataList();
  }
};

// 5. 分配空间
const updateSpace = (row) => {
  formData.value.userId = row.userId;
  formData.value.space = Math.round(row.totalSpace / (1024 * 1024)); // Byte转MB
  dialogVisible.value = true;
};
const submitSpace = async () => {
  if (!formData.value.space || formData.value.space <= 0) {
    proxy.Message.warning("空间大小必须大于0");
    return;
  }
  let result = await proxy.Request({
    url: "/admin/updateUserSpace",
    params: {
      userId: formData.value.userId,
      changeSpace: formData.value.space
    }
  });
  if (result) {
    proxy.Message.success("空间分配成功");
    dialogVisible.value = false;
    loadDataList();
  }
};

loadDataList();
</script>

<style lang="scss" scoped>
.user-list { padding-right: 20px; }
.top-op {
  display: flex; justify-content: space-between; align-items: center;
  margin-top: 20px; margin-bottom: 20px;
}
.search-panel { width: 300px; .icon-search { cursor: pointer; color: #999; &:hover { color: #05a1f5; } } }
.avatar-box {
  width: 40px; height: 40px; border-radius: 50%; background: #05a1f5;
  color: #fff; display: flex; justify-content: center; align-items: center; font-weight: bold; font-size: 18px;
}
</style>