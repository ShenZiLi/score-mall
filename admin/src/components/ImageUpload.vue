<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadFile } from '../api/upload'

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  // 上传图片的预览尺寸
  width: {
    type: Number,
    default: 148,
  },
  height: {
    type: Number,
    default: 148,
  },
})

const emit = defineEmits(['update:modelValue'])

const uploading = ref(false)

function customRequest({ file }) {
  uploading.value = true
  uploadFile(file)
    .then((res) => {
      // res.data 为 { url }
      const url = res.data?.url || res.data
      emit('update:modelValue', url)
      ElMessage.success('上传成功')
    })
    .catch(() => {
      ElMessage.error('上传失败')
    })
    .finally(() => {
      uploading.value = false
    })
}

function handleRemove() {
  emit('update:modelValue', '')
}
</script>

<template>
  <div class="image-uploader">
    <el-upload
      :show-file-list="false"
      :http-request="customRequest"
      accept="image/*"
    >
      <div
        v-if="modelValue"
        class="image-preview"
        :style="{ width: width + 'px', height: height + 'px' }"
      >
        <img :src="modelValue" class="preview-img" />
        <div class="image-actions">
          <span class="action-item" @click.stop="handleRemove">删除</span>
        </div>
      </div>
      <div
        v-else
        class="image-placeholder"
        :style="{ width: width + 'px', height: height + 'px' }"
        v-loading="uploading"
      >
        <el-icon class="placeholder-icon"><Plus /></el-icon>
      </div>
    </el-upload>
  </div>
</template>

<style scoped>
.image-preview,
.image-placeholder {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
}

.image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  background: #fafafa;
}

.placeholder-icon {
  font-size: 28px;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.image-actions {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.image-preview:hover .image-actions {
  opacity: 1;
}

.action-item {
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}
</style>
