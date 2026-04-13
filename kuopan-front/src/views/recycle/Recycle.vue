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
const selectIdList = ref([]);

// ==================== 智能图标匹配逻辑 ====================
const getThumbnailUrl = (row) => {
  if (row.fileCover) return `/api/file_res/${row.fileCover}`;
  if (row.fileCategory === 3 && row.filePath) return `/api/file_res/${row.filePath}`;
  return null;
};

const iconMap = {
  pdf: iconPdf,
  doc: iconWord, docx: iconWord,
  xls: iconExcel, xlsx: iconExcel,
  ppt: iconPpt, pptx: iconPpt,
  txt: iconTxt,
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

// ==================== 回收站核心交互逻辑 ====================
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

const recover = async (row) => {
  let result = await Request({ url: "/recycle/recoverFile", params: { fileIds: row.fileId } });
  if(result) { Message.success("还原成功"); loadDataList(); }
};

const recoverBatch = async () => {
  let result = await Request({ url: "/recycle/recoverFile", params: { fileIds: selectIdList.value.join(",") } });
  if(result) { Message.success("批量还原成功"); loadDataList(); }
};

const del = (row) => {
  ElMessageBox.confirm('彻底删除后无法恢复，确定吗？', '警告', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'error' }).then(async () => {
    let result = await Request({ url: "/recycle/delFile", params: { fileIds: row.fileId } });
    if(result) { Message.success("已彻底删除"); loadDataList(); }
  }).catch(() => {});
};

const delBatch = () => {
  ElMessageBox.confirm('彻底删除后无法恢复，确定吗？', '警告', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'error' }).then(async () => {
    let result = await Request({ url: "/recycle/delFile", params: { fileIds: selectIdList.value.join(",") } });
    if(result) { Message.success("已批量彻底删除"); loadDataList(); }
  }).catch(() => {});
};

loadDataList();
</script>

<style lang="scss" scoped>
.recycle-list { padding-right: 20px; }
.top-op { margin-top: 20px; margin-bottom: 20px; display: flex; gap: 10px; }


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