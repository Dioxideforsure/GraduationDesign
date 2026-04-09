<template>
  <div class="user-list">
    <div class="top-op">
      <div class="search-panel">
        <el-input clearable v-model="searchName" placeholder="搜索用户昵称" @keyup.enter="loadDataList">
          <template #suffix><i class="iconfont icon-search" @click="loadDataList"></i></template>
        </el-input>
      </div>
    </div>

    <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="avatar" label="头像" width="80">
        <template #default="scope">
          <div class="avatar-box">{{ scope.row.nickName.substring(0,1) }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="nickName" label="昵称" width="150" />
      <el-table-column prop="email" label="邮箱" width="200" />
      <el-table-column label="空间使用" width="200">
        <template #default="scope">
          {{ Utils.size2Str(scope.row.useSpace) }} / {{ Utils.size2Str(scope.row.totalSpace) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <span :style="{color: scope.row.status === 1 ? '#67c23a' : '#f56c6c'}">
            {{ scope.row.status === 1 ? '正常' : '禁用' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button type="primary" link @click="updateSpace(scope.row)">分配空间</el-button>
          <el-button :type="scope.row.status === 1 ? 'danger' : 'success'" link @click="changeStatus(scope.row)">
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref } from "vue";
import Utils from "@/utils/Utils.js";
import Message from "@/utils/Message.js";

const loading = ref(false);
const tableData = ref([]);
const searchName = ref("");

const loadDataList = async () => {
  loading.value = true;
  try {
    await new Promise((r) => setTimeout(r, 500));
    tableData.value = [
      { userId: "1", nickName: "Kuo", email: "admin@example.com", useSpace: 536870912, totalSpace: 10737418240, status: 1 },
      { userId: "2", nickName: "测试用户", email: "test@example.com", useSpace: 1024000, totalSpace: 1073741824, status: 0 },
    ];
  } finally {
    loading.value = false;
  }
};

const updateSpace = (row) => {
  Message.success(`准备为 ${row.nickName} 分配空间 (待对接后端)`);
};
const changeStatus = (row) => {
  Message.success(`状态已更改 (待对接后端)`);
};

loadDataList();
</script>

<style lang="scss" scoped>
.user-list { padding-right: 20px; }
.top-op { margin-top: 20px; margin-bottom: 20px; }
.search-panel { width: 300px; .icon-search { cursor: pointer; color: #999; &:hover { color: #05a1f5; } } }
.avatar-box {
  width: 40px; height: 40px; border-radius: 50%; background: #05a1f5;
  color: #fff; display: flex; justify-content: center; align-items: center; font-weight: bold;
}
</style>