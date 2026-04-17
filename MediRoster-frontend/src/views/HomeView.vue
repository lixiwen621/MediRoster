<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getHealth } from '@/api/medir'
import { getAxiosErrorMessage } from '@/api/client'

const loading = ref(false)
const error = ref('')
const payload = ref<unknown>(null)

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    payload.value = await getHealth()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="space-y-4">
    <h1 class="text-2xl font-semibold">欢迎使用 MediRoster</h1>
    <p class="opacity-80 max-w-2xl">
      本前端对接 MediRoster v1 REST（班组、岗位、班次、人员、规则配置、排班周与日历日）。开发环境通过 Vite 代理访问
      <code class="mx-1">/api</code>
      ，默认后端端口 8084。
    </p>

    <div v-if="loading" class="loading loading-spinner loading-md" />

    <div v-else-if="error" role="alert" class="alert alert-warning max-w-xl">
      <span>健康检查失败（后端未启动或地址不对）：{{ error }}</span>
    </div>

    <div v-else class="card bg-base-100 shadow max-w-xl">
      <div class="card-body">
        <h2 class="card-title text-base">GET /api/v1/health</h2>
        <pre class="text-xs overflow-auto bg-base-200 p-3 rounded">{{ JSON.stringify(payload, null, 2) }}</pre>
      </div>
    </div>
  </div>
</template>
