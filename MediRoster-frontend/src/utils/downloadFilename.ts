function decodeRfc5987(value: string): string {
  // 形如: UTF-8''%E4%B8%B4%E6%A3%80%E7%BB%84_2026-04-13_v0.xlsx
  const m = value.match(/^([^']*)''(.*)$/)
  if (!m) return value
  const encoded = m[2] || ''
  try {
    return decodeURIComponent(encoded)
  } catch {
    return encoded
  }
}

export function getDownloadFilename(contentDisposition?: string): string {
  if (!contentDisposition) return 'roster.xlsx'

  // 1) 优先 filename*
  // 兼容: filename*=UTF-8''... 或 filename*=...;
  const starMatch = contentDisposition.match(/filename\*\s*=\s*([^;]+)/i)
  if (starMatch?.[1]) {
    const raw = starMatch[1].trim().replace(/^"|"$/g, '')
    const decoded = decodeRfc5987(raw)
    if (decoded) return decoded
  }

  // 2) 其次 filename
  const normalMatch = contentDisposition.match(/filename\s*=\s*([^;]+)/i)
  if (normalMatch?.[1]) {
    const raw = normalMatch[1].trim().replace(/^"|"$/g, '')
    if (raw) return raw
  }

  // 3) 兜底
  return 'roster.xlsx'
}
