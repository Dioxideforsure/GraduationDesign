<template>
  <div class="admin-file-list">
    <div class="top-op">
      <div class="search-panel">
        <el-input clearable v-model="fileNameFuzzy" placeholder="搜索文件名" @keyup.enter="loadDataList">
          <template #suffix>
            <i class="iconfont icon-search" @click="loadDataList"></i>
          </template>
        </el-input>
      </div>
    </div>

    <el-table :data="tableData" stripe style="width: 100%" height="calc(100vh - 150px)" v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column label="文件名" prop="fileName" min-width="300">
        <template #default="scope">
          <div class="file-item">
            <span :class="['iconfont', scope.row.folderType == 1 ? 'icon-folder' : 'icon-file']"></span>
            <span class="file-name">{{ scope.row.fileName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="nickName" label="发布人" width="150" />
      <el-table-column prop="updateTime" label="更新时间" width="200" />
      <el-table-column prop="fileSize" label="大小" width="150">
        <template #default="scope">
          {{ scope.row.folderType == 1 ? '-' : Utils.size2Str(scope.row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="scope">
          <el-button type="danger" link @click="delFile(scope.row)">删除</el-button>
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
const fileNameFuzzy = ref("");
const selectFileIdList = ref([]);

const loadDataList = async () => {
  loading.value = true;
  try {
    await new Promise((r) => setTimeout(r, 500));
    tableData.value = [
      { fileId: "101", fileName: "JavaEE大作业要求.pdf", nickName: "Kuo", updateTime: "2026-04-07 10:30", fileSize: 5242880, folderType: 0 },
      { fileId: "102", fileName: "前端学习资料", nickName: "测试用户", updateTime: "2026-04-06 14:00", fileSize: 0, folderType: 1 },
      { fileId: "103", fileName: "H3C配置脚本.py", nickName: "Kuo", updateTime: "2026-04-05 09:15", fileSize: 10240, folderType: 0 },
    ];
  } finally {
    loading.value = false;
  }
};

const handleSelectionChange = (val) => {
  selectFileIdList.value = val.map((item) => item.fileId);
};
const delFile = (row) => {
  Message.error(`管理员已删除文件: ${row.fileName} (待对接后端)`);
};

loadDataList();
</script>

<style lang="scss" scoped>
.admin-file-list { padding-right: 20px; }
.top-op { margin-top: 20px; margin-bottom: 20px; }
.search-panel { width: 300px; .icon-search { cursor: pointer; color: #999; &:hover { color: #05a1f5; } } }
.file-item {
  display: flex; align-items: center;
  .iconfont { font-size: 26px; margin-right: 10px; color: #ffd659; }
  .icon-file { color: #b0b0b0; }
  .file-name { color: #4a4a4a; }
}
</style>