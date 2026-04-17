<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { createPost, deletePost, listPosts, updatePost } from '@/api/medir'
import type { MedirPost } from '@/api/medir/types'
import { getAxiosErrorMessage } from '@/api/client'

const rows = ref<MedirPost[]>([])
const loading = ref(false)
const error = ref('')
const editing = ref<MedirPost | null>(null)
const form = ref({
  postCode: '',
  postName: '',
  description: '',
  sortOrder: 0 as number,
  enabled: 1 as number,
})

async function loadPosts() {
  loading.value = true
  error.value = ''
  try {
    rows.value = await listPosts()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  } finally {
    loading.value = false
  }
}

onMounted(loadPosts)

function openCreate() {
  editing.value = null
  form.value = { postCode: '', postName: '', description: '', sortOrder: 0, enabled: 1 }
  ;(document.getElementById('post_modal') as HTMLDialogElement)?.showModal()
}

function openEdit(row: MedirPost) {
  editing.value = row
  form.value = {
    postCode: row.postCode,
    postName: row.postName,
    description: row.description ?? '',
    sortOrder: row.sortOrder,
    enabled: row.enabled,
  }
  ;(document.getElementById('post_modal') as HTMLDialogElement)?.showModal()
}

async function save() {
  error.value = ''
  try {
    const body = {
      postCode: form.value.postCode,
      postName: form.value.postName,
      description: form.value.description || null,
      sortOrder: form.value.sortOrder,
      enabled: form.value.enabled,
    }
    if (editing.value) {
      await updatePost(editing.value.id, body)
    } else {
      await createPost(body)
    }
    ;(document.getElementById('post_modal') as HTMLDialogElement)?.close()
    await loadPosts()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
}

async function remove(row: MedirPost) {
  if (!confirm(`删除岗位「${row.postName}」？`)) return
  error.value = ''
  try {
    await deletePost(row.id)
    await loadPosts()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
}
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between gap-2 flex-wrap">
      <h1 class="text-2xl font-semibold">岗位</h1>
      <p class="text-sm opacity-70">全局岗位字典，无按班组筛选。</p>
      <button type="button" class="btn btn-primary btn-sm" @click="openCreate">新建</button>
    </div>

    <div v-if="error" class="alert alert-error text-sm">{{ error }}</div>
    <div v-if="loading" class="loading loading-spinner" />

    <div v-else class="overflow-x-auto">
      <table class="table table-zebra table-sm bg-base-100 rounded-box">
        <thead>
          <tr>
            <th>ID</th>
            <th>编码</th>
            <th>名称</th>
            <th>排序</th>
            <th>启用</th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in rows" :key="r.id">
            <td>{{ r.id }}</td>
            <td class="font-mono">{{ r.postCode }}</td>
            <td>{{ r.postName }}</td>
            <td>{{ r.sortOrder }}</td>
            <td>{{ r.enabled === 1 ? '是' : '否' }}</td>
            <td class="text-right space-x-1">
              <button type="button" class="btn btn-ghost btn-xs" @click="openEdit(r)">编辑</button>
              <button type="button" class="btn btn-ghost btn-xs text-error" @click="remove(r)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <dialog id="post_modal" class="modal">
      <div class="modal-box">
        <h3 class="font-bold text-lg">{{ editing ? '编辑岗位' : '新建岗位' }}</h3>
        <label class="form-control w-full">
          <span class="label-text">postCode</span>
          <input v-model="form.postCode" class="input input-bordered w-full font-mono" />
        </label>
        <label class="form-control w-full">
          <span class="label-text">postName</span>
          <input v-model="form.postName" class="input input-bordered w-full" />
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
