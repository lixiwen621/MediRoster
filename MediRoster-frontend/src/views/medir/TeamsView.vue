<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { createTeam, deleteTeam, listTeams, updateTeam } from '@/api/medir'
import type { MedirTeam } from '@/api/medir/types'
import { getAxiosErrorMessage } from '@/api/client'

const rows = ref<MedirTeam[]>([])
const loading = ref(false)
const error = ref('')
const editing = ref<MedirTeam | null>(null)
const form = ref({
  teamCode: '',
  teamName: '',
  description: '',
  enabled: 1 as number,
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    rows.value = await listTeams()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  } finally {
    loading.value = false
  }
}

onMounted(load)

function openCreate() {
  editing.value = null
  form.value = { teamCode: '', teamName: '', description: '', enabled: 1 }
  ;(document.getElementById('team_modal') as HTMLDialogElement)?.showModal()
}

function openEdit(row: MedirTeam) {
  editing.value = row
  form.value = {
    teamCode: row.teamCode,
    teamName: row.teamName,
    description: row.description ?? '',
    enabled: row.enabled,
  }
  ;(document.getElementById('team_modal') as HTMLDialogElement)?.showModal()
}

async function save() {
  error.value = ''
  try {
    const body = {
      teamCode: form.value.teamCode,
      teamName: form.value.teamName,
      description: form.value.description || null,
      enabled: form.value.enabled,
    }
    if (editing.value) {
      await updateTeam(editing.value.id, body)
    } else {
      await createTeam(body)
    }
    ;(document.getElementById('team_modal') as HTMLDialogElement)?.close()
    await load()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
}

async function remove(row: MedirTeam) {
  if (!confirm(`删除班组「${row.teamName}」？`)) return
  error.value = ''
  try {
    await deleteTeam(row.id)
    await load()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
}
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between gap-2 flex-wrap">
      <h1 class="text-2xl font-semibold">班组</h1>
      <button type="button" class="btn btn-primary btn-sm" @click="openCreate">新建</button>
    </div>

    <div v-if="error" class="alert alert-error text-sm">{{ error }}</div>

    <div v-if="loading" class="loading loading-spinner" />

    <div v-else class="overflow-x-auto">
      <table class="table table-zebra table-sm bg-base-100 rounded-box">
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>编码</th>
            <th>备注</th>
            <th>启用</th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in rows" :key="r.id">
            <td>{{ r.id }}</td>
            <td>{{ r.teamName }}</td>
            <td class="font-mono text-xs">{{ r.teamCode }}</td>
            <td class="max-w-xs truncate">{{ r.description || '—' }}</td>
            <td>{{ r.enabled === 1 ? '是' : '否' }}</td>
            <td class="text-right space-x-1">
              <button type="button" class="btn btn-ghost btn-xs" @click="openEdit(r)">编辑</button>
              <button type="button" class="btn btn-ghost btn-xs text-error" @click="remove(r)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <dialog id="team_modal" class="modal">
      <div class="modal-box">
        <h3 class="font-bold text-lg">{{ editing ? '编辑班组' : '新建班组' }}</h3>
        <label class="form-control w-full">
          <span class="label-text">teamName</span>
          <input v-model="form.teamName" class="input input-bordered w-full" />
        </label>
        <label class="form-control w-full">
          <span class="label-text">teamCode</span>
          <input v-model="form.teamCode" class="input input-bordered w-full font-mono" />
        </label>
        <label class="form-control w-full">
          <span class="label-text">description</span>
          <input v-model="form.description" class="input input-bordered w-full" />
        </label>
        <label class="form-control w-full">
          <span class="label-text">enabled（0/1）</span>
          <select v-model.number="form.enabled" class="select select-bordered w-full">
            <option :value="1">1 启用</option>
            <option :value="0">0 停用</option>
          </select>
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
