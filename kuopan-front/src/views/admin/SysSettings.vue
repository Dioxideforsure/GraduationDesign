<template>
  <div class="sys-settings">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>系统基础设置</span>
        </div>
      </template>
      <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="150px" class="settings-form">
        <el-form-item label="注册邮件标题" prop="registerEmailTitle">
          <el-input v-model="formData.registerEmailTitle" placeholder="请输入注册邮件验证码标题" />
        </el-form-item>
        
        <el-form-item label="注册邮件内容" prop="registerEmailContent">
          <el-input 
            v-model="formData.registerEmailContent" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入注册邮件验证码内容，验证码使用 %s 占位" 
          />
        </el-form-item>
        
        <el-form-item label="初始网盘空间大小" prop="userInitUseSpace">
          <el-input v-model.number="formData.userInitUseSpace" placeholder="请输入用户初始网盘空间大小">
            <template #append>MB</template>
          </el-input>
          <div class="tips">新用户注册时默认分配的可用空间大小 ( 1024 MB = 1 GB )</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="saveSettings">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, getCurrentInstance } from "vue";
const { proxy } = getCurrentInstance();

const formDataRef = ref();
const formData = ref({});

const rules = {
  registerEmailTitle: [{ required: true, message: "请输入邮件标题", trigger: "blur" }],
  registerEmailContent: [{ required: true, message: "请输入邮件内容", trigger: "blur" }],
  userInitUseSpace: [
    { required: true, message: "请输入初始空间大小", trigger: "blur" },
    { type: 'number', message: "空间大小必须为数字", trigger: "blur" }
  ]
};

// 模拟加载系统设置
const loadSettings = () => {
  // 模拟从后端获取数据
  setTimeout(() => {
    formData.value = {
      registerEmailTitle: "【KuoPan】邮箱验证码",
      registerEmailContent: "您好，您的注册验证码是：%s，有效期5分钟。如非本人操作请忽略。",
      userInitUseSpace: 1024 // 默认给 1GB
    };
  }, 300);
};

const saveSettings = () => {
  formDataRef.value.validate((valid) => {
    if (valid) {
      // 发送请求保存到后端
      proxy.Message.success("系统设置保存成功 (待对接后端)");
      console.log("保存的数据:", formData.value);
    }
  });
};

loadSettings();
</script>

<style lang="scss" scoped>
.sys-settings {
  padding-right: 20px;
  padding-top: 20px;
  
  .box-card {
    width: 60%;
    min-width: 600px;
  }
  
  .card-header {
    font-weight: bold;
    color: #333;
  }

  .tips {
    color: #888;
    font-size: 12px;
    margin-top: 5px;
    line-height: 1.5;
  }
}
</style>