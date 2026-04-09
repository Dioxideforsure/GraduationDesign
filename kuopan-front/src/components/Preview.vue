<template>
  <el-dialog
      v-model="dialogVisible"
      :title="fileInfo.fileName"
      width="80%"
      top="5vh"
      @close="closePreview"
      destroy-on-close
  >
    <div class="preview-container">
      <video
          v-if="previewType === 'video'"
          ref="videoPlayer"
          controls
          autoplay
          class="video-player"
      ></video>

      <el-image
          v-else-if="previewType === 'image'"
          :src="fileUrl"
          fit="contain"
          class="image-preview"
      />

      <iframe
          v-else-if="previewType === 'pdf'"
          :src="fileUrl"
          class="pdf-preview"
          frameborder="0"
      ></iframe>

      <div v-else-if="previewType === 'text'" class="text-preview">
        <pre>{{ txtContent }}</pre>
      </div>

      <div v-else class="unsupported">
        <span class="iconfont icon-file" style="font-size: 60px; margin-bottom: 20px; color: #b0b0b0;"></span>
        <p style="margin-bottom: 15px;">该文件类型 ({{ fileExt }}) 暂不支持在线预览</p>
        <el-button type="primary" @click="downloadFile">直接下载</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, nextTick } from 'vue';

const dialogVisible = ref(false);
const fileInfo = ref({});
const fileUrl = ref("");
const videoPlayer = ref(null);
let hlsInstance = null;

const previewType = ref("unsupported"); // video, image, pdf, text, unsupported
const fileExt = ref("");
const txtContent = ref("");

// 纯文本/代码文件后缀
const textExtensions = ['txt', 'json', 'js', 'vue', 'html', 'css', 'java', 'xml', 'md', 'py', 'sql', 'yml', 'properties'];

const show = async (file) => {
  fileInfo.value = file;
  dialogVisible.value = true;
  txtContent.value = "正在加载文档内容...";

  const fileName = file.fileName || "";
  fileExt.value = fileName.includes('.') ? fileName.split('.').pop().toLowerCase() : "";

  if (file.fileCategory == 1) {
    previewType.value = "video";
    const baseFolder = file.filePath.substring(0, file.filePath.lastIndexOf('.'));
    fileUrl.value = `/api/file_res/${baseFolder}/index.m3u8`;
    nextTick(() => { initVideoPlayer(); });
  } else if (file.fileCategory == 3) {
    previewType.value = "image";
    fileUrl.value = `/api/file_res/${file.filePath}`;
  } else if (fileExt.value === 'pdf') {
    previewType.value = "pdf";
    fileUrl.value = `/api/file_res/${file.filePath}`;
  } else if (textExtensions.includes(fileExt.value)) {
    previewType.value = "text";
    fileUrl.value = `/api/file_res/${file.filePath}`;
    await fetchTextContent(fileUrl.value);
  } else {
    previewType.value = "unsupported";
    fileUrl.value = `/api/file_res/${file.filePath}`;
  }
};

const fetchTextContent = async (url) => {
  try {
    const response = await fetch(url);
    if (!response.ok) throw new Error("网络响应异常");
    const text = await response.text();
    txtContent.value = text;
  } catch (error) {
    console.error("读取文本失败", error);
    txtContent.value = "加载文本内容失败，可能文件编码不受支持。";
  }
};

const initVideoPlayer = () => {
  const video = videoPlayer.value;
  if (window.Hls && window.Hls.isSupported()) {
    hlsInstance = new window.Hls();
    hlsInstance.loadSource(fileUrl.value);
    hlsInstance.attachMedia(video);
  } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
    video.src = fileUrl.value;
  }
};

const downloadFile = () => {
  const link = document.createElement("a");
  link.href = fileUrl.value;
  link.download = fileInfo.value.fileName;
  link.click();
};

const closePreview = () => {
  if (hlsInstance) {
    hlsInstance.destroy();
    hlsInstance = null;
  }
  dialogVisible.value = false;
  previewType.value = "unsupported";
  txtContent.value = "";
};

defineExpose({ show });
</script>

<style lang="scss" scoped>
:deep(.el-dialog__body) { padding: 0; }
.preview-container { display: flex; flex-direction: column; justify-content: center; align-items: center; height: 75vh; background-color: #f5f7fa; overflow: hidden; }
.video-player { width: 100%; height: 100%; background-color: #000; outline: none; }
.image-preview { width: 100%; height: 100%; }
.pdf-preview { width: 100%; height: 100%; border: none; background-color: #525659; }
.text-preview { width: 100%; height: 100%; background-color: #1e1e1e; color: #d4d4d4; padding: 20px; overflow-y: auto; box-sizing: border-box; pre { margin: 0; font-family: Consolas, Monaco, 'Courier New', monospace; font-size: 14px; line-height: 1.6; white-space: pre-wrap; word-break: break-all; } }
.unsupported { display: flex; flex-direction: column; align-items: center; justify-content: center; color: #909399; font-size: 16px; height: 100%; }
</style>
