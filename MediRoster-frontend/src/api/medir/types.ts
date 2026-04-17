/**
 * 与 MediRoster REST v1 文档字段名一致（camelCase）。
 */

export interface ApiResponse<T> {
  success: boolean
  code: number
  message: string
  data: T
}

export interface HealthDto {
  status: string
}

/** TeamResponse */
export interface MedirTeam {
  id: number
  teamCode: string
  teamName: string
  description: string
  enabled: number
  createdAt?: string
  updatedAt?: string
}

/** TeamUpsertRequest（文档亦接受 code/name/remark 别名，此处发标准字段） */
export interface TeamUpsertRequest {
  teamCode: string
  teamName: string
  description?: string | null
  enabled?: number
}

/** PostResponse — 全局岗位字典 */
export interface MedirPost {
  id: number
  postCode: string
  postName: string
  description: string
  sortOrder: number
  enabled: number
  createdAt?: string
  updatedAt?: string
}

export interface PostUpsertRequest {
  postCode: string
  postName: string
  description?: string | null
  sortOrder: number
  enabled: number
}

/** ShiftTypeResponse */
export interface MedirShiftType {
  id: number
  typeCode: string
  nameZh: string
  sortOrder: number
  isRest: number
  isDutyZhong: number
  isDutyDa: number
  isQiban: number
  isSmallNight: number
  countsDaytimeHeadcount: number
  countsWeekendFullDayStat: number
  countsAsZhongForStructure: number
  countsAsLinForStructure: number
  nextDayMustRest: number
  enabled: number
  createdAt?: string
  updatedAt?: string
}

/** ShiftTypeUpdateRequest — PUT 全量 */
export type ShiftTypeUpdateRequest = Omit<MedirShiftType, 'id' | 'createdAt' | 'updatedAt'>

export type StaffStatus = 1 | 2

/** StaffResponse */
export interface MedirStaff {
  id: number
  teamId: number
  name: string
  employeeNo: string | null
  phone: string | null
  email: string | null
  memberType: string
  sortOrder: number
  status: StaffStatus
  fixedPostId: number | null
  remark: string | null
  deletedAt?: string | null
  createdAt?: string
  updatedAt?: string
}

export interface StaffUpsertRequest {
  teamId: number
  name: string
  employeeNo?: string | null
  phone?: string | null
  email?: string | null
  memberType: string
  sortOrder: number
  status: StaffStatus
  fixedPostId?: number | null
  remark?: string | null
}

/** StaffCapabilityResponse */
export interface MedirStaffCapability {
  id: number
  staffId: number
  capabilityCode: string
  enabled: number
  createdAt?: string
  updatedAt?: string
}

export interface StaffCapabilityUpsertRequest {
  capabilityCode: string
  enabled: number
}

/** ConfigResponse */
export interface MedirConfig {
  id: number
  teamId: number
  configKey: string
  configValue: string
  valueType: string
  category: string
  description: string
  sortOrder: number
  enabled: number
  createdAt?: string
  updatedAt?: string
}

export interface ConfigUpsertRequest {
  teamId: number
  configKey: string
  configValue: string
  valueType: string
  category?: string | null
  description?: string | null
  sortOrder: number
  enabled: number
}

/** RuleMetaResponse */
export interface MedirRuleMeta {
  id: number
  ruleCode: string
  category: string
  labelZh: string
  valueType: string
  defaultValue: string
  optionsJson: string | null
  helpText: string
  sortOrder: number
  enabled: number
  createdAt?: string
  updatedAt?: string
}

export type RosterWeekStatus = 1 | 2

/** RosterWeekResponse */
export interface MedirRosterWeek {
  id: number
  teamId: number
  weekStartDate: string
  yearLabel: number
  status: RosterWeekStatus
  version: number
  remark: string
  createdAt?: string
  updatedAt?: string
}

export interface RosterWeekCreateRequest {
  teamId: number
  weekStartDate: string
  yearLabel: number
  status: RosterWeekStatus
  remark?: string | null
}

export interface RosterWeekUpdateRequest {
  yearLabel: number
  status: RosterWeekStatus
  remark?: string | null
  version: number
}

/** RosterCellResponse / CellItem */
export interface MedirRosterCell {
  id?: number
  rosterWeekId?: number
  staffId: number
  workDate: string
  shiftTypeId: number
  postId?: number | null
  postLabel?: string | null
  validationExempt: number
  exemptReason?: string | null
  remark?: string | null
  createdAt?: string
  updatedAt?: string
}

export interface RosterCellItem {
  staffId: number
  workDate: string
  shiftTypeId: number
  postId?: number | null
  postLabel?: string | null
  validationExempt: number
  exemptReason?: string | null
  remark?: string | null
}

export interface RosterCellReplaceRequest {
  cells: RosterCellItem[] | null
}

/** RosterWeekStaffPostResponse */
export interface MedirStaffPostRow {
  id?: number
  rosterWeekId?: number
  staffId: number
  displayPostId: number | null
  displayLabel: string | null
  createdAt?: string
  updatedAt?: string
}

export interface RosterStaffPostReplaceRequest {
  items: Array<{
    staffId: number
    displayPostId?: number | null
    displayLabel?: string | null
  }>
}

/** Weekend stats */
export interface MedirRosterWeekendStat {
  staffId: number
  weekendFullAuto: number
  weekendFullFinal: number
  lastWeekendAuto: number
  lastWeekendFinal: number
  isOverridden: number
  overrideReason: string | null
  /** 可选：若后端同时返回覆盖值字段，优先用它 */
  weekendFullOverride?: number | null
  lastWeekendOverride?: number | null
}

export interface RosterWeekendStatUpsertItem {
  staffId: number
  weekendFullOverride: number | null
  lastWeekendOverride: number | null
  overrideReason: string | null
}

export interface RosterWeekendStatReplaceRequest {
  items: RosterWeekendStatUpsertItem[]
}

/** POST /roster-weeks/{weekId}/generate */
export type RosterGenerateStrategy = 'FILL_UNCONFIRMED' | 'OVERWRITE_ALL'

export interface RosterWeekGenerateRequest {
  strategy?: RosterGenerateStrategy
  respectManualConfirmed?: number
  dryRun?: number
  reason?: string | null
}

export interface RosterWeekGenerateResponse {
  weekId: number
  strategy: string
  generatedCellCount: number
  overwrittenCellCount: number
  skippedConfirmedCount: number
  dryRun: number
  message: string
}

/** CalendarDayResponse */
export interface MedirCalendarDay {
  id: number
  calDate: string
  dayType: string
  holidayName: string
  createdAt?: string
  updatedAt?: string
}

export interface CalendarDayUpsertRequest {
  calDate: string
  dayType: string
  holidayName?: string | null
}
