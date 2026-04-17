<script setup lang="ts">
import { ref } from 'vue'
import { format } from 'date-fns'
import { createCalendarDay, deleteCalendarDay, listCalendarDays, updateCalendarDay } from '@/api/medir'
import type { MedirCalendarDay } from '@/api/medir/types'
import { getAxiosErrorMessage } from '@/api/client'

const from = ref(format(new Date(), 'yyyy-MM-dd'))
const to = ref(format(new Date(), 'yyyy-MM-dd'))
const rows = ref<MedirCalendarDay[]>([])
const loading = ref(false)
const error = ref('')
const editing = ref<MedirCalendarDay | null>(null)
const form = ref({ calDate: '', dayType: 'WORKDAY', holidayName: '' })

async function load() {
  loading.value = true
  error.value = ''
  try {
    rows.value = await listCalendarDays({ from: from.value, to: to.value })
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = null
  form.value = { calDate: from.value, dayType: 'WORKDAY', holidayName: '' }
  ;(document.getElementById('cal_modal') as HTMLDialogElement)?.showModal()
}

function openEdit(row: MedirCalendarDay) {
  editing.value = row
  form.value = {
    calDate: row.calDate,
    dayType: row.dayType,
    holidayName: row.holidayName ?? '',
  }
  ;(document.getElementById('cal_modal') as HTMLDialogElement)?.showModal()
}

async function save() {
  error.value = ''
  try {
    const body = {
      calDate: form.value.calDate,
      dayType: form.value.dayType,
      holidayName: form.value.holidayName.trim() || null,
    }
    if (editing.value) {
      await updateCalendarDay(editing.value.id, body)
    } else {
      await createCalendarDay(body)
    }
    ;(document.getElementById('cal_modal') as HTMLDialogElement)?.close()
    await load()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
}

async function remove(row: MedirCalendarDay) {
  if (!confirm(`删除日历日 ${row.calDate}？`)) return
  error.value = ''
  try {
    await deleteCalendarDay(row.id)
    await load()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
}
</script>

<template>
  <div class="space-y-4">
    <h1 class="text-2xl font-semibold">日历日</h1>
    <p class="text-sm opacity-70">字段：calDate、dayType（业务编码）、holidayName。</p>
    <div class="flex flex-wrap items-end gap-2">
      <label class="form-control">
        <span class="label-text">from</span>
        <input v-model="from" type="date" class="input input-bordered input-sm" />
      </label>
      <label class="form-control">
        <span class="label-text">to</span>
        <input v-model="to" type="date" class="input input-bordered input-sm" />
      </label>
      <button type="button" class="btn btn-sm btn-primary" @click="load">查询</button>
      <button type="button" class="btn btn-sm" @click="openCreate">新建</button>
    </div>

    <div v-if="error" class="alert alert-error text-sm">{{ error }}</div>
    <div v-if="loading" class="loading loading-spinner" />

    <div v-else class="overflow-x-auto">
      <table class="table table-zebra table-sm bg-base-100 rounded-box text-xs">
        <thead>
          <tr>
            <th>ID</th>
            <th>calDate</th>
            <th>dayType</th>
            <th>holidayName</th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in rows" :key="r.id">
            <td>{{ r.id }}</td>
            <td>{{ r.calDate }}</td>
            <td class="font-mono">{{ r.dayType }}</td>
            <td>{{ r.holidayName || '—' }}</td>
            <td class="text-right space-x-1">
              <button type="button" class="btn btn-ghost btn-xs" @click="openEdit(r)">编辑</button>
              <button type="button" class="btn btn-ghost btn-xs text-error" @click="remove(r)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <dialog id="cal_modal" class="modal">
      <div class="modal-box">
        <h3 class="font-bold text-lg">{{ editing ? '编辑日历日' : '新建日历日' }}</h3>
        <label class="form-control w-full">
          <span class="label-text">calDate</span>
          <input v-model="form.calDate" type="date" class="input input-bordered w-full" :disabled="!!editing" />
        </label>
        <label class="form-control w-full">
          <span class="label-text">dayType</span>
          <input v-model="form.dayType" class="input input-bordered w-full font-mono" placeholder="业务日类型编码" />
        </label>
        <label class="form-control w-full">
          <span class="label-text">holidayName</span>
          <input v-model="form.holidayName" class="input input-bordered w-full" />
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
