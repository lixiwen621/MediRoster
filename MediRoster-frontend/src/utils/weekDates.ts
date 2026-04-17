import { addDays, format, getISOWeek, getISOWeekYear, parseISO, startOfISOWeek } from 'date-fns'

/** 由「周起始日（周一）」推算当周 7 天的 `yyyy-MM-dd`，与 RosterWeekResponse.weekStartDate 一致 */
export function weekDaysFromWeekStart(weekStartDate: string): string[] {
  const base = parseISO(weekStartDate)
  return Array.from({ length: 7 }, (_, i) => format(addDays(base, i), 'yyyy-MM-dd'))
}

/** 兼容：按 ISO 周年与周序号推算（若界面仍需要） */
export function isoWeekDayStrings(year: number, weekNumber: number): string[] {
  const jan4 = new Date(year, 0, 4)
  const week1Monday = startOfISOWeek(jan4)
  const weekStart = addDays(week1Monday, (weekNumber - 1) * 7)
  return Array.from({ length: 7 }, (_, i) => format(addDays(weekStart, i), 'yyyy-MM-dd'))
}

export function isoWeekFromDate(isoDate: string): { year: number; week: number } {
  const d = parseISO(isoDate)
  return { year: getISOWeekYear(d), week: getISOWeek(d) }
}
