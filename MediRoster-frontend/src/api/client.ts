import axios, { type AxiosError } from 'axios'
import type { ApiResponse } from './medir/types'

export class ApiBusinessError extends Error {
  readonly code: string
  readonly httpStatus?: number

  constructor(code: string, message: string, httpStatus?: number) {
    super(message)
    this.name = 'ApiBusinessError'
    this.code = code
    this.httpStatus = httpStatus
  }
}

function getBaseURL(): string {
  return import.meta.env.VITE_API_BASE_URL ?? ''
}

export const apiClient = axios.create({
  baseURL: getBaseURL(),
  timeout: 60_000,
  headers: {
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.request.use((config) => {
  if (typeof navigator !== 'undefined' && navigator.language) {
    const lang = navigator.language
    const h = config.headers
    if (h && typeof (h as { set?: (k: string, v: string) => void }).set === 'function') {
      ;(h as { set: (k: string, v: string) => void }).set('Accept-Language', lang)
    } else {
      ;(config.headers as Record<string, string>)['Accept-Language'] = lang
    }
  }
  return config
})

/** 成功且 `data` 有业务载荷（非 null） */
export async function unwrap<T>(promise: Promise<{ data: ApiResponse<T | null> }>): Promise<T> {
  const { data } = await promise
  if (!data.success) {
    throw new ApiBusinessError(data.code ?? 'UNKNOWN', data.message ?? '请求失败')
  }
  if (data.data === null || data.data === undefined) {
    throw new ApiBusinessError('EMPTY_DATA', '响应 data 为空')
  }
  return data.data
}

/** 删除、仅状态成功等：`data` 可为 null */
export async function unwrapOk(promise: Promise<{ data: ApiResponse<unknown> }>): Promise<void> {
  const { data } = await promise
  if (!data.success) {
    throw new ApiBusinessError(data.code ?? 'UNKNOWN', data.message ?? '请求失败')
  }
}

export function getAxiosErrorMessage(err: unknown): string {
  if (axios.isAxiosError(err)) {
    const ax = err as AxiosError<ApiResponse<unknown>>
    const body = ax.response?.data
    if (body && typeof body === 'object' && 'message' in body && typeof body.message === 'string') {
      return body.message
    }
    return ax.message
  }
  if (err instanceof Error) return err.message
  return String(err)
}
