<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listRuleMeta } from '@/api/medir'
import type { MedirRuleMeta } from '@/api/medir/types'
import { getAxiosErrorMessage } from '@/api/client'

const rows = ref<MedirRuleMeta[]>([])
const loading = ref(false)
const error = ref('')

onMounted(async () => {
  loading.value = true
  try {
    rows.value = await listRuleMeta()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="space-y-4">
    <h1 class="text-2xl font-semibold">规则元数据</h1>
    <p class="text-sm opacity-70">RuleMetaResponse，供配置页按 ruleCode 渲染。</p>

    <div v-if="error" class="alert alert-error text-sm">{{ error }}</div>
    <div v-if="loading" class="loading loading-spinner" />

    <div v-else class="overflow-x-auto">
      <table class="table table-zebra table-sm bg-base-100 rounded-box text-xs">
        <thead>
          <tr>
            <th>ID</th>
            <th>ruleCode</th>
            <th>category</th>
            <th>labelZh</th>
            <th>valueType</th>
            <th>helpText</th>
            <th>sort</th>
            <th>en</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in rows" :key="r.id">
            <td>{{ r.id }}</td>
            <td class="font-mono">{{ r.ruleCode }}</td>
            <td>{{ r.category }}</td>
            <td>{{ r.labelZh }}</td>
            <td>{{ r.valueType }}</td>
            <td class="max-w-xs truncate">{{ r.helpText || '—' }}</td>
            <td>{{ r.sortOrder }}</td>
            <td>{{ r.enabled }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
