<template>
  <div class="share-check-wrap">
    <div class="check-card" v-if="shareInfo">
      <div class="header">
        <div class="avatar">{{ shareInfo.nickName ? shareInfo.nickName.substring(0,1) : 'U' }}</div>
        <div class="info">
          <div class="name">{{ shareInfo.nickName }} 的私密分享</div>
          <div class="time">{{ shareInfo.shareTime }}</div>
        </div>
      </div>

      <div class="body" v-if="!isPassed">
        <div class="tip">请输入提取码：</div>
        <div class="input-wrap">
          <el-input v-model="code" placeholder="请输入5位提取码" maxlength="5" @keyup.enter="checkCode" />
          <el-button type="primary" @click="checkCode" :loading="loading">提取文件</el-button>
        </div>
      </div>

      <div class="body passed" v-else>
        <div class="success-tip"><el-icon size="50" color="#67C23A"><CircleCheckFilled /></el-icon></div>
        <h3>文件提取成功！</h3>
        <p style="color: #909399; font-size: 14px;">（这里后续可以展示文件列表和下载按钮）</p>
      </div>
    </div>

    <div class="error-card" v-else-if="!initLoading">
      <h2>啊哦，你来晚了...</h2>
      <p>该分享链接不存在或已失效</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance } from 'vue';
import { useRoute } from 'vue-router';
import { CircleCheckFilled } from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();
const route = useRoute();

const shareId = route.params.shareId;
const shareInfo = ref(null);
const code = ref('');
const loading = ref(false);
const initLoading = ref(true);
const isPassed = ref(false);

// 初始化获取分享信息
const getShareInfo = async () => {
  let result = await proxy.Request({
    url: "/showShare/getShareInfo",
    params: { shareId }
  });
  initLoading.value = false;
  if (result && result.data) {
    shareInfo.value = result.data;
  }
};

// 校验提取码
const checkCode = async () => {
  if (!code.value || code.value.length !== 5) {
    proxy.Message.warning("请输入完整的5位提取码");
    return;
  }
  loading.value = true;
  let result = await proxy.Request({
    url: "/showShare/checkShareCode",
    params: { shareId, code: code.value }
  });
  loading.value = false;
  if (result) {
    proxy.Message.success("提取成功");
    isPassed.value = true;
  }
};

onMounted(() => {
  if (shareId) {
    getShareInfo();
  }
});
</script>

<style lang="scss" scoped>
.share-check-wrap {
  min-height: 100vh;
  background: #f0f2f5;
  display: flex;
  justify-content: center;
  align-items: center;

  .check-card, .error-card {
    width: 450px;
    background: #fff;
    border-radius: 10px;
    box-shadow: 0 4px 15px rgba(0,0,0,0.05);
    padding: 30px;
  }

  .header {
    display: flex;
    align-items: center;
    border-bottom: 1px solid #ebeef5;
    padding-bottom: 20px;
    margin-bottom: 20px;

    .avatar {
      width: 50px; height: 50px; border-radius: 50%; background: #409EFF;
      color: #fff; display: flex; justify-content: center; align-items: center;
      font-size: 20px; font-weight: bold; margin-right: 15px;
    }
    .info {
      .name { font-size: 16px; font-weight: bold; color: #333; margin-bottom: 5px; }
      .time { font-size: 13px; color: #909399; }
    }
  }

  .body {
    .tip { font-size: 14px; color: #606266; margin-bottom: 10px; }
    .input-wrap {
      display: flex; gap: 10px;
      .el-input { flex: 1; }
    }
    &.passed {
      text-align: center;
      h3 { margin: 15px 0 10px; color: #303133; }
    }
  }

  .error-card {
    text-align: center;
    padding: 50px 30px;
    h2 { color: #f56c6c; margin-bottom: 10px; }
    p { color: #909399; }
  }
}
</style>