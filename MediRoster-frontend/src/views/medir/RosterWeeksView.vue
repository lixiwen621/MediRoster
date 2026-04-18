<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import axios from 'axios'
import { addDays, format, parseISO, startOfWeek } from 'date-fns'
import {
  createRosterWeek,
  deleteRosterWeek,
  exportRosterWeekExcel,
  generateRosterWeek,
  getRosterWeek,
  getRosterWeekCells,
  getRosterWeekWeekendStats,
  listRosterWeeks,
  listShiftTypes,
  listStaff,
  listTeams,
  putRosterWeekCells,
  putRosterWeekWeekendStats,
  updateRosterWeek,
} from '@/api/medir'
import type {
  MedirRosterCell,
  MedirRosterWeek,
  MedirShiftType,
  MedirStaff,
  MedirTeam,
  RosterCellItem,
  RosterWeekGenerateResponse,
  RosterWeekendStatUpsertItem,
} from '@/api/medir/types'
import { getAxiosErrorMessage } from '@/api/client'
import { getDownloadFilename } from '@/utils/downloadFilename'
import { weekDaysFromWeekStart } from '@/utils/weekDates'
import {
  lastWeekendZhongPairCount,
  weekendBothWorkButNotDoubleZhong,
  weekendFullDayCount,
} from '@/utils/rosterStats'

interface WeekendStatRowVm {
  staffId: number
  weekendFullAuto: number
  weekendFullFinal: number
  lastWeekendAuto: number
  lastWeekendFinal: number
  isOverridden: number
  overrideReason: string | null
  weekendFullOverrideBase: number | null
  lastWeekendOverrideBase: number | null
  overrideReasonBase: string | null
  weekendFullOverrideEdit: number | null
  lastWeekendOverrideEdit: number | null
  overrideReasonEdit: string
  weekendStatDirty: boolean
}

const teams = ref<MedirTeam[]>([])
const teamId = ref<number | ''>('')
const year = ref(new Date().getFullYear())
const weeks = ref<MedirRosterWeek[]>([])
const selected = ref<MedirRosterWeek | null>(null)

const shiftTypes = ref<MedirShiftType[]>([])
const staffList = ref<MedirStaff[]>([])
const matrix = ref<Record<string, number | ''>>({})
const weekendStatMap = ref<Record<number, WeekendStatRowVm>>({})
/** 相对上次从服务端加载/保存后的单元格是否有未保存修改 */
const cellsDirty = ref(false)

const loading = ref(false)
const saving = ref(false)
const clearing = ref(false)
const weekendSaving = ref(false)
const generating = ref(false)
const exporting = ref(false)
const error = ref('')
const loadWeekError = ref('')
const generateNotice = ref('')
const exportNotice = ref('')
const clearNotice = ref('')
const dryRunPreview = ref<RosterWeekGenerateResponse | null>(null)
const exportFilename = ref('')

const newWeekStart = ref(format(startOfWeek(new Date(), { weekStartsOn: 1 }), 'yyyy-MM-dd'))
const newYearLabel = ref(new Date().getFullYear())
const newWeekStatus = ref<1 | 2>(1)

const weekdayLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const shiftById = computed(() => {
  const m = new Map<number, MedirShiftType>()
  for (const s of shiftTypes.value) m.set(s.id, s)
  return m
})

const weekDates = computed(() => {
  if (!selected.value?.weekStartDate) return [] as string[]
  return weekDaysFromWeekStart(selected.value.weekStartDate)
})

const satSun = computed(() => {
  const d = weekDates.value
  if (d.length < 7) return { sat: undefined as string | undefined, sun: undefined as string | undefined }
  return { sat: d[5], sun: d[6] }
})

const sortedStaff = computed(() =>
  [...staffList.value].sort((a, b) => (a.sortOrder ?? a.id) - (b.sortOrder ?? b.id)),
)

const weekendDirtyCount = computed(
  () => Object.values(weekendStatMap.value).filter((r) => r.weekendStatDirty).length,
)

function cellKey(staffId: number, date: string) {
  return `${staffId}:${date}`
}

function getCell(staffId: number, date: string): number | '' {
  return matrix.value[cellKey(staffId, date)] ?? ''
}

function setCell(staffId: number, date: string, v: number | '') {
  matrix.value[cellKey(staffId, date)] = v
  cellsDirty.value = true
}

function localWeekendCalc(staffId: number) {
  const { sat, sun } = satSun.value
  if (!sat || !sun) return { full: 0, lastWe: 0 as 0 | 1, warn: false }
  const sid = getCell(staffId, sat)
  const uid = getCell(staffId, sun)
  const satN = sid === '' ? undefined : sid
  const sunN = uid === '' ? undefined : uid
  const map = shiftById.value
  return {
    full: weekendFullDayCount(satN, sunN, map),
    lastWe: lastWeekendZhongPairCount(satN, sunN, map),
    warn: weekendBothWorkButNotDoubleZhong(satN, sunN, map),
  }
}

function normalizeReason(v: string | null | undefined): string | null {
  if (!v) return null
  const t = v.trim()
  return t.length > 0 ? t : null
}

function createDefaultWeekendRow(staffId: number): WeekendStatRowVm {
  const local = localWeekendCalc(staffId)
  return {
    staffId,
    weekendFullAuto: local.full,
    weekendFullFinal: local.full,
    lastWeekendAuto: local.lastWe,
    lastWeekendFinal: local.lastWe,
    isOverridden: 0,
    overrideReason: null,
    weekendFullOverrideBase: null,
    lastWeekendOverrideBase: null,
    overrideReasonBase: null,
    weekendFullOverrideEdit: null,
    lastWeekendOverrideEdit: null,
    overrideReasonEdit: '',
    weekendStatDirty: false,
  }
}

function markWeekendDirty(row: WeekendStatRowVm) {
  const reasonEdit = normalizeReason(row.overrideReasonEdit)
  const reasonBase = normalizeReason(row.overrideReasonBase)
  row.weekendStatDirty =
    row.weekendFullOverrideEdit !== row.weekendFullOverrideBase ||
    row.lastWeekendOverrideEdit !== row.lastWeekendOverrideBase ||
    reasonEdit !== reasonBase
}

function weekendRowOf(staffId: number): WeekendStatRowVm {
  return weekendStatMap.value[staffId] ?? createDefaultWeekendRow(staffId)
}

function weekendFullDisplay(row: WeekendStatRowVm): number {
  return row.weekendFullOverrideEdit ?? row.weekendFullAuto
}

function lastWeekendDisplay(row: WeekendStatRowVm): number {
  return row.lastWeekendOverrideEdit ?? row.lastWeekendAuto
}

function onWeekendInput(
  row: WeekendStatRowVm,
  field: 'weekendFullOverrideEdit' | 'lastWeekendOverrideEdit',
  raw: string,
) {
  const v = Number(raw)
  if (!Number.isInteger(v) || v < 0) {
    error.value = '周末全天/上周末 需为非负整数。'
    return
  }
  row[field] = v
  markWeekendDirty(row)
}

function onReasonInput(row: WeekendStatRowVm, raw: string) {
  row.overrideReasonEdit = raw
  if (raw.length > 512) {
    error.value = '覆盖原因长度不能超过 512。'
  }
  markWeekendDirty(row)
}

function restoreWeekendAuto(row: WeekendStatRowVm) {
  row.weekendFullOverrideEdit = null
  row.lastWeekendOverrideEdit = null
  row.overrideReasonEdit = ''
  markWeekendDirty(row)
}

function rowWarn(staffId: number): boolean {
  return localWeekendCalc(staffId).warn
}

function isRowOverriddenNow(row: WeekendStatRowVm): boolean {
  return row.weekendFullOverrideEdit !== null || row.lastWeekendOverrideEdit !== null
}

function rowHighlightClass(row: WeekendStatRowVm): string {
  if (row.weekendStatDirty) return 'bg-warning/10'
  if (isRowOverriddenNow(row)) return 'bg-info/10'
  return ''
}

function weekendHoverText(row: WeekendStatRowVm): string {
  const reason = normalizeReason(row.overrideReasonEdit) ?? '无'
  return `周末全天: Auto ${row.weekendFullAuto} -> Final ${weekendFullDisplay(row)} | 上周末: Auto ${row.lastWeekendAuto} -> Final ${lastWeekendDisplay(row)} | 原因: ${reason}`
}

function applyWeekendStats(rawRows: Array<{
  staffId: number
  weekendFullAuto: number
  weekendFullFinal: number
  lastWeekendAuto: number
  lastWeekendFinal: number
  isOverridden: number
  overrideReason: string | null
  weekendFullOverride?: number | null
  lastWeekendOverride?: number | null
}>) {
  const byStaff = new Map<number, (typeof rawRows)[number]>()
  for (const r of rawRows) byStaff.set(r.staffId, r)

  const next: Record<number, WeekendStatRowVm> = {}
  for (const s of sortedStaff.value) {
    const raw = byStaff.get(s.id)
    if (!raw) {
      next[s.id] = createDefaultWeekendRow(s.id)
      continue
    }
    const fullBase =
      raw.weekendFullOverride !== undefined
        ? raw.weekendFullOverride
        : raw.isOverridden === 1
          ? raw.weekendFullFinal
          : null
    const lastBase =
      raw.lastWeekendOverride !== undefined
        ? raw.lastWeekendOverride
        : raw.isOverridden === 1
          ? raw.lastWeekendFinal
          : null

    next[s.id] = {
      staffId: s.id,
      weekendFullAuto: raw.weekendFullAuto,
      weekendFullFinal: raw.weekendFullFinal,
      lastWeekendAuto: raw.lastWeekendAuto,
      lastWeekendFinal: raw.lastWeekendFinal,
      isOverridden: raw.isOverridden,
      overrideReason: raw.overrideReason,
      weekendFullOverrideBase: fullBase,
      lastWeekendOverrideBase: lastBase,
      overrideReasonBase: normalizeReason(raw.overrideReason),
      weekendFullOverrideEdit: fullBase,
      lastWeekendOverrideEdit: lastBase,
      overrideReasonEdit: raw.overrideReason ?? '',
      weekendStatDirty: false,
    }
  }
  weekendStatMap.value = next
}

async function loadTeams() {
  teams.value = await listTeams()
  if (teams.value.length && teamId.value === '') teamId.value = teams.value[0].id
}

async function loadWeeks() {
  if (teamId.value === '') return
  loading.value = true
  error.value = ''
  try {
    weeks.value = await listRosterWeeks({ teamId: Number(teamId.value), year: year.value })
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  } finally {
    loading.value = false
  }
}

async function loadShiftTypes() {
  shiftTypes.value = await listShiftTypes()
}

async function loadStaff() {
  if (teamId.value === '') return
  staffList.value = await listStaff({ teamId: Number(teamId.value) })
}

async function loadWeekendStats(weekId: number) {
  try {
    const stats = await getRosterWeekWeekendStats(weekId)
    applyWeekendStats(stats)
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
}

async function selectWeek(w: MedirRosterWeek) {
  selected.value = w
  loadWeekError.value = ''
  matrix.value = {}
  weekendStatMap.value = {}
  dryRunPreview.value = null
  generateNotice.value = ''
  try {
    const [full, cells, weekendStats] = await Promise.all([
      getRosterWeek(w.id),
      getRosterWeekCells(w.id),
      getRosterWeekWeekendStats(w.id),
    ])
    selected.value = full
    applyCells(cells)
    applyWeekendStats(weekendStats)
  } catch (e) {
    loadWeekError.value = getAxiosErrorMessage(e)
  }
}

function applyCells(cells: MedirRosterCell[]) {
  const next: Record<string, number | ''> = {}
  for (const c of cells) {
    next[cellKey(c.staffId, c.workDate)] = c.shiftTypeId
  }
  matrix.value = next
  cellsDirty.value = false
}

function clearSelection() {
  selected.value = null
  matrix.value = {}
  weekendStatMap.value = {}
  dryRunPreview.value = null
  generateNotice.value = ''
  exportNotice.value = ''
  clearNotice.value = ''
  exportFilename.value = ''
  cellsDirty.value = false
}

onMounted(async () => {
  try {
    await loadTeams()
    await loadShiftTypes()
    await loadStaff()
    await loadWeeks()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
})

watch(teamId, async () => {
  clearSelection()
  await loadStaff()
  await loadWeeks()
})

watch(year, () => loadWeeks())

async function createWeek() {
  if (teamId.value === '') return
  error.value = ''
  try {
    await createRosterWeek({
      teamId: Number(teamId.value),
      weekStartDate: newWeekStart.value,
      yearLabel: newYearLabel.value,
      status: newWeekStatus.value,
      remark: null,
    })
    await loadWeeks()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
}

async function removeWeek(w: MedirRosterWeek) {
  if (!confirm(`删除排班周（周一起 ${w.weekStartDate}）？`)) return
  error.value = ''
  try {
    await deleteRosterWeek(w.id)
    if (selected.value?.id === w.id) clearSelection()
    await loadWeeks()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
}

async function publishToggle() {
  if (!selected.value) return
  error.value = ''
  try {
    const nextStatus = selected.value.status === 1 ? 2 : 1
    selected.value = await updateRosterWeek(selected.value.id, {
      yearLabel: selected.value.yearLabel,
      status: nextStatus,
      remark: selected.value.remark || null,
      version: selected.value.version,
    })
    await loadWeeks()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  }
}

function buildCellsPayload(): RosterCellItem[] {
  if (!selected.value) return []
  const dates = weekDates.value
  const out: RosterCellItem[] = []
  for (const s of sortedStaff.value) {
    for (const d of dates) {
      const v = getCell(s.id, d)
      if (v === '') continue
      out.push({
        staffId: s.id,
        workDate: d,
        shiftTypeId: v,
        validationExempt: 0,
      })
    }
  }
  return out
}

async function reloadWeekMatrix() {
  if (!selected.value) return
  selected.value = await getRosterWeek(selected.value.id)
  const cells = await getRosterWeekCells(selected.value.id)
  applyCells(cells)
  await loadWeekendStats(selected.value.id)
}

async function saveCells(): Promise<boolean> {
  if (!selected.value) return false
  saving.value = true
  error.value = ''
  try {
    const weekendStats = await putRosterWeekCells(selected.value.id, {
      cells: buildCellsPayload(),
    })
    selected.value = await getRosterWeek(selected.value.id)
    const cells = await getRosterWeekCells(selected.value.id)
    applyCells(cells)
    applyWeekendStats(weekendStats)
    return true
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
    return false
  } finally {
    saving.value = false
  }
}

async function clearWeekCells() {
  if (!selected.value) return
  if (!confirm('确认清空本周所有排班单元格？此操作会删除当前周已保存的全部班次。')) return
  if (!confirm('再次确认：清空后不会自动补全，需要你手动点击“执行补全落库”。确定继续？')) return
  clearing.value = true
  error.value = ''
  clearNotice.value = ''
  generateNotice.value = ''
  try {
    const weekendStats = await putRosterWeekCells(selected.value.id, { cells: [] })
    selected.value = await getRosterWeek(selected.value.id)
    const cells = await getRosterWeekCells(selected.value.id)
    applyCells(cells)
    applyWeekendStats(weekendStats)
    clearNotice.value = '已清空本周排班'
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  } finally {
    clearing.value = false
  }
}

function ensureCellsSavedForGenerate(): boolean {
  if (cellsDirty.value) {
    error.value = '请先点击「保存单元格」保存已确认班次，或使用「保存并试算补全」。生成仅基于服务端已保存数据。'
    return false
  }
  return true
}

function formatGenerateResult(r: RosterWeekGenerateResponse): string {
  const dry = r.dryRun === 1 ? '试算' : '落库'
  return `${r.message} [${dry}] 新增单元格 ${r.generatedCellCount}，覆盖 ${r.overwrittenCellCount}，跳过已确认 ${r.skippedConfirmedCount}`
}

async function dryRunFillUnconfirmed() {
  if (!selected.value) return
  if (!ensureCellsSavedForGenerate()) return
  generating.value = true
  error.value = ''
  generateNotice.value = ''
  try {
    const r = await generateRosterWeek(selected.value.id, {
      strategy: 'FILL_UNCONFIRMED',
      dryRun: 1,
      reason: null,
    })
    dryRunPreview.value = r
    generateNotice.value = formatGenerateResult(r)
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  } finally {
    generating.value = false
  }
}

async function commitFillUnconfirmed() {
  if (!selected.value) return
  if (!ensureCellsSavedForGenerate()) return
  if (!dryRunPreview.value) {
    if (!confirm('尚未试算，确定直接按「补全未确认」策略写入落库？')) return
  }
  generating.value = true
  error.value = ''
  try {
    const r = await generateRosterWeek(selected.value.id, {
      strategy: 'FILL_UNCONFIRMED',
      respectManualConfirmed: 1,
      dryRun: 0,
      reason: 'manual trigger',
    })
    dryRunPreview.value = null
    generateNotice.value = formatGenerateResult(r)
    await reloadWeekMatrix()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  } finally {
    generating.value = false
  }
}

async function saveCellsAndDryRun() {
  const ok = await saveCells()
  if (!ok) return
  await dryRunFillUnconfirmed()
}

async function overwriteAllGenerate() {
  if (!selected.value) return
  if (
    !confirm(
      '危险操作：将使用 OVERWRITE_ALL 全量重新生成本周排班；respectManualConfirmed=0 时不再保留手工已保存单元格。确定继续？',
    )
  ) {
    return
  }
  if (!confirm('再次确认：该操作会覆盖本周全部单元格，且不可撤销。是否继续？')) return
  if (!ensureCellsSavedForGenerate()) return
  generating.value = true
  error.value = ''
  try {
    const r = await generateRosterWeek(selected.value.id, {
      strategy: 'OVERWRITE_ALL',
      respectManualConfirmed: 0,
      dryRun: 0,
      reason: '全量重新生成',
    })
    dryRunPreview.value = null
    generateNotice.value = formatGenerateResult(r)
    await reloadWeekMatrix()
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  } finally {
    generating.value = false
  }
}

async function parseBlobApiErrorMessage(blob: Blob): Promise<string | null> {
  try {
    const text = await blob.text()
    const parsed = JSON.parse(text) as { message?: string }
    if (typeof parsed.message === 'string' && parsed.message.length > 0) {
      return parsed.message
    }
    return null
  } catch {
    return null
  }
}

async function exportExcel() {
  if (!selected.value) return
  if (cellsDirty.value || weekendDirtyCount.value > 0) {
    error.value = '当前有未保存改动，请先保存单元格/周末统计后再导出。'
    return
  }
  exporting.value = true
  error.value = ''
  exportNotice.value = ''
  try {
    const params = exportFilename.value.trim() ? { filename: exportFilename.value.trim() } : undefined
    const resp = await exportRosterWeekExcel(selected.value.id, params)
    const contentType = String(resp.headers['content-type'] ?? '')
    const blob = new Blob(
      [resp.data],
      {
        type:
          contentType || 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      },
    )

    if (contentType.includes('application/json')) {
      const msg = (await parseBlobApiErrorMessage(blob)) ?? '导出失败'
      throw new Error(msg)
    }

    const cd =
      (resp.headers['content-disposition'] as string | undefined) ??
      (resp.headers['Content-Disposition'] as string | undefined)
    const downloadName = getDownloadFilename(cd)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = downloadName
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
    exportNotice.value = '导出成功'
  } catch (e) {
    if (axios.isAxiosError(e) && e.response?.data instanceof Blob) {
      const msg = await parseBlobApiErrorMessage(e.response.data)
      error.value = msg ?? getAxiosErrorMessage(e)
    } else {
      error.value = getAxiosErrorMessage(e)
    }
  } finally {
    exporting.value = false
  }
}

function buildWeekendStatsPayload(): RosterWeekendStatUpsertItem[] {
  const rows = Object.values(weekendStatMap.value).filter((r) => r.weekendStatDirty)
  const items: RosterWeekendStatUpsertItem[] = []
  for (const r of rows) {
    if (r.weekendFullOverrideEdit !== null && (!Number.isInteger(r.weekendFullOverrideEdit) || r.weekendFullOverrideEdit < 0)) {
      throw new Error(`人员 ${r.staffId} 周末全天需为非负整数`)
    }
    if (r.lastWeekendOverrideEdit !== null && (!Number.isInteger(r.lastWeekendOverrideEdit) || r.lastWeekendOverrideEdit < 0)) {
      throw new Error(`人员 ${r.staffId} 上周末需为非负整数`)
    }
    const reason = normalizeReason(r.overrideReasonEdit)
    if (reason && reason.length > 512) {
      throw new Error(`人员 ${r.staffId} 覆盖原因长度超过 512`)
    }
    items.push({
      staffId: r.staffId,
      weekendFullOverride: r.weekendFullOverrideEdit,
      lastWeekendOverride: r.lastWeekendOverrideEdit,
      overrideReason:
        r.weekendFullOverrideEdit === null && r.lastWeekendOverrideEdit === null ? null : reason,
    })
  }
  return items
}

async function saveWeekendStats() {
  if (!selected.value) return
  if (weekendDirtyCount.value === 0) return
  weekendSaving.value = true
  error.value = ''
  try {
    const items = buildWeekendStatsPayload()
    await putRosterWeekWeekendStats(selected.value.id, { items })
    await loadWeekendStats(selected.value.id)
  } catch (e) {
    error.value = getAxiosErrorMessage(e)
  } finally {
    weekendSaving.value = false
  }
}

function dayHeader(dateStr: string) {
  const parts = dateStr.split('-').map(Number)
  if (parts.length < 3) return dateStr
  const [, m, d] = parts
  return `${m}.${d}`
}

function weekRangeText(weekStartDate: string): string {
  const start = parseISO(weekStartDate)
  const end = addDays(start, 6)
  const sameYear = start.getFullYear() === end.getFullYear()
  if (sameYear) {
    return `${format(start, 'yyyy-MM-dd')} - ${format(end, 'MM-dd')}`
  }
  return `${format(start, 'yyyy-MM-dd')} - ${format(end, 'yyyy-MM-dd')}`
}
</script>

<template>
  <div class="space-y-4">
    <h1 class="text-2xl font-semibold">排班周</h1>
    <p class="text-sm opacity-70 max-w-3xl">
      先保存已确认班次（
      <code class="text-xs">PUT .../cells</code>
      ），再使用
      <code class="text-xs">POST .../generate</code>
      半自动补全空单元格；周末统计仍用
      <code class="text-xs">GET/PUT .../weekend-stats</code>
      。
    </p>

    <div class="flex flex-wrap gap-3 items-end">
      <label class="form-control">
        <span class="label-text">班组</span>
        <select v-model="teamId" class="select select-bordered select-sm">
          <option v-for="t in teams" :key="t.id" :value="t.id">{{ t.teamName }}</option>
        </select>
      </label>
      <label class="form-control">
        <span class="label-text">列表筛选 year</span>
        <input v-model.number="year" type="number" class="input input-bordered input-sm w-28" />
      </label>
      <button type="button" class="btn btn-sm" @click="loadWeeks">刷新列表</button>
      <div class="flex flex-wrap items-center gap-2 border-l pl-3">
        <span class="text-sm">新建周</span>
        <input v-model="newWeekStart" type="date" class="input input-bordered input-sm" title="weekStartDate 周一" />
        <input v-model.number="newYearLabel" type="number" class="input input-bordered input-sm w-24" title="yearLabel" />
        <select v-model.number="newWeekStatus" class="select select-bordered select-sm">
          <option :value="1">草稿</option>
          <option :value="2">已发布</option>
        </select>
        <button type="button" class="btn btn-sm btn-primary" :disabled="teamId === ''" @click="createWeek">创建</button>
      </div>
    </div>

    <div v-if="error" class="alert alert-error text-sm">{{ error }}</div>
    <div v-if="loading" class="loading loading-spinner" />

    <div v-else class="grid grid-cols-1 lg:grid-cols-4 gap-4">
      <div class="card bg-base-100 shadow lg:col-span-1">
        <div class="card-body p-4">
          <h2 class="card-title text-base">周列表</h2>
          <ul class="menu menu-sm bg-base-200 rounded-box max-h-96 overflow-auto">
            <li v-for="w in weeks" :key="w.id">
              <a
                href="#"
                :class="{ active: selected?.id === w.id }"
                @click.prevent="selectWeek(w)"
              >
                周一起 {{ weekRangeText(w.weekStartDate) }} · v{{ w.version }} ·
                {{ w.status === 2 ? '已发布' : '草稿' }}
              </a>
            </li>
          </ul>
        </div>
      </div>

      <div class="lg:col-span-3 space-y-3">
        <div v-if="!selected" class="alert alert-info">请选择左侧一周，或新建排班周。</div>

        <template v-else>
          <div v-if="loadWeekError" class="alert alert-warning text-sm">加载单元格失败：{{ loadWeekError }}</div>
          <div v-if="clearNotice" role="status" class="alert alert-success text-sm">{{ clearNotice }}</div>
          <div v-if="generateNotice" role="status" class="alert alert-success text-sm">{{ generateNotice }}</div>
          <div v-if="exportNotice" role="status" class="alert alert-success text-sm">{{ exportNotice }}</div>

          <div class="flex flex-wrap items-center gap-2">
            <span class="font-medium">
              周一起 {{ weekRangeText(selected.weekStartDate) }} · version {{ selected.version }}
            </span>
            <span v-if="cellsDirty" class="badge badge-warning badge-sm">单元格已改未保存</span>
            <button type="button" class="btn btn-sm btn-outline" @click="publishToggle">
              {{ selected.status === 2 ? '改回草稿' : '发布' }}
            </button>
            <button type="button" class="btn btn-sm btn-error btn-outline" @click="removeWeek(selected)">删除该周</button>
            <button type="button" class="btn btn-sm btn-primary" :disabled="saving" @click="saveCells">
              {{ saving ? '保存中…' : '保存单元格' }}
            </button>
            <button
              type="button"
              class="btn btn-sm btn-error"
              :disabled="clearing || saving || generating || exporting"
              @click="clearWeekCells"
            >
              {{ clearing ? '清空中…' : '清空排班' }}
            </button>
            <button
              type="button"
              class="btn btn-sm btn-secondary"
              :disabled="weekendSaving || weekendDirtyCount === 0 || clearing"
              @click="saveWeekendStats"
            >
              {{ weekendSaving ? '保存中…' : `保存周末统计(${weekendDirtyCount})` }}
            </button>
            <button
              type="button"
              class="btn btn-sm btn-outline"
              :disabled="generating || saving || clearing"
              title="dryRun=1，不落库"
              @click="dryRunFillUnconfirmed"
            >
              {{ generating ? '处理中…' : '试算补全' }}
            </button>
            <button
              type="button"
              class="btn btn-sm btn-accent"
              :disabled="generating || saving || clearing"
              title="dryRun=0，补全未确认格子"
              @click="commitFillUnconfirmed"
            >
              {{ generating ? '处理中…' : '执行补全落库' }}
            </button>
            <button
              type="button"
              class="btn btn-sm btn-outline"
              :disabled="generating || saving || clearing"
              @click="saveCellsAndDryRun"
            >
              保存并试算补全
            </button>
            <button
              type="button"
              class="btn btn-sm btn-error"
              :disabled="generating || saving || clearing"
              @click="overwriteAllGenerate"
            >
              全量重新生成
            </button>
            <label class="input input-bordered input-sm flex items-center gap-2 w-48">
              <span class="text-xs opacity-70">文件名</span>
              <input
                v-model="exportFilename"
                type="text"
                class="grow text-xs"
                placeholder="可选，不含扩展名也可"
              />
            </label>
            <button
              type="button"
              class="btn btn-sm btn-info"
              :disabled="exporting || saving || generating || clearing"
              @click="exportExcel"
            >
              {{ exporting ? '导出中…' : '导出Excel' }}
            </button>
          </div>

          <div class="overflow-x-auto rounded-box border border-base-300 bg-base-100">
            <table class="table table-xs">
              <thead>
                <tr>
                  <th class="sticky left-0 z-10 bg-base-100">姓名</th>
                  <th v-for="(d, i) in weekDates" :key="d">
                    <div class="flex flex-col items-center min-w-[5.5rem]">
                      <span>{{ weekdayLabels[i] }}</span>
                      <span class="opacity-60 text-[10px]">{{ dayHeader(d) }}</span>
                    </div>
                  </th>
                  <th>周末全天（可改）</th>
                  <th>上周末（可改）</th>
                  <th>覆盖原因</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="s in sortedStaff" :key="s.id" :class="rowHighlightClass(weekendRowOf(s.id))">
                  <td
                    class="sticky left-0 z-10 font-medium"
                    :class="isRowOverriddenNow(weekendRowOf(s.id)) ? 'bg-info/20' : 'bg-base-100'"
                  >
                    {{ s.name }}
                    <span v-if="rowWarn(s.id)" class="badge badge-warning badge-xs ml-1" title="REQ-STAT-06"
                      >周末双上班非双中</span
                    >
                  </td>
                  <td v-for="d in weekDates" :key="d">
                    <select
                      class="select select-bordered select-xs w-full max-w-[6rem]"
                      :value="getCell(s.id, d)"
                      @change="
                        setCell(
                          s.id,
                          d,
                          ($event.target as HTMLSelectElement).value === ''
                            ? ''
                            : Number(($event.target as HTMLSelectElement).value),
                        )
                      "
                    >
                      <option value="">—</option>
                      <option v-for="st in shiftTypes" :key="st.id" :value="st.id">{{ st.nameZh }}</option>
                    </select>
                  </td>
                  <td class="min-w-28" :title="weekendHoverText(weekendRowOf(s.id))">
                    <input
                      type="number"
                      min="0"
                      step="1"
                      class="input input-bordered input-xs w-20"
                      :value="weekendFullDisplay(weekendRowOf(s.id))"
                      @change="
                        onWeekendInput(
                          weekendRowOf(s.id),
                          'weekendFullOverrideEdit',
                          ($event.target as HTMLInputElement).value,
                        )
                      "
                    />
                    <div class="text-[10px] opacity-60">
                      Auto {{ weekendRowOf(s.id).weekendFullAuto }} -> Final {{ weekendFullDisplay(weekendRowOf(s.id)) }}
                    </div>
                  </td>
                  <td class="min-w-28" :title="weekendHoverText(weekendRowOf(s.id))">
                    <input
                      type="number"
                      min="0"
                      step="1"
                      class="input input-bordered input-xs w-20"
                      :value="lastWeekendDisplay(weekendRowOf(s.id))"
                      @change="
                        onWeekendInput(
                          weekendRowOf(s.id),
                          'lastWeekendOverrideEdit',
                          ($event.target as HTMLInputElement).value,
                        )
                      "
                    />
                    <div class="text-[10px] opacity-60">
                      Auto {{ weekendRowOf(s.id).lastWeekendAuto }} -> Final {{ lastWeekendDisplay(weekendRowOf(s.id)) }}
                    </div>
                  </td>
                  <td class="min-w-48" :title="weekendHoverText(weekendRowOf(s.id))">
                    <input
                      type="text"
                      maxlength="512"
                      class="input input-bordered input-xs w-full"
                      :value="weekendRowOf(s.id).overrideReasonEdit"
                      placeholder="可选：覆盖原因"
                      @input="onReasonInput(weekendRowOf(s.id), ($event.target as HTMLInputElement).value)"
                    />
                  </td>
                  <td class="min-w-32">
                    <div class="flex items-center gap-1">
                      <button
                        type="button"
                        class="btn btn-ghost btn-xs"
                        @click="restoreWeekendAuto(weekendRowOf(s.id))"
                      >
                        恢复自动
                      </button>
                      <span v-if="weekendRowOf(s.id).weekendStatDirty" class="badge badge-warning badge-xs">已修改</span>
                      <span
                        v-else-if="
                          weekendRowOf(s.id).weekendFullOverrideBase !== null ||
                          weekendRowOf(s.id).lastWeekendOverrideBase !== null
                        "
                        class="badge badge-info badge-xs"
                        :title="weekendHoverText(weekendRowOf(s.id))"
                      >
                        已覆盖
                      </span>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <p class="text-xs opacity-60">今日：{{ format(new Date(), 'yyyy-MM-dd HH:mm') }} · 周六/周日为列索引 5、6</p>
        </template>
      </div>
    </div>
  </div>
</template>
