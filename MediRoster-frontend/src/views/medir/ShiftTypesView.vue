<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { getShiftType, listShiftTypes, updateShiftType } from '@/api/medir'
import type { MedirShiftType, ShiftTypeUpdateRequest } from '@/api/medir/types'
import { getAxiosErrorMessage } from '@/api/client'

const rows = ref<MedirShiftType[]>([])
const loading = ref(false)
const error = ref('')
const form = ref<MedirShiftType | null>(null)

const flagFields: Array<{ key: keyof ShiftTypeUpdateRequest; label: string }> = [
  { key: 'isRest', label: 'isRest 休息类' },
  { key: 'isDutyZhong', label: 'isDutyZhong 值中' },
  { key: 'isDutyDa', label: 'isDutyDa 值大' },
  { key: 'isQiban', label: 'isQiban 起班' },
  { key: 'isSmallNight', label: 'isSmallNight 小夜' },
  { key: 'countsDaytimeHeadcount', label: 'countsDaytimeHeadcount' },
  { key: 'countsWeekendFullDayStat', label: 'countsWeekendFullDayStat' },
  { key: 'countsAsZhongForStructure', label: 'countsAsZhongForStructure' },
  { key: 'countsAsLinForStructure', label: 'countsAsLinForStructure' },
  { key: 'nextDayMustRest', label: 'nextDayMustRest' },
  { key: 'enabled', label: 'enabled' },
]

async function load() {
  loading.value = true
  error.value = ''
  try {
    rows.value = await listShiftTypes()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  } finally {
    loading.value = false
  }
}

onMounted(load)

async function openEdit(id: number) {
  error.value = ''
  try {
    form.value = await getShiftType(id)
    await nextTick()
    ;(document.getElementById('st_modal') as HTMLDialogElement)?.showModal()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
}

function toPayload(f: MedirShiftType): ShiftTypeUpdateRequest {
  return {
    typeCode: f.typeCode,
    nameZh: f.nameZh,
    sortOrder: f.sortOrder,
    isRest: f.isRest,
    isDutyZhong: f.isDutyZhong,
    isDutyDa: f.isDutyDa,
    isQiban: f.isQiban,
    isSmallNight: f.isSmallNight,
    countsDaytimeHeadcount: f.countsDaytimeHeadcount,
    countsWeekendFullDayStat: f.countsWeekendFullDayStat,
    countsAsZhongForStructure: f.countsAsZhongForStructure,
    countsAsLinForStructure: f.countsAsLinForStructure,
    nextDayMustRest: f.nextDayMustRest,
    enabled: f.enabled,
  }
}

async function save() {
  if (!form.value?.id) return
  error.value = ''
  try {
    await updateShiftType(form.value.id, toPayload(form.value))
    ;(document.getElementById('st_modal') as HTMLDialogElement)?.close()
    await load()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
}

function getFlag(key: keyof ShiftTypeUpdateRequest): number {
  if (!form.value) return 0
  return Number((form.value as unknown as Record<string, number>)[key as string])
}

function setFlag(key: keyof ShiftTypeUpdateRequest, v: number) {
  if (!form.value) return
  ;(form.value as unknown as Record<string, number>)[key as string] = v
}
</script>

<template>
  <div class="space-y-4">
    <h1 class="text-2xl font-semibold">班次类型</h1>
    <p class="text-sm opacity-70">PUT 全量更新，标志位均为 0/1。</p>

    <div v-if="error" class="alert alert-error text-sm">{{ error }}</div>
    <div v-if="loading" class="loading loading-spinner" />

    <div v-else class="overflow-x-auto">
      <table class="table table-zebra table-sm bg-base-100 rounded-box text-xs">
        <thead>
          <tr>
            <th>ID</th>
            <th>typeCode</th>
            <th>nameZh</th>
            <th>sortOrder</th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in rows" :key="r.id">
            <td>{{ r.id }}</td>
            <td class="font-mono">{{ r.typeCode }}</td>
            <td>{{ r.nameZh }}</td>
            <td>{{ r.sortOrder }}</td>
            <td>
              <button type="button" class="btn btn-ghost btn-xs" @click="openEdit(r.id)">编辑</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <dialog v-if="form" id="st_modal" class="modal">
      <div class="modal-box max-w-3xl max-h-[90vh] overflow-y-auto">
        <h3 class="font-bold text-lg">编辑班次类型 #{{ form.id }}</h3>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-2 mt-2">
          <label class="form-control">
            <span class="label-text">typeCode</span>
            <input v-model="form.typeCode" class="input input-bordered input-sm font-mono" />
          </label>
          <label class="form-control">
            <span class="label-text">nameZh</span>
            <input v-model="form.nameZh" class="input input-bordered input-sm" />
          </label>
          <label class="form-control">
            <span class="label-text">sortOrder</span>
            <input v-model.number="form.sortOrder" type="number" class="input input-bordered input-sm" />
          </label>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-x-4 gap-y-1 mt-3">
          <label v-for="ff in flagFields" :key="String(ff.key)" class="form-control">
            <span class="label-text text-[10px] font-mono">{{ ff.label }}</span>
            <select
              class="select select-bordered select-xs"
              :value="getFlag(ff.key)"
              @change="setFlag(ff.key, Number(($event.target as HTMLSelectElement).value))"
            >
              <option :value="0">0</option>
              <option :value="1">1</option>
            </select>
          </label>
        </div>

        <label class="form-control mt-2">
          <span class="label-text">JSON</span>
          <textarea :value="JSON.stringify(form, null, 2)" class="textarea textarea-bordered font-mono text-xs h-32" readonly />
        </label>

        <div class="modal-action">
          <form method="dialog">
            <button class="btn">关闭</button>
          </form>
          <button type="button" class="btn btn-primary" @click="save">保存</button>
        </div>
      </div>
    </dialog>
  </div>
</template>
