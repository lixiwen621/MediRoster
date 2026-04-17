import type { MedirShiftType } from '@/api/medir/types'

/** 需求 §15.1：周末全天统计（与 typeCode 一致） */
const WEEKEND_FULL_CODES_DEFAULT = new Set(['LIN', 'GUISUI_QUAN'])

/** §15.2：「上周末」——中班 / 小夜等 typeCode */
const ZHONG_CODES_DEFAULT = new Set(['ZHONG', 'XIAOYE'])

function int01(v: number | undefined): boolean {
  return v === 1
}

export function isRestShift(st: MedirShiftType | undefined): boolean {
  if (!st) return false
  if (st.typeCode?.toUpperCase() === 'XIU') return true
  return int01(st.isRest)
}

/** 周末全天天数 0～2 */
export function weekendFullDayCount(
  satShiftId: number | undefined,
  sunShiftId: number | undefined,
  byId: Map<number, MedirShiftType>,
  fullDayCodes: Set<string> = WEEKEND_FULL_CODES_DEFAULT,
): number {
  let n = 0
  for (const id of [satShiftId, sunShiftId]) {
    if (id == null) continue
    const st = byId.get(id)
    if (st && fullDayCodes.has(String(st.typeCode).toUpperCase())) n += 1
  }
  return n
}

/** 按周：周六周日均非休且均为「中」口径 → 1 */
export function lastWeekendZhongPairCount(
  satShiftId: number | undefined,
  sunShiftId: number | undefined,
  byId: Map<number, MedirShiftType>,
  zhongCodes: Set<string> = ZHONG_CODES_DEFAULT,
): 0 | 1 {
  if (satShiftId == null || sunShiftId == null) return 0
  const sat = byId.get(satShiftId)
  const sun = byId.get(sunShiftId)
  if (!sat || !sun) return 0
  if (isRestShift(sat) || isRestShift(sun)) return 0
  const satOk = zhongCodes.has(String(sat.typeCode).toUpperCase())
  const sunOk = zhongCodes.has(String(sun.typeCode).toUpperCase())
  return satOk && sunOk ? 1 : 0
}

/** REQ-STAT-06 */
export function weekendBothWorkButNotDoubleZhong(
  satShiftId: number | undefined,
  sunShiftId: number | undefined,
  byId: Map<number, MedirShiftType>,
  zhongCodes: Set<string> = ZHONG_CODES_DEFAULT,
): boolean {
  if (satShiftId == null || sunShiftId == null) return false
  const sat = byId.get(satShiftId)
  const sun = byId.get(sunShiftId)
  if (!sat || !sun) return false
  if (isRestShift(sat) || isRestShift(sun)) return false
  const satOk = zhongCodes.has(String(sat.typeCode).toUpperCase())
  const sunOk = zhongCodes.has(String(sun.typeCode).toUpperCase())
  return !(satOk && sunOk)
}
