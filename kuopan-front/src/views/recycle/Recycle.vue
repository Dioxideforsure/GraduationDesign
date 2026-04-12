<template>
  <div class="recycle-list">
    <div class="top-op">
      <el-button type="success" :disabled="selectIdList.length === 0" @click="recoverBatch">
        <span class="iconfont icon-revert"></span> 还原
      </el-button>
      <el-button type="danger" :disabled="selectIdList.length === 0" @click="delBatch">
        <span class="iconfont icon-del"></span> 彻底删除
      </el-button>
    </div>

    <el-table :data="tableData" stripe style="width: 100%" v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />

      <el-table-column prop="fileName" label="文件名" min-width="200">
        <template #default="scope">
          <div class="file-item">
            <template v-if="getThumbnailUrl(scope.row)">
              <el-image class="file-cover" :src="getThumbnailUrl(scope.row)" fit="cover">
                <template #error>
                  <div class="image-slot"><span class="iconfont icon-file"></span></div>
                </template>
              </el-image>
            </template>
            <template v-else>
              <span :class="['iconfont', scope.row.folderType == 1 ? 'icon-folder' : 'icon-file']"></span>
            </template>

            <span class="file-name">{{ scope.row.fileName }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="recoveryTime" label="删除时间" width="200" />

      <el-table-column prop="fileSize" label="大小" width="150">
        <template #default="scope">
          {{ scope.row.folderType == 1 ? '-' : Utils.size2Str(scope.row.fileSize) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="150" align="center">
        <template #default="scope">
          <el-button type="success" link @click="recover(scope.row)">还原</el-button>
          <el-button type="danger" link @click="del(scope.row)">彻底删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { ElMessageBox } from 'element-plus';
import Request from "@/utils/Request.js";
import Message from "@/utils/Message.js";
import Utils from "@/utils/Utils.js";

const loading = ref(false);
const tableData = ref([]);
const selectIdList = ref([]);

// 智能获取缩略图
const getThumbnailUrl = (row) => {
  if (row.fileCover) return `/api/file_res/${row.fileCover}`;
  if (row.fileCategory === 3 && row.filePath) return `/api/file_res/${row.filePath}`;
  return null;
};

// 加载真实的回收站列表
const loadDataList = async () => {
  loading.value = true;
  try {
    let result = await Request({
      url: "/recycle/loadRecycleList",
      showLoading: false
    });
    if (result && result.data) {
      tableData.value = result.data.list || result.data || [];
    }
  } finally {
    loading.value = false;
  }
};

const handleSelectionChange = (val) => {
  selectIdList.value = val.map(item => item.fileId);
};

// 还原单文件
const recover = async (row) => {
  let result = await Request({
    url: "/recycle/recoverFile",
    params: { fileIds: row.fileId }
  });
  if(result) {
    Message.success("还原成功");
    loadDataList();
  }
};

// 批量还原
const recoverBatch = async () => {
  let result = await Request({
    url: "/recycle/recoverFile",
    params: { fileIds: selectIdList.value.join(",") }
  });
  if(result) {
    Message.success("批量还原成功");
    loadDataList();
  }
};

// 彻底删除单文件
const del = (row) => {
  ElMessageBox.confirm('彻底删除后无法恢复，确定吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    let result = await Request({
      url: "/recycle/delFile",
      params: { fileIds: row.fileId }
    });
    if(result) {
      Message.success("已彻底删除");
      loadDataList();
    }
  }).catch(() => {});
};

// 批量彻底删除
const delBatch = () => {
  ElMessageBox.confirm('彻底删除后无法恢复，确定吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    let result = await Request({
      url: "/recycle/delFile",
      params: { fileIds: selectIdList.value.join(",") }
    });
    if(result) {
      Message.success("已批量彻底删除");
      loadDataList();
    }
  }).catch(() => {});
};

// 初始化加载数据
loadDataList();
</script>

<style lang="scss" scoped>
.recycle-list { padding-right: 20px; }
.top-op { margin-top: 20px; margin-bottom: 20px; display: flex; gap: 10px; }

/* 👇 增加和 Main.vue 一模一样的图片样式 👇 */
.file-item {
  display: flex; align-items: center;
  .file-cover {
    width: 40px; height: 40px; border-radius: 6px; margin-right: 15px; flex-shrink: 0; background-color: #f0f0f0;
    display: flex; align-items: center; justify-content: center;
    .image-slot { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: #b0b0b0; .iconfont { margin-right: 0; font-size: 24px; } }
  }
  .iconfont { font-size: 28px; margin-right: 15px; color: #ffd659; }
  .icon-file { color: #b0b0b0; }
  .file-name { color: #4a4a4a; }
}
</style>