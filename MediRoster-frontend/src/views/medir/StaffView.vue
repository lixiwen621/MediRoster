<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import {
  addCapability,
  createStaff,
  deleteCapability,
  deleteStaff,
  listCapabilities,
  listStaff,
  listTeams,
  updateStaff,
} from '@/api/medir'
import type { MedirStaff, MedirStaffCapability, MedirTeam } from '@/api/medir/types'
import { getAxiosErrorMessage } from '@/api/client'

const teams = ref<MedirTeam[]>([])
const teamId = ref<number | ''>('')
const rows = ref<MedirStaff[]>([])
const loading = ref(false)
const error = ref('')
const editing = ref<MedirStaff | null>(null)
const form = ref({
  name: '',
  status: 1 as 1 | 2,
  memberType: 'FIXED',
  sortOrder: 0,
  employeeNo: '',
  phone: '',
  email: '',
  fixedPostId: '',
  remark: '',
})

const expandedId = ref<number | null>(null)
const caps = ref<MedirStaffCapability[]>([])
const capLoading = ref(false)
const newCap = ref('')

const statusLabel = (s: number) => (s === 1 ? '在职' : '停用')

async function loadTeams() {
  teams.value = await listTeams()
  if (teams.value.length && teamId.value === '') teamId.value = teams.value[0].id
}

async function loadStaff() {
  if (teamId.value === '') return
  loading.value = true
  error.value = ''
  try {
    rows.value = await listStaff({ teamId: Number(teamId.value) })
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    await loadTeams()
    await loadStaff()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
})

watch(teamId, () => {
  expandedId.value = null
  loadStaff()
})

async function toggleCaps(row: MedirStaff) {
  if (expandedId.value === row.id) {
    expandedId.value = null
    return
  }
  expandedId.value = row.id
  capLoading.value = true
  caps.value = []
  try {
    caps.value = await listCapabilities(row.id)
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  } finally {
    capLoading.value = false
  }
}

function openCreate() {
  if (teamId.value === '') return
  editing.value = null
  form.value = {
    name: '',
    status: 1,
    memberType: 'FIXED',
    sortOrder: 0,
    employeeNo: '',
    phone: '',
    email: '',
    fixedPostId: '',
    remark: '',
  }
  ;(document.getElementById('staff_modal') as HTMLDialogElement)?.showModal()
}

function openEdit(row: MedirStaff) {
  editing.value = row
  form.value = {
    name: row.name,
    status: row.status,
    memberType: row.memberType,
    sortOrder: row.sortOrder,
    employeeNo: row.employeeNo ?? '',
    phone: row.phone ?? '',
    email: row.email ?? '',
    fixedPostId: row.fixedPostId != null ? String(row.fixedPostId) : '',
    remark: row.remark ?? '',
  }
  ;(document.getElementById('staff_modal') as HTMLDialogElement)?.showModal()
}

function buildUpsertBody() {
  const tid = editing.value ? editing.value.teamId : Number(teamId.value)
  return {
    teamId: tid,
    name: form.value.name,
    status: form.value.status,
    memberType: form.value.memberType,
    sortOrder: form.value.sortOrder,
    employeeNo: form.value.employeeNo.trim() || null,
    phone: form.value.phone.trim() || null,
    email: form.value.email.trim() || null,
    fixedPostId: form.value.fixedPostId.trim() === '' ? null : Number(form.value.fixedPostId),
    remark: form.value.remark.trim() || null,
  }
}

async function save() {
  if (teamId.value === '') return
  error.value = ''
  try {
    if (editing.value) {
      await updateStaff(editing.value.id, buildUpsertBody())
    } else {
      await createStaff(buildUpsertBody())
    }
    ;(document.getElementById('staff_modal') as HTMLDialogElement)?.close()
    await loadStaff()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
}

async function remove(row: MedirStaff) {
  if (!confirm(`停用/删除人员「${row.name}」？`)) return
  error.value = ''
  try {
    await deleteStaff(row.id)
    await loadStaff()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
}

const expandedStaff = computed(() => rows.value.find((s) => s.id === expandedId.value))

async function addCap() {
  if (!expandedStaff.value || !newCap.value.trim()) return
  error.value = ''
  try {
    await addCapability(expandedStaff.value.id, {
      capabilityCode: newCap.value.trim(),
      enabled: 1,
    })
    newCap.value = ''
    caps.value = await listCapabilities(expandedStaff.value.id)
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
}

async function removeCap(c: MedirStaffCapability) {
  if (!expandedStaff.value) return
  error.value = ''
  try {
    await deleteCapability(expandedStaff.value.id, c.id)
    caps.value = await listCapabilities(expandedStaff.value.id)
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
}
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between gap-2 flex-wrap">
      <h1 class="text-2xl font-semibold">人员</h1>
      <div class="flex items-center gap-2">
        <label class="form-control">
          <span class="label-text">班组</span>
          <select v-model="teamId" class="select select-bordered select-sm">
            <option v-for="t in teams" :key="t.id" :value="t.id">{{ t.teamName }}</option>
          </select>
        </label>
        <button type="button" class="btn btn-primary btn-sm" :disabled="teamId === ''" @click="openCreate">新建</button>
      </div>
    </div>

    <div v-if="error" class="alert alert-error text-sm">{{ error }}</div>
    <div v-if="loading" class="loading loading-spinner" />

    <div v-else class="space-y-2">
      <table class="table table-zebra table-sm bg-base-100 rounded-box">
        <thead>
          <tr>
            <th>ID</th>
            <th>姓名</th>
            <th>成员类型</th>
            <th>状态</th>
            <th>排序</th>
            <th />
          </tr>
        </thead>
        <tbody>
          <template v-for="r in rows" :key="r.id">
            <tr>
              <td>{{ r.id }}</td>
              <td>{{ r.name }}</td>
              <td class="font-mono text-xs">{{ r.memberType }}</td>
              <td>{{ statusLabel(r.status) }}</td>
              <td>{{ r.sortOrder }}</td>
              <td class="text-right space-x-1">
                <button type="button" class="btn btn-ghost btn-xs" @click="toggleCaps(r)">
                  {{ expandedId === r.id ? '收起能力' : '能力' }}
                </button>
                <button type="button" class="btn btn-ghost btn-xs" @click="openEdit(r)">编辑</button>
                <button type="button" class="btn btn-ghost btn-xs text-error" @click="remove(r)">删除</button>
              </td>
            </tr>
            <tr v-if="expandedId === r.id" class="bg-base-200">
              <td colspan="6">
                <div v-if="capLoading" class="loading loading-spinner loading-sm" />
                <div v-else class="flex flex-col gap-2">
                  <div class="flex flex-wrap gap-2 items-center">
                    <span class="text-sm font-medium">能力</span>
                    <span v-for="c in caps" :key="c.id" class="badge badge-outline gap-1">
                      {{ c.capabilityCode }} ({{ c.enabled === 1 ? '启' : '停' }})
                      <button type="button" class="btn btn-ghost btn-xs px-0" @click="removeCap(c)">×</button>
                    </span>
                  </div>
                  <div class="flex gap-2 max-w-md">
                    <input v-model="newCap" class="input input-bordered input-sm flex-1" placeholder="capabilityCode" @keyup.enter="addCap" />
                    <button type="button" class="btn btn-sm btn-primary" @click="addCap">添加（enabled=1）</button>
                  </div>
                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>

    <dialog id="staff_modal" class="modal">
      <div class="modal-box max-w-lg">
        <h3 class="font-bold text-lg">{{ editing ? '编辑人员' : '新建人员' }}</h3>
        <label class="form-control w-full">
          <span class="label-text">name</span>
          <input v-model="form.name" class="input input-bordered w-full" />
        </label>
        <label class="form-control w-full">
          <span class="label-text">memberType</span>
          <input v-model="form.memberType" class="input input-bordered w-full font-mono" placeholder="如 FIXED / 机动" />
        </label>
        <label class="form-control w-full">
          <span class="label-text">sortOrder</span>
          <input v-model.number="form.sortOrder" type="number" class="input input-bordered w-full" />
        </label>
        <label class="form-control w-full">
          <span class="label-text">status</span>
          <select v-model.number="form.status" class="select select-bordered w-full">
            <option :value="1">1 在职</option>
            <option :value="2">2 停用</option>
          </select>
        </label>
        <label class="form-control w-full">
          <span class="label-text">employeeNo</span>
          <input v-model="form.employeeNo" class="input input-bordered w-full" />
        </label>
        <label class="form-control w-full">
          <span class="label-text">phone</span>
          <input v-model="form.phone" class="input input-bordered w-full" />
        </label>
        <label class="form-control w-full">
          <span class="label-text">email</span>
          <input v-model="form.email" class="input input-bordered w-full" />
        </label>
        <label class="form-control w-full">
          <span class="label-text">fixedPostId（可空）</span>
          <input v-model="form.fixedPostId" type="text" class="input input-bordered w-full font-mono" placeholder="数字或留空" />
        </label>
        <label class="form-control w-full">
          <span class="label-text">remark</span>
          <input v-model="form.remark" class="input input-bordered w-full" />
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
