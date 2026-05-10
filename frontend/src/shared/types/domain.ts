export type Role = 'OWNER' | 'CONTENT_MANAGER' | 'EXECUTOR';
export type ContentType = 'POST' | 'STORY' | 'VIDEO' | 'ARTICLE' | 'REELS' | 'OTHER';
export type ContentUnitStatus = 'DRAFT' | 'IN_PROGRESS' | 'ON_REVIEW' | 'NEEDS_CHANGES' | 'APPROVED' | 'SCHEDULED' | 'PARTIALLY_PUBLISHED' | 'PUBLISHED' | 'FAILED';
export type TaskType = 'COPYWRITING' | 'DESIGN' | 'VIDEO_EDITING' | 'REVIEW' | 'PUBLICATION_PREP' | 'OTHER';
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'ON_REVIEW' | 'DONE' | 'CANCELED';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type ApprovalStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
export type Platform = 'TELEGRAM' | 'VK' | 'TENCHAT' | 'SETKA' | 'MAX' | 'MASTODON' | 'OTHER';
export type PlatformMode = 'AUTO' | 'MANUAL' | 'AUTO_WITH_MANUAL_FALLBACK';
export type PublicationVariantStatus = 'DRAFT' | 'READY' | 'SCHEDULED' | 'PUBLISHING' | 'PUBLISHED' | 'MANUAL_REQUIRED' | 'MANUAL_COMPLETED' | 'CANCELED';
export type PublicationAttemptStatus = 'SUCCESS' | 'FAILED';

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: Role;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface AuthResponse {
  accessToken: string;
  user: User;
}

export interface ContentUnit {
  id: number;
  title: string;
  description?: string;
  baseText?: string;
  contentType: ContentType;
  status: ContentUnitStatus;
  createdBy?: User;
  responsibleUser?: User;
  plannedPublishAt?: string;
  createdAt?: string;
  updatedAt?: string;
  tasksCount: number;
  variantsCount: number;
}

export interface Task {
  id: number;
  contentUnitId: number;
  contentUnitTitle: string;
  title: string;
  description?: string;
  type: TaskType;
  status: TaskStatus;
  priority: TaskPriority;
  assignee: User;
  createdBy?: User;
  deadline?: string;
  resultComment?: string;
  reviewComment?: string;
  overdue: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface MediaFile {
  id: number;
  contentUnitId: number;
  taskId?: number;
  originalName: string;
  mimeType: string;
  size: number;
  downloadUrl: string;
  uploadedById: number;
  uploadedAt: string;
}

export interface Approval {
  id: number;
  contentUnit: ContentUnit;
  reviewer: User;
  status: ApprovalStatus;
  comment?: string;
  createdAt?: string;
  reviewedAt?: string;
}

export interface PublicationVariant {
  id: number;
  contentUnitId: number;
  contentUnitTitle: string;
  platform: Platform;
  adaptedText?: string;
  scheduledAt?: string;
  status: PublicationVariantStatus;
  externalPostId?: string;
  externalPostUrl?: string;
  errorMessage?: string;
  manualInstruction?: string;
  manualCompletedById?: number;
  manualCompletedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface PublicationAttempt {
  id: number;
  publicationVariantId: number;
  attemptNumber: number;
  status: PublicationAttemptStatus;
  errorMessage?: string;
  responsePayload?: string;
  createdAt?: string;
}

export interface ManualPublicationDetails {
  contentUnit: ContentUnit;
  variant: PublicationVariant;
  platformUrl: string;
  mediaFiles: MediaFile[];
}

export interface PlatformSetting {
  platform: Platform;
  enabled: boolean;
  mode: PlatformMode;
  tokenConfigured: boolean;
  communityId?: string;
  communityConfigured?: boolean;
  manualUrl?: string;
  instanceUrl?: string;
  apiVersion?: string;
  updatedAt?: string;
}

export interface AnalyticsSummary {
  content: Record<string, number>;
  tasks: Record<string, number>;
  publications: Record<string, number>;
  notifications: Record<string, number>;
}

export interface NotificationItem {
  id: number;
  type: string;
  message: string;
  link?: string;
  isRead: boolean;
  createdAt: string;
}

export const roles: Role[] = ['OWNER', 'CONTENT_MANAGER', 'EXECUTOR'];
export const contentTypes: ContentType[] = ['POST', 'STORY', 'VIDEO', 'ARTICLE', 'REELS', 'OTHER'];
export const taskTypes: TaskType[] = ['COPYWRITING', 'DESIGN', 'VIDEO_EDITING', 'REVIEW', 'PUBLICATION_PREP', 'OTHER'];
export const taskPriorities: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];
export const platforms: Platform[] = ['TELEGRAM', 'VK', 'TENCHAT', 'SETKA', 'MAX', 'MASTODON'];
export const platformModes: PlatformMode[] = ['AUTO', 'AUTO_WITH_MANUAL_FALLBACK', 'MANUAL'];
