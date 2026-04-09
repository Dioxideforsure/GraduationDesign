<template>
  <div class="share-list">
    <div class="top-op">
      <el-button type="primary" :disabled="selectIdList.length === 0" @click="cancelShareBatch">
        <span class="iconfont icon-cancel"></span> 批量取消分享
      </el-button>
    </div>
    
    <el-table :data="tableData" stripe style="width: 100%" v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="fileName" label="文件名" min-width="200">
        <template #default="scope">
          <div class="file-item">
            <span :class="['iconfont', scope.row.folderType == 1 ? 'icon-folder' : 'icon-file']"></span>
            <span class="file-name">{{ scope.row.fileName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="shareTime" label="分享时间" width="200" />
      <el-table-column prop="expireTime" label="失效时间" width="200" />
      <el-table-column prop="browseCount" label="浏览次数" width="100" />
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button type="primary" link @click="copyLink(scope.row)">复制链接</el-button>
          <el-button type="danger" link @click="cancelShare(scope.row)">取消分享</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, getCurrentInstance } from "vue";
const { proxy } = getCurrentInstance();

const loading = ref(false);
const tableData = ref([]);
const selectIdList = ref([]);

// 模拟加载分享列表
const loadDataList = () => {
  loading.value = true;
  setTimeout(() => {
    tableData.value = [
      { shareId: "1", fileName: "毕设开题报告.pdf", shareTime: "2026-04-06 10:00", expireTime: "2026-04-13 10:00", browseCount: 5, folderType: 0 },
      { shareId: "2", fileName: "项目源码", shareTime: "2026-04-05 14:00", expireTime: "永久有效", browseCount: 12, folderType: 1 }
    ];
    loading.value = false;
  }, 500);
};

const handleSelectionChange = (val) => { selectIdList.value = val.map(item => item.shareId); };
const copyLink = (row) => { proxy.Message.success(`链接已复制: ${row.fileName}`); };
const cancelShare = (row) => { proxy.Message.success("取消分享成功 (待对接后端)"); };
const cancelShareBatch = () => { proxy.Message.success("批量取消分享成功 (待对接后端)"); };

loadDataList();
</script>

<style lang="scss" scoped>
.share-list { padding-right: 20px; }
.top-op { margin-top: 20px; margin-bottom: 20px; }
.file-item {
  display: flex; align-items: center;
  .iconfont { font-size: 24px; margin-right: 10px; color: #ffd659; }
  .icon-file { color: #b0b0b0; }
  .file-name { color: #4a4a4a; }
}
</style>