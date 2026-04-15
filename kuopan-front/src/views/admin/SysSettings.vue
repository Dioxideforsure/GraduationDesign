<template>
  <div class="sys-settings-wrap">
    <el-card shadow="hover" class="card-container">
      <template #header>
        <div class="card-header">
          <span><i class="iconfont icon-setting"></i> 系统全局参数设置</span>
        </div>
      </template>

      <el-form
          :model="formData"
          :rules="rules"
          ref="formDataRef"
          label-width="150px"
          style="max-width: 800px"
      >
        <el-divider content-position="left">邮件验证码设置</el-divider>
        <el-form-item label="邮件标题" prop="registerEmailTitle">
          <el-input v-model="formData.registerEmailTitle" placeholder="请输入注册/找回密码邮件的标题" />
        </el-form-item>
        <el-form-item label="邮件内容模板" prop="registerEmailContent">
          <el-input
              type="textarea"
              :rows="4"
              v-model="formData.registerEmailContent"
              placeholder="内容中必须包含 %s 占位符代表验证码"
          />
          <div class="tips">提示：系统发送时会自动将 <code>%s</code> 替换为6位验证码数字</div>
        </el-form-item>

        <el-divider content-position="left">存储空间设置</el-divider>
        <el-form-item label="新用户初始空间" prop="userInitUseSpace">
          <el-input v-model.number="formData.userInitUseSpace" placeholder="请输入MB单位数值">
            <template #append>MB</template>
          </el-input>
          <div class="tips">新注册用户默认分配的容量（1024MB = 1GB）</div>
        </el-form-item>

        <el-form-item style="margin-top: 30px">
          <el-button type="primary" size="large" @click="saveSettings">保存系统配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, onMounted } from 'vue';

const { proxy } = getCurrentInstance();

const formData = reactive({
  registerEmailTitle: '',
  registerEmailContent: '',
  userInitUseSpace: 1024,
});

const formDataRef = ref();

const rules = {
  registerEmailTitle: [{ required: true, message: '请输入邮件标题' }],
  registerEmailContent: [
    { required: true, message: '请输入模板内容' },
    {
      validator: (rule, value, callback) => {
        if (value.indexOf('%s') === -1) {
          callback(new Error('内容模板必须包含 %s 占位符'));
        } else {
          callback();
        }
      },
    },
  ],
  userInitUseSpace: [
    { required: true, message: '请输入初始空间' },
    { type: 'number', message: '空间大小必须为数字' },
  ],
};

// 获取配置
const getSettings = async () => {
  let result = await proxy.Request({
    url: '/admin/getSysSettings',
  });
  if (!result) return;
  Object.assign(formData, result.data);
};

// 保存配置
const saveSettings = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) return;
    let result = await proxy.Request({
      url: '/admin/saveSysSettings',
      params: formData,
    });
    if (result) {
      proxy.Message.success('保存成功！设置已即时生效');
    }
  });
};

onMounted(() => {
  getSettings();
});
</script>

<style lang="scss" scoped>
.sys-settings-wrap {
  padding: 20px;
  .card-container {
    border-radius: 8px;
  }
  .card-header {
    font-weight: bold;
    font-size: 18px;
    color: #409eff;
  }
  .tips {
    color: #909399;
    font-size: 12px;
    line-height: 1.5;
    margin-top: 5px;
    code {
      background: #f4f4f5;
      padding: 2px 4px;
      color: #f56c6c;
      border-radius: 4px;
    }
  }
}
</style>