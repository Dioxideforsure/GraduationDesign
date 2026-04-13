<template>
  <div class="main-body">
    <div class="top-op">
      <div class="btn-list">
        <el-button type="primary" @click="triggerFileSelect">
          <span class="iconfont icon-upload"></span> 上传
        </el-button>
        <input
            type="file"
            ref="fileInputRef"
            style="display: none"
            @change="handleFileSelect"
        />

        <el-button type="success" @click="openNewFolderDialog">
          <span class="iconfont icon-add-folder"></span> 新建文件夹
        </el-button>

        <el-button type="danger" :disabled="selectFileIdList.length === 0" @click="batchDelete">
          <span class="iconfont icon-del"></span> 批量删除
        </el-button>
      </div>

      <div class="search-panel">
        <el-input
            clearable
            v-model="fileNameFuzzy"
            placeholder="搜索我的文件"
            @keyup.enter="loadDataList"
        >
          <template #suffix>
            <i class="iconfont icon-search" @click="loadDataList"></i>
          </template>
        </el-input>
      </div>
    </div>

    <div class="breadcrumb" v-if="category === 'all' || category === ''">
      <span class="nav-item" @click="navToRoot">全部文件</span>
      <span v-for="(item, index) in breadcrumbList" :key="item.fileId">
        <span class="separator">></span>
        <span class="nav-item" @click="navToFolder(item, index)">{{ item.fileName }}</span>
      </span>
    </div>

    <div class="file-list">
      <el-table
          :data="fileList"
          stripe
          style="width: 100%"
          height="calc(100vh - 200px)"
          v-loading="loading"
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />

        <el-table-column label="文件名" prop="fileName" min-width="350">
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

              <span class="file-name" @click="itemClick(scope.row)">
                {{ scope.row.fileName }}
                <span style="color: #909399; font-size: 12px; margin-left: 10px;" v-if="scope.row.status === 0">(视频转码中...)</span>
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="updateTime" label="修改时间" width="200" />
        <el-table-column prop="fileSize" label="大小" width="120">
          <template #default="scope">
            {{ scope.row.folderType == 1 ? "-" : Utils.size2Str(scope.row.fileSize) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button type="primary" link v-if="scope.row.folderType != 1" @click="downloadFile(scope.row)">下载</el-button>
            <el-button type="danger" link @click="deleteFile(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="newFolderDialogVisible" title="新建文件夹" width="400px">
      <el-form @submit.prevent>
        <el-form-item label="文件夹名称">
          <el-input v-model="newFolderName" placeholder="请输入文件夹名称" @keyup.enter="submitNewFolder" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="newFolderDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitNewFolder">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="chunkUploadDialogVisible" title="分片上传" width="500px" :close-on-click-modal="false" :close-on-press-escape="!uploading">
      <div v-if="selectedFiles.length > 0">
        <div class="file-info">
          <p><strong>文件名：</strong>{{ selectedFiles[0].name }}</p>
          <p><strong>文件大小：</strong>{{ Utils.size2Str(selectedFiles[0].size) }}</p>
        </div>
        <div v-for="(progress, fileName) in uploadProgress" :key="fileName" class="upload-progress">
          <div class="progress-info">
            <span>{{ fileName }}</span>
            <span>{{ progress.percent }}%</span>
          </div>
          <el-progress :percentage="progress.percent" :status="uploading ? '' : 'success'" :stroke-width="8" />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeChunkDialog" :disabled="uploading">取消</el-button>
          <el-button type="primary" @click="startChunkUpload(selectedFiles[0])" :disabled="uploading">
            {{ uploading ? '上传中...' : '开始上传' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <Preview ref="previewRef" />
  </div>
</template>

<script setup>
import { ref, watch, computed } from "vue";
import { useRoute } from "vue-router";
import { ElLoading, ElMessageBox } from 'element-plus';
import Utils from "@/utils/Utils.js";
import Request from "@/utils/Request.js";
import Message from "@/utils/Message.js";
import SparkMD5 from 'spark-md5';
import Preview from "@/components/Preview.vue";

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

const route = useRoute();
const loading = ref(false);
const fileList = ref([]);
const fileNameFuzzy = ref("");
const selectFileIdList = ref([]);

const currentFolderId = ref("0");
const breadcrumbList = ref([]);
const category = computed(() => route?.params?.category || "all");

const previewRef = ref(null);
const newFolderDialogVisible = ref(false);
const newFolderName = ref("");

const fileInputRef = ref(null);
const chunkUploadDialogVisible = ref(false);
const selectedFiles = ref([]);
const chunkSize = 10 * 1024 * 1024;
const uploadProgress = ref({});
const uploading = ref(false);
const AUTO_CHUNK_THRESHOLD = 10 * 1024 * 1024;

// ==================== 智能图标匹配逻辑 ====================
// 1. 获取服务器真实封面 (图片/视频专用)
const getThumbnailUrl = (row) => {
  if (row.fileCover) return `/api/file_res/${row.fileCover}`;
  if (row.fileCategory === 3 && row.filePath) return `/api/file_res/${row.filePath}`;
  return null;
};

// 2. 文件后缀与图标映射表
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

// 3. 根据后缀获取对应图标
const getFileIcon = (row) => {
  if (row.folderType === 1) return iconFolder; // 文件夹直接返回
  const fileName = row.fileName || '';
  const suffix = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
  return iconMap[suffix] || iconOthers; // 匹配不到的给 others.png
};

// ==================== 文件列表加载 ====================
const loadDataList = async () => {
  loading.value = true;
  try {
    const result = await Request({
      url: "/file/list",
      params: {
        fileNameFuzzy: fileNameFuzzy.value,
        category: category.value === 'all' ? '' : category.value,
        filePid: currentFolderId.value
      },
      showLoading: false,
    });
    fileList.value = result?.data?.list || result?.data || [];
  } finally {
    loading.value = false;
  }
};

watch(
    () => route?.params?.category,
    () => {
      currentFolderId.value = "0";
      breadcrumbList.value = [];
      loadDataList();
    },
    { immediate: true },
);

// ==================== 目录导航 ====================
const itemClick = (row) => {
  if (row.folderType == 1) {
    breadcrumbList.value.push(row);
    currentFolderId.value = row.fileId;
    loadDataList();
  } else {
    if (row.fileCategory === 1 && row.status === 0) {
      Message.warning("视频正在努力转码中，请稍后再试！");
      return;
    }
    previewRef.value.show(row);
  }
};

const navToRoot = () => {
  currentFolderId.value = "0";
  breadcrumbList.value = [];
  loadDataList();
};

const navToFolder = (item, index) => {
  if (index === breadcrumbList.value.length - 1) return;
  currentFolderId.value = item.fileId;
  breadcrumbList.value = breadcrumbList.value.slice(0, index + 1);
  loadDataList();
};

// ==================== 新建文件夹 ====================
const openNewFolderDialog = () => {
  newFolderName.value = "";
  newFolderDialogVisible.value = true;
};

const submitNewFolder = async () => {
  if (!newFolderName.value.trim()) {
    Message.warning("文件夹名称不能为空");
    return;
  }
  const result = await Request({
    url: "/file/newFolder",
    params: {
      filePid: currentFolderId.value,
      fileName: newFolderName.value.trim()
    }
  });
  if (result) {
    Message.success("新建成功");
    newFolderDialogVisible.value = false;
    loadDataList();
  }
};

// ==================== 下载与删除 ====================
const downloadFile = (row) => {
  if (!row.filePath) {
    Message.error("文件路径丢失，无法下载");
    return;
  }
  const url = `/api/file_res/${row.filePath}`;
  const link = document.createElement("a");
  link.href = url;
  link.download = row.fileName;
  link.click();
};

const deleteFile = (row) => {
  ElMessageBox.confirm(`确定要将【${row.fileName}】放入回收站吗？`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning',
  }).then(async () => {
    const res = await Request({
      url: "/file/delFile",
      params: { fileIds: row.fileId }
    });
    if (res) {
      Message.success("已放入回收站");
      loadDataList();
    }
  }).catch(() => {});
};

const batchDelete = () => {
  if (selectFileIdList.value.length === 0) return;
  ElMessageBox.confirm(`确定要删除选中的 ${selectFileIdList.value.length} 个文件吗？`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning',
  }).then(async () => {
    const res = await Request({
      url: "/file/delFile",
      params: { fileIds: selectFileIdList.value.join(",") }
    });
    if (res) {
      Message.success("批量删除成功");
      loadDataList();
    }
  }).catch(() => {});
};

const handleSelectionChange = (val) => {
  selectFileIdList.value = val.map((item) => item.fileId);
};

// ==================== 上传逻辑 ====================
const triggerFileSelect = () => fileInputRef.value.click();

const handleFileSelect = async (event) => {
  const files = event.target.files;
  if (!files || files.length === 0) return;
  const file = files[0];
  event.target.value = '';

  if (file.size > 50 * 1024 * 1024) {
    Message.warning("文件大小超过50MB，暂不支持上传");
    return;
  }
  if (file.size >= AUTO_CHUNK_THRESHOLD) {
    selectedFiles.value = [file];
    uploadProgress.value = { [file.name]: { loaded: 0, total: file.size, percent: 0 } };
    chunkUploadDialogVisible.value = true;
  } else {
    await uploadFileDirect(file);
  }
};

const uploadFileDirect = async (file) => {
  const loadingInstance = ElLoading.service({ lock: true, text: '上传中...', background: 'rgba(0, 0, 0, 0.7)' });
  try {
    const result = await Request({
      url: "/file/upload",
      params: { filePid: currentFolderId.value },
      file: file,
      showLoading: false,
    });
    loadingInstance.close();
    if (result) {
      Message.success(result.data?.instantUpload ? "秒传成功" : "上传成功");
      await loadDataList();
    }
  } catch (error) {
    loadingInstance.close();
    Message.error("上传失败，请重试");
  }
};

const executeInstantUpload = async (fileMd5, file) => {
  const res = await Request({
    url: "/file/mergeChunk",
    params: {
      fileMd5, fileName: file.name, fileSize: file.size,
      chunks: Math.ceil(file.size / chunkSize),
      filePid: currentFolderId.value
    },
    showLoading: false,
  });
  if (res?.data?.instantUpload) {
    Message.success("秒传成功");
    closeChunkDialog();
    loadDataList();
    return true;
  }
  return false;
};

const calculateFileMd5 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    const spark = new SparkMD5.ArrayBuffer();
    reader.onload = (e) => { spark.append(e.target.result); resolve(spark.end()); };
    reader.onerror = reject;
    reader.readAsArrayBuffer(file);
  });
};

const checkFileExists = async (fileMd5, fileSize) => {
  try {
    const res = await Request({ url: "/file/checkMd5", method: 'GET', params: { fileMd5, fileSize }, showLoading: false });
    return res?.data?.exists === true;
  } catch { return false; }
};

const startChunkUpload = async (file) => {
  if (!file) return;
  let fileMd5;
  try {
    fileMd5 = await calculateFileMd5(file);
    uploadProgress.value = { [file.name]: { loaded: 0, total: file.size, percent: 0 } };
  } catch {
    Message.error("计算文件摘要失败"); closeChunkDialog(); return;
  }
  const exists = await checkFileExists(fileMd5, file.size);
  if (exists) {
    await executeInstantUpload(fileMd5, file);
    return;
  }
  await uploadChunks(fileMd5, file);
};

const uploadChunks = async (fileMd5, file) => {
  uploading.value = true;
  const chunks = Math.ceil(file.size / chunkSize);
  let uploadedChunksCount = 0;

  for (let i = 0; i < chunks; i++) {
    const start = i * chunkSize, end = Math.min(start + chunkSize, file.size);
    const chunk = file.slice(start, end);
    try {
      const res = await Request({
        url: "/file/uploadChunk",
        params: { fileMd5: fileMd5, chunkIndex: i, chunks: chunks },
        file: chunk,
        fileField: "chunk",
        showLoading: false,
        uploadProgressCallback: (progressEvent) => {
          let totalLoaded = (uploadedChunksCount * chunkSize) + progressEvent.loaded;
          if (totalLoaded > file.size) totalLoaded = file.size;
          uploadProgress.value[file.name] = {
            loaded: totalLoaded, total: file.size, percent: Math.min(Math.round((totalLoaded / file.size) * 100), 100)
          };
        }
      });
      if (res?.data?.uploaded) uploadedChunksCount++;
    } catch {
      Message.error(`分片上传失败`); uploading.value = false; return;
    }
  }
  if (uploadedChunksCount === chunks) {
    await mergeChunks(fileMd5, file);
  } else {
    uploading.value = false; Message.error("部分分片上传失败");
  }
};

const mergeChunks = async (fileMd5, file) => {
  try {
    const res = await Request({
      url: "/file/mergeChunk",
      params: {
        fileMd5, fileName: file.name, fileSize: file.size,
        chunks: Math.ceil(file.size / chunkSize),
        filePid: currentFolderId.value
      },
      showLoading: false
    });
    if (res?.data) {
      Message.success(res.data.instantUpload ? "秒传成功" : "文件上传成功");
      closeChunkDialog();
      loadDataList();
    }
  } catch {
    Message.error("文件合并失败");
  } finally {
    uploading.value = false;
  }
};

const closeChunkDialog = () => {
  chunkUploadDialogVisible.value = false; uploading.value = false; selectedFiles.value = []; uploadProgress.value = {};
};
</script>

<style lang="scss" scoped>
.main-body { height: 100%; padding-right: 20px; }
.top-op { display: flex; justify-content: space-between; align-items: center; margin: 20px 0; }
.btn-list { display: flex; gap: 10px; }
.search-panel { width: 300px; .icon-search { cursor: pointer; color: #999; &:hover { color: #05a1f5; } } }

/* 面包屑导航样式 */
.breadcrumb {
  margin-bottom: 15px; font-size: 14px; color: #606266;
  .nav-item { cursor: pointer; color: #05a1f5; &:hover { text-decoration: underline; } }
  .separator { margin: 0 8px; color: #909399; }
}

.file-list {
  background: #fff; border-radius: 5px;
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

    .file-name { cursor: pointer; color: #4a4a4a; transition: color 0.3s; &:hover { color: #05a1f5; } }
  }
}
.upload-progress { margin-top: 20px; .progress-info { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 14px; } }
</style>