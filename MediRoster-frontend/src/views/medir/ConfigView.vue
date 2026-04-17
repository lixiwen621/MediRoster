<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { createConfig, deleteConfig, listConfig, listTeams, updateConfig } from '@/api/medir'
import type { MedirConfig, MedirTeam } from '@/api/medir/types'
import { getAxiosErrorMessage } from '@/api/client'

const teams = ref<MedirTeam[]>([])
const filterTeam = ref<number | '' | 'all'>('all')
const rows = ref<MedirConfig[]>([])
const loading = ref(false)
const error = ref('')
const editing = ref<MedirConfig | null>(null)
const form = ref({
  teamId: 0,
  configKey: '',
  configValue: '',
  valueType: 'string',
  category: '',
  description: '',
  sortOrder: 0,
  enabled: 1,
})

async function loadTeams() {
  teams.value = await listTeams()
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const params = filterTeam.value === 'all' ? undefined : { teamId: Number(filterTeam.value) }
    rows.value = await listConfig(params)
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    await loadTeams()
    await load()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
})

watch(filterTeam, () => load())

function openCreate() {
  editing.value = null
  form.value = {
    teamId: 0,
    configKey: '',
    configValue: '',
    valueType: 'string',
    category: '',
    description: '',
    sortOrder: 0,
    enabled: 1,
  }
  ;(document.getElementById('cfg_modal') as HTMLDialogElement)?.showModal()
}

function openEdit(row: MedirConfig) {
  editing.value = row
  form.value = {
    teamId: row.teamId,
    configKey: row.configKey,
    configValue: row.configValue,
    valueType: row.valueType,
    category: row.category ?? '',
    description: row.description ?? '',
    sortOrder: row.sortOrder,
    enabled: row.enabled,
  }
  ;(document.getElementById('cfg_modal') as HTMLDialogElement)?.showModal()
}

async function save() {
  error.value = ''
  try {
    const body = {
      teamId: form.value.teamId,
      configKey: form.value.configKey,
      configValue: form.value.configValue,
      valueType: form.value.valueType,
      category: form.value.category || null,
      description: form.value.description || null,
      sortOrder: form.value.sortOrder,
      enabled: form.value.enabled,
    }
    if (editing.value) {
      await updateConfig(editing.value.id, body)
    } else {
      await createConfig(body)
    }
    ;(document.getElementById('cfg_modal') as HTMLDialogElement)?.close()
    await load()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
}

async function remove(row: MedirConfig) {
  if (!confirm(`删除配置「${row.configKey}」？`)) return
  error.value = ''
  try {
    await deleteConfig(row.id)
    await load()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
}
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between gap-2 flex-wrap">
      <h1 class="text-2xl font-semibold">规则配置（medir_config）</h1>
      <div class="flex items-center gap-2">
        <label class="form-control">
          <span class="label-text">班组筛选</span>
          <select v-model="filterTeam" class="select select-bordered select-sm">
            <option value="all">全部</option>
            <option :value="0">全局 (0)</option>
            <option v-for="t in teams" :key="t.id" :value="t.id">{{ t.teamName }}</option>
          </select>
        </label>
        <button type="button" class="btn btn-primary btn-sm" @click="openCreate">新建</button>
      </div>
    </div>

    <div v-if="error" class="alert alert-error text-sm">{{ error }}</div>
    <div v-if="loading" class="loading loading-spinner" />

    <div v-else class="overflow-x-auto">
      <table class="table table-zebra table-sm bg-base-100 rounded-box text-xs">
        <thead>
          <tr>
            <th>ID</th>
            <th>teamId</th>
            <th>键</th>
            <th>值</th>
            <th>valueType</th>
            <th>sort</th>
            <th>en</th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in rows" :key="r.id">
            <td>{{ r.id }}</td>
            <td>{{ r.teamId }}</td>
            <td class="font-mono">{{ r.configKey }}</td>
            <td class="max-w-xs truncate" :title="r.configValue">{{ r.configValue }}</td>
            <td>{{ r.valueType }}</td>
            <td>{{ r.sortOrder }}</td>
            <td>{{ r.enabled }}</td>
            <td class="text-right space-x-1">
              <button type="button" class="btn btn-ghost btn-xs" @click="openEdit(r)">编辑</button>
              <button type="button" class="btn btn-ghost btn-xs text-error" @click="remove(r)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <dialog id="cfg_modal" class="modal">
      <div class="modal-box max-w-2xl">
        <h3 class="font-bold text-lg">{{ editing ? '编辑配置' : '新建配置' }}</h3>
        <label class="form-control w-full">
          <span class="label-text">teamId（0=全局）</span>
          <input v-model.number="form.teamId" type="number" class="input input-bordered w-full" :disabled="!!editing" />
        </label>
        <label class="form-control w-full">
          <span class="label-text">configKey</span>
          <input v-model="form.configKey" class="input input-bordered w-full font-mono" :disabled="!!editing" />
        </label>
        <label class="form-control w-full">
          <span class="label-text">configValue</span>
          <textarea v-model="form.configValue" class="textarea textarea-bordered font-mono text-xs w-full min-h-24" />
        </label>
        <label class="form-control w-full">
          <span class="label-text">valueType</span>
          <input v-model="form.valueType" class="input input-bordered w-full font-mono" placeholder="string / json / number" />
        </label>
        <label class="form-control w-full">
          <span class="label-text">sortOrder</span>
          <input v-model.number="form.sortOrder" type="number" class="input input-bordered w-full" />
        </label>
        <label class="form-control w-full">
          <span class="label-text">enabled（0/1）</span>
          <select v-model.number="form.enabled" class="select select-bordered w-full">
            <option :value="1">1</option>
            <option :value="0">0</option>
          </select>
        </label>
        <label class="form-control w-full">
          <span class="label-text">category</span>
          <input v-model="form.category" class="input input-bordered w-full" />
        </label>
        <label class="form-control w-full">
          <span class="label-text">description</span>
          <input v-model="form.description" class="input input-bordered w-full" />
        </label>
        <div class="modal-action">
          <form method="dialog">
            <button class="btn">取消</button>
          </form>
          <button type="button" class="btn btn-primary" @click="save">保存</button>
        </div>
      </div>
    </dialog>
  </div>
</template>
