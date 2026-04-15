<template>
  <div class="admin-file-list">
    <div class="top-op">
      <div class="search-panel">
        <el-input clearable v-model="fileNameFuzzy" placeholder="搜索全局文件名" @keyup.enter="loadDataList">
          <template #suffix>
            <i class="iconfont icon-search" @click="loadDataList"></i>
          </template>
        </el-input>
      </div>
    </div>

    <el-table :data="tableData" stripe style="width: 100%" height="calc(100vh - 150px)" v-loading="loading">
      <el-table-column label="文件名" prop="fileName" min-width="300">
        <template #default="scope">
          <div class="file-item">
            <template v-if="getThumbnailUrl(scope.row)">
              <el-image class="file-cover" :src="getThumbnailUrl(scope.row)" fit="cover">
                <template #error>
                  <div class="image-slot">
                    <img class="file-icon" :src="getFileIcon(scope.row)" />
                  </div>
                </template>
              </el-image>
            </template>
            <template v-else>
              <img class="file-icon" :src="getFileIcon(scope.row)" />
            </template>

            <span class="file-name">{{ scope.row.fileName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="nickName" label="归属用户" width="150" />
      <el-table-column prop="updateTime" label="更新时间" width="200" />
      <el-table-column prop="fileSize" label="大小" width="150">
        <template #default="scope">
          {{ scope.row.folderType == 1 ? '-' : Utils.size2Str(scope.row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center">
        <template #default="scope">
          <el-button type="danger" link @click="delFile(scope.row)">强制删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, getCurrentInstance } from "vue";
import { ElMessageBox } from 'element-plus';
import Utils from "@/utils/Utils.js";

const { proxy } = getCurrentInstance();

// ====== 🚀 引入本地精美图标资源 ======
import iconFolder from '@/assets/icon-image/folder.png';
import iconPdf from '@/assets/icon-image/pdf.png';
import iconWord from '@/assets/icon-image/word.png';
import iconExcel from '@/assets/icon-image/excel.png';
import iconPpt from '@/assets/icon-image/ppt1.png';
import iconTxt from '@/assets/icon-image/txt.png';
import iconCode from '@/assets/icon-image/code.png';
import iconZip from '@/assets/icon-image/zip.png';
import iconExe from '@/assets/icon-image/exe.png';
import iconVideo from '@/assets/icon-image/video.png';
import iconMusic from '@/assets/icon-image/music.png';
import iconImage from '@/assets/icon-image/image.png';
import iconOthers from '@/assets/icon-image/others.png';

const loading = ref(false);
const tableData = ref([]);
const fileNameFuzzy = ref("");

// ==================== 智能图标匹配逻辑 ====================
const getThumbnailUrl = (row) => {
  if (row.fileCover) return `/api/file_res/${row.fileCover}`;
  if (row.fileCategory === 3 && row.filePath) return `/api/file_res/${row.filePath}`;
  return null;
};

const iconMap = {
  pdf: iconPdf, doc: iconWord, docx: iconWord, xls: iconExcel, xlsx: iconExcel,
  ppt: iconPpt, pptx: iconPpt, txt: iconTxt,
  zip: iconZip, rar: iconZip, '7z': iconZip, tar: iconZip, gz: iconZip,
  exe: iconExe, bat: iconExe, msi: iconExe,
  js: iconCode, java: iconCode, vue: iconCode, html: iconCode, css: iconCode, json: iconCode, py: iconCode, xml: iconCode, sql: iconCode,
  mp3: iconMusic, wav: iconMusic, flac: iconMusic, ogg: iconMusic,
  mp4: iconVideo, avi: iconVideo, mkv: iconVideo, rmvb: iconVideo, mov: iconVideo,
  jpg: iconImage, jpeg: iconImage, png: iconImage, gif: iconImage, bmp: iconImage, svg: iconImage
};

const getFileIcon = (row) => {
  if (row.folderType === 1) return iconFolder;
  const fileName = row.fileName || '';
  const suffix = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
  return iconMap[suffix] || iconOthers;
};

// 1. 加载全站文件
const loadDataList = async () => {
  loading.value = true;
  let result = await proxy.Request({
    url: "/admin/loadFileList",
    params: { fileNameFuzzy: fileNameFuzzy.value }
  });
  loading.value = false;
  if (result && result.data) {
    tableData.value = result.data.list || result.data;
  }
};

// 2. 强制彻底删除文件
const delFile = (row) => {
  ElMessageBox.confirm(`确定要强制删除文件【${row.fileName}】吗？删除后该用户将无法找回！`, '严重警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'error',
  }).then(async () => {
    let result = await proxy.Request({
      url: "/admin/delFile",
      params: {
        fileId: row.fileId,
        userId: row.userId  // 传给后端以扣除该用户的空间
      }
    });
    if (result) {
      proxy.Message.success("强制删除成功");
      loadDataList();
    }
  }).catch(() => {});
};

loadDataList();
</script>

<style lang="scss" scoped>
.admin-file-list { padding-right: 20px; }
.top-op { margin-top: 20px; margin-bottom: 20px; }
.search-panel { width: 300px; .icon-search { cursor: pointer; color: #999; &:hover { color: #05a1f5; } } }


.file-item {
  display: flex; align-items: center;
  .file-icon {
    width: 32px; height: 32px; margin-right: 15px; flex-shrink: 0;
  }
  .file-cover {
    width: 36px; height: 36px; border-radius: 4px; margin-right: 15px; flex-shrink: 0; background-color: #f0f0f0;
    display: flex; align-items: center; justify-content: center;
    .image-slot { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; }
  }
  .file-name { color: #4a4a4a; }
}
</style>