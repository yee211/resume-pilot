import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 120000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('email')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export interface Result<T> {
  code: number
  message: string
  data: T
}

// ========== 认证 ==========
export function sendCode(email: string) {
  return api.post<Result<null>>(`/auth/send-code?email=${encodeURIComponent(email)}`)
}

export function register(email: string, password: string, code: string) {
  return api.post<Result<null>>('/auth/register', { email, password, code })
}

export function login(email: string, password: string) {
  return api.post<Result<{ token: string; email: string }>>('/auth/login', { email, password })
}

// ========== 简历 ==========
export function uploadResume(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return api.post<Result<any>>('/resume/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getResume(id: number) {
  return api.get<Result<any>>(`/resume/${id}`)
}

export function getMyResumes() {
  return api.get<Result<any[]>>('/resume/my')
}

// ========== ATS（异步提交 + 轮询） ==========
export function submitAts(resumeId: number) {
  return api.post<Result<any>>(`/ats/analyze/${resumeId}`)
}

export function getAtsResult(id: number) {
  return api.get<Result<any>>(`/ats/${id}`)
}

// ========== JD（异步提交 + 轮询） ==========
export function submitJd(resumeId: number, jdText: string) {
  return api.post<Result<any>>('/jd/match', { resumeId, jdText })
}

export function getJdResult(id: number) {
  return api.get<Result<any>>(`/jd/${id}`)
}
