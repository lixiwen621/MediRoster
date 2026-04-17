import { apiClient, unwrap, unwrapOk } from '@/api/client'
import type { AxiosResponse } from 'axios'
import type {
  ApiResponse,
  CalendarDayUpsertRequest,
  ConfigUpsertRequest,
  HealthDto,
  MedirCalendarDay,
  MedirConfig,
  MedirPost,
  MedirRosterCell,
  MedirRosterWeekendStat,
  MedirRosterWeek,
  MedirRuleMeta,
  MedirShiftType,
  MedirStaff,
  MedirStaffCapability,
  MedirStaffPostRow,
  MedirTeam,
  PostUpsertRequest,
  RosterCellReplaceRequest,
  RosterStaffPostReplaceRequest,
  RosterWeekendStatReplaceRequest,
  RosterWeekGenerateRequest,
  RosterWeekGenerateResponse,
  RosterWeekCreateRequest,
  RosterWeekUpdateRequest,
  ShiftTypeUpdateRequest,
  StaffCapabilityUpsertRequest,
  StaffUpsertRequest,
  TeamUpsertRequest,
} from '@/api/medir/types'

const M = '/api/v1/medir'

export async function getHealth(): Promise<HealthDto> {
  return unwrap(apiClient.get<ApiResponse<HealthDto | null>>('/api/v1/health'))
}

/** 班组 */
export async function listTeams(): Promise<MedirTeam[]> {
  return unwrap(apiClient.get(`${M}/teams`))
}
export async function getTeam(id: number): Promise<MedirTeam> {
  return unwrap(apiClient.get(`${M}/teams/${id}`))
}
export async function createTeam(body: TeamUpsertRequest): Promise<MedirTeam> {
  return unwrap(apiClient.post(`${M}/teams`, body))
}
export async function updateTeam(id: number, body: TeamUpsertRequest): Promise<MedirTeam> {
  return unwrap(apiClient.put(`${M}/teams/${id}`, body))
}
export async function deleteTeam(id: number): Promise<void> {
  await unwrapOk(apiClient.delete(`${M}/teams/${id}`))
}

/** 岗位（全局列表，无 teamId） */
export async function listPosts(): Promise<MedirPost[]> {
  return unwrap(apiClient.get(`${M}/posts`))
}
export async function getPost(id: number): Promise<MedirPost> {
  return unwrap(apiClient.get(`${M}/posts/${id}`))
}
export async function createPost(body: PostUpsertRequest): Promise<MedirPost> {
  return unwrap(apiClient.post(`${M}/posts`, body))
}
export async function updatePost(id: number, body: PostUpsertRequest): Promise<MedirPost> {
  return unwrap(apiClient.put(`${M}/posts/${id}`, body))
}
export async function deletePost(id: number): Promise<void> {
  await unwrapOk(apiClient.delete(`${M}/posts/${id}`))
}

/** 班次类型 */
export async function listShiftTypes(): Promise<MedirShiftType[]> {
  return unwrap(apiClient.get(`${M}/shift-types`))
}
export async function getShiftType(id: number): Promise<MedirShiftType> {
  return unwrap(apiClient.get(`${M}/shift-types/${id}`))
}
export async function updateShiftType(id: number, body: ShiftTypeUpdateRequest): Promise<MedirShiftType> {
  return unwrap(apiClient.put(`${M}/shift-types/${id}`, body))
}

/** 人员 */
export async function listStaff(params: { teamId: number; includeDeleted?: boolean }): Promise<MedirStaff[]> {
  return unwrap(apiClient.get(`${M}/staff`, { params }))
}
export async function getStaff(id: number): Promise<MedirStaff> {
  return unwrap(apiClient.get(`${M}/staff/${id}`))
}
export async function createStaff(body: StaffUpsertRequest): Promise<MedirStaff> {
  return unwrap(apiClient.post(`${M}/staff`, body))
}
export async function updateStaff(id: number, body: StaffUpsertRequest): Promise<MedirStaff> {
  return unwrap(apiClient.put(`${M}/staff/${id}`, body))
}
export async function deleteStaff(id: number): Promise<void> {
  await unwrapOk(apiClient.delete(`${M}/staff/${id}`))
}

/** 人员能力 */
export async function listCapabilities(staffId: number): Promise<MedirStaffCapability[]> {
  return unwrap(apiClient.get(`${M}/staff/${staffId}/capabilities`))
}
export async function addCapability(staffId: number, body: StaffCapabilityUpsertRequest): Promise<MedirStaffCapability> {
  return unwrap(apiClient.post(`${M}/staff/${staffId}/capabilities`, body))
}
export async function deleteCapability(staffId: number, capabilityId: number): Promise<void> {
  await unwrapOk(apiClient.delete(`${M}/staff/${staffId}/capabilities/${capabilityId}`))
}

/** 规则键值 */
export async function listConfig(params?: { teamId?: number }): Promise<MedirConfig[]> {
  return unwrap(apiClient.get(`${M}/config`, { params }))
}
export async function getConfig(id: number): Promise<MedirConfig> {
  return unwrap(apiClient.get(`${M}/config/${id}`))
}
export async function createConfig(body: ConfigUpsertRequest): Promise<MedirConfig> {
  return unwrap(apiClient.post(`${M}/config`, body))
}
export async function updateConfig(id: number, body: ConfigUpsertRequest): Promise<MedirConfig> {
  return unwrap(apiClient.put(`${M}/config/${id}`, body))
}
export async function deleteConfig(id: number): Promise<void> {
  await unwrapOk(apiClient.delete(`${M}/config/${id}`))
}

/** 规则元数据 */
export async function listRuleMeta(): Promise<MedirRuleMeta[]> {
  return unwrap(apiClient.get(`${M}/rule-meta`))
}

/** 排班周 — 列表查询参数为 teamId + year */
export async function listRosterWeeks(params: { teamId: number; year: number }): Promise<MedirRosterWeek[]> {
  return unwrap(apiClient.get(`${M}/roster-weeks`, { params }))
}
export async function getRosterWeek(id: number): Promise<MedirRosterWeek> {
  return unwrap(apiClient.get(`${M}/roster-weeks/${id}`))
}
export async function createRosterWeek(body: RosterWeekCreateRequest): Promise<MedirRosterWeek> {
  return unwrap(apiClient.post(`${M}/roster-weeks`, body))
}
export async function updateRosterWeek(id: number, body: RosterWeekUpdateRequest): Promise<MedirRosterWeek> {
  return unwrap(apiClient.put(`${M}/roster-weeks/${id}`, body))
}
export async function deleteRosterWeek(id: number): Promise<void> {
  await unwrapOk(apiClient.delete(`${M}/roster-weeks/${id}`))
}

export async function getRosterWeekCells(weekId: number): Promise<MedirRosterCell[]> {
  return unwrap(apiClient.get(`${M}/roster-weeks/${weekId}/cells`))
}

export async function putRosterWeekCells(weekId: number, body: RosterCellReplaceRequest): Promise<void> {
  await unwrapOk(apiClient.put(`${M}/roster-weeks/${weekId}/cells`, body))
}

export async function getRosterWeekStaffPosts(weekId: number): Promise<MedirStaffPostRow[]> {
  return unwrap(apiClient.get(`${M}/roster-weeks/${weekId}/staff-posts`))
}

export async function putRosterWeekStaffPosts(weekId: number, body: RosterStaffPostReplaceRequest): Promise<void> {
  await unwrapOk(apiClient.put(`${M}/roster-weeks/${weekId}/staff-posts`, body))
}

export async function getRosterWeekWeekendStats(weekId: number): Promise<MedirRosterWeekendStat[]> {
  return unwrap(apiClient.get(`${M}/roster-weeks/${weekId}/weekend-stats`))
}

export async function putRosterWeekWeekendStats(
  weekId: number,
  body: RosterWeekendStatReplaceRequest,
): Promise<void> {
  await unwrapOk(apiClient.put(`${M}/roster-weeks/${weekId}/weekend-stats`, body))
}

export async function generateRosterWeek(
  weekId: number,
  body: RosterWeekGenerateRequest,
): Promise<RosterWeekGenerateResponse> {
  return unwrap(apiClient.post(`${M}/roster-weeks/${weekId}/generate`, body))
}

export async function exportRosterWeekExcel(
  weekId: number,
  params?: { filename?: string },
): Promise<AxiosResponse<Blob>> {
  return apiClient.get(`${M}/roster-weeks/${weekId}/export`, {
    params,
    responseType: 'blob',
  })
}

/** 日历日 */
export async function listCalendarDays(params: { from: string; to: string }): Promise<MedirCalendarDay[]> {
  return unwrap(apiClient.get(`${M}/calendar-days`, { params }))
}
export async function getCalendarDay(id: number): Promise<MedirCalendarDay> {
  return unwrap(apiClient.get(`${M}/calendar-days/${id}`))
}
export async function createCalendarDay(body: CalendarDayUpsertRequest): Promise<MedirCalendarDay> {
  return unwrap(apiClient.post(`${M}/calendar-days`, body))
}
export async function updateCalendarDay(id: number, body: CalendarDayUpsertRequest): Promise<MedirCalendarDay> {
  return unwrap(apiClient.put(`${M}/calendar-days/${id}`, body))
}
export async function deleteCalendarDay(id: number): Promise<void> {
  await unwrapOk(apiClient.delete(`${M}/calendar-days/${id}`))
}
