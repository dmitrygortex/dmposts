import { apiClient } from './apiClient';
import {
  AnalyticsSummary,
  Approval,
  AuthResponse,
  ContentType,
  ContentUnit,
  ContentUnitStatus,
  ManualPublicationDetails,
  MediaFile,
  NotificationItem,
  PageResponse,
  Platform,
  PlatformMode,
  PlatformSetting,
  PublicationAttempt,
  PublicationVariant,
  PublicationVariantStatus,
  Role,
  Task,
  TaskPriority,
  TaskStatus,
  TaskType,
  User
} from '../shared/types/domain';

export const authApi = {
  setupStatus: () => apiClient.get<{ hasUsers: boolean; registrationAvailable: boolean }>('/auth/setup-status').then((r) => r.data),
  register: (payload: { email: string; password: string; fullName: string }) => apiClient.post<AuthResponse>('/auth/register', payload).then((r) => r.data),
  login: (payload: { email: string; password: string }) => apiClient.post<AuthResponse>('/auth/login', payload).then((r) => r.data),
  me: () => apiClient.get<User>('/auth/me').then((r) => r.data)
};

export const userApi = {
  list: () => apiClient.get<User[]>('/users').then((r) => r.data),
  create: (payload: { email: string; password: string; fullName: string; role: Role }) => apiClient.post<User>('/users', payload).then((r) => r.data),
  updateRole: (id: number, role: Role) => apiClient.patch<User>(`/users/${id}/role`, { role }).then((r) => r.data),
  deactivate: (id: number) => apiClient.patch<User>(`/users/${id}/deactivate`).then((r) => r.data),
  activate: (id: number) => apiClient.patch<User>(`/users/${id}/activate`).then((r) => r.data)
};

export const contentApi = {
  list: (params?: Record<string, unknown>) => apiClient.get<PageResponse<ContentUnit>>('/content-units', { params }).then((r) => r.data),
  create: (payload: { title: string; description?: string; baseText?: string; contentType: ContentType; responsibleUserId?: number; plannedPublishAt?: string }) =>
    apiClient.post<ContentUnit>('/content-units', payload).then((r) => r.data),
  get: (id: number) => apiClient.get<ContentUnit>(`/content-units/${id}`).then((r) => r.data),
  update: (id: number, payload: { title: string; description?: string; baseText?: string; contentType: ContentType; responsibleUserId?: number; plannedPublishAt?: string }) =>
    apiClient.patch<ContentUnit>(`/content-units/${id}`, payload).then((r) => r.data),
  updateBaseText: (id: number, baseText: string) => apiClient.patch<ContentUnit>(`/content-units/${id}/base-text`, { baseText }).then((r) => r.data),
  changeStatus: (id: number, status: ContentUnitStatus) => apiClient.patch<ContentUnit>(`/content-units/${id}/status`, { status }).then((r) => r.data),
  remove: (id: number) => apiClient.delete(`/content-units/${id}`)
};

export const taskApi = {
  list: (params?: Record<string, unknown>) => apiClient.get<PageResponse<Task>>('/tasks', { params }).then((r) => r.data),
  create: (payload: { contentUnitId: number; title: string; description?: string; type: TaskType; priority: TaskPriority; assigneeId: number; deadline?: string }) =>
    apiClient.post<Task>('/tasks', payload).then((r) => r.data),
  get: (id: number) => apiClient.get<Task>(`/tasks/${id}`).then((r) => r.data),
  changeStatus: (id: number, status: TaskStatus, comment?: string) => apiClient.patch<Task>(`/tasks/${id}/status`, { status, comment }).then((r) => r.data)
};

export const mediaApi = {
  list: (params?: { contentUnitId?: number; taskId?: number }) => apiClient.get<MediaFile[]>('/media', { params }).then((r) => r.data),
  upload: (file: File, contentUnitId: number, taskId?: number) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('contentUnitId', String(contentUnitId));
    if (taskId) formData.append('taskId', String(taskId));
    return apiClient.post<MediaFile>('/media/upload', formData).then((r) => r.data);
  },
  download: (id: number) => apiClient.get<Blob>(`/media/${id}/download`, { responseType: 'blob' }).then((r) => r.data),
  remove: (id: number) => apiClient.delete(`/media/${id}`)
};

export const approvalApi = {
  list: (params?: { contentUnitId?: number }) => apiClient.get<Approval[]>('/approvals', { params }).then((r) => r.data),
  submit: (payload: { contentUnitId: number; reviewerId: number; comment?: string }) => apiClient.post<Approval>('/approvals/submit', payload).then((r) => r.data),
  approve: (id: number, comment?: string) => apiClient.post<Approval>(`/approvals/${id}/approve`, { comment }).then((r) => r.data),
  reject: (id: number, comment: string) => apiClient.post<Approval>(`/approvals/${id}/reject`, { comment }).then((r) => r.data)
};

export const publicationApi = {
  list: (params?: { contentUnitId?: number; platform?: Platform; status?: PublicationVariantStatus; from?: string; to?: string; page?: number; size?: number }) =>
    apiClient.get<PageResponse<PublicationVariant>>('/publication-variants', { params }).then((r) => r.data),
  bulkCreate: (contentUnitId: number, platforms: Platform[]) => apiClient.post<PublicationVariant[]>('/publication-variants/bulk', { contentUnitId, platforms }).then((r) => r.data),
  update: (id: number, payload: { adaptedText?: string; scheduledAt?: string; externalPostUrl?: string }) => apiClient.patch<PublicationVariant>(`/publication-variants/${id}`, payload).then((r) => r.data),
  schedule: (id: number, scheduledAt: string) => apiClient.post<PublicationVariant>(`/publication-variants/${id}/schedule`, { scheduledAt }).then((r) => r.data),
  publishNow: (id: number) => apiClient.post<PublicationVariant>(`/publication-variants/${id}/publish-now`).then((r) => r.data),
  retry: (id: number) => apiClient.post<PublicationVariant>(`/publication-variants/${id}/retry`).then((r) => r.data),
  switchToManual: (id: number, reason?: string) => apiClient.post<PublicationVariant>(`/publication-variants/${id}/switch-to-manual`, { reason }).then((r) => r.data),
  manualDetails: (id: number) => apiClient.get<ManualPublicationDetails>(`/publication-variants/${id}/manual`).then((r) => r.data),
  manualComplete: (id: number, externalPostUrl: string) => apiClient.post<PublicationVariant>(`/publication-variants/${id}/manual-complete`, { externalPostUrl }).then((r) => r.data),
  attemptsByContent: (contentUnitId: number) => apiClient.get<PublicationAttempt[]>('/publication-attempts', { params: { contentUnitId } }).then((r) => r.data)
};

export const platformSettingsApi = {
  list: () => apiClient.get<PlatformSetting[]>('/platform-settings').then((r) => r.data),
  update: (platform: Platform, payload: { enabled: boolean; mode: PlatformMode; accessToken?: string; communityId?: string; apiVersion?: string; manualUrl?: string; instanceUrl?: string }) =>
    apiClient.patch<PlatformSetting>(`/platform-settings/${platform}`, payload).then((r) => r.data)
};

export const analyticsApi = {
  summary: () => apiClient.get<AnalyticsSummary>('/analytics/summary').then((r) => r.data)
};

export const notificationApi = {
  list: () => apiClient.get<PageResponse<NotificationItem>>('/notifications', { params: { unreadOnly: false } }).then((r) => r.data),
  readAll: () => apiClient.patch('/notifications/read-all')
};
