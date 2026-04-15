<template>
  <div class="share-list">
    <div class="top-op">
      <el-button type="primary" :disabled="selectIdList.length === 0" @click="cancelShareBatch">
        <span class="iconfont icon-cancel"></span> 批量取消分享
      </el-button>
    </div>
    
    <el-table :data="tableData" stripe style="width: 100%" v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="fileName" label="文件名" min-width="250">
        <template #default="scope">
          <div class="file-item">
            <template v-if="getThumbnailUrl(scope.row)">
              <el-image class="file-cover" :src="getThumbnailUrl(scope.row)" fit="cover">
                <template #error><div class="image-slot"><img class="file-icon" :src="getFileIcon(scope.row)" /></div></template>
              </el-image>
            </template>
            <template v-else>
              <img class="file-icon" :src="getFileIcon(scope.row)" />
            </template>
            <span class="file-name">{{ scope.row.fileName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="shareTime" label="分享时间" width="180" />
      <el-table-column prop="expireTime" label="失效时间" width="180" />
      <el-table-column prop="code" label="提取码" width="100" />
      <el-table-column prop="browseCount" label="浏览次数" width="100" />
      <el-table-column label="操作" width="150" align="center">
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
import { ElMessageBox } from 'element-plus';
const { proxy } = getCurrentInstance();

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

// 👇 接入真实接口 👇
const loadDataList = async () => {
  loading.value = true;
  let result = await proxy.Request({ url: "/share/loadShareList", showLoading: false });
  loading.value = false;
  if (result) tableData.value = result.data;
};

const handleSelectionChange = (val) => { selectIdList.value = val.map(item => item.shareId); };

const copyLink = async (row) => {
  const host = window.location.origin;
  const text = `链接: ${host}/shareCheck/${row.shareId} 提取码: ${row.code}`;
  try {
    await navigator.clipboard.writeText(text);
    proxy.Message.success("链接及提取码已复制到剪贴板");
  } catch (err) {
    proxy.Message.error("复制失败，请手动复制");
  }
};

const cancelShare = (row) => {
  ElMessageBox.confirm(`确定取消分享【${row.fileName}】吗？`, '提示', { type: 'warning' }).then(async () => {
    let result = await proxy.Request({ url: "/share/cancelShare", params: { shareIds: row.shareId } });
    if (result) { proxy.Message.success("已取消分享"); loadDataList(); }
  }).catch(() => {});
};

const cancelShareBatch = () => {
  ElMessageBox.confirm(`确定批量取消这 ${selectIdList.value.length} 个分享吗？`, '提示', { type: 'warning' }).then(async () => {
    let result = await proxy.Request({ url: "/share/cancelShare", params: { shareIds: selectIdList.value.join(",") } });
    if (result) { proxy.Message.success("批量取消成功"); loadDataList(); }
  }).catch(() => {});
};

loadDataList();
</script>

<style lang="scss" scoped>
.share-list { padding-right: 20px; }
.top-op { margin-top: 20px; margin-bottom: 20px; }
.file-item { 
  display: flex; align-items: center; 
  .file-icon { width: 32px; height: 32px; margin-right: 15px; flex-shrink: 0; }
  .file-cover { width: 36px; height: 36px; border-radius: 4px; margin-right: 15px; flex-shrink: 0; background-color: #f0f0f0; display: flex; align-items: center; justify-content: center; .image-slot { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; } }
  .file-name { color: #4a4a4a; } 
}
</style>