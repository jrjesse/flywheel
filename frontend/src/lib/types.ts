export type UserRole = "ADMIN" | "MANAGER" | "AGENT" | "VIEWER";

export interface AuthUser {
  userId: string;
  tenantId: string;
  email: string;
  displayName: string;
  roles: UserRole[];
}

export interface AuthResponse {
  token: string;
  webhookSecret?: string;
  userId: string;
  tenantId: string;
  email: string;
  displayName: string;
  role: UserRole;
}

export interface Lead {
  id: number;
  name: string;
  email?: string;
  phone?: string;
  status: string;
  score?: number;
  assignedToUserId?: string;
  tenantId?: string;
  createdAt?: string;
}

export interface UserResponse {
  id: string;
  email: string;
  displayName: string;
  role: UserRole;
  active: boolean;
  createdAt: string;
}

export interface QueueInteraction {
  id: string;
  leadId: number;
  leadName: string;
  status: string;
  channel: string;
  assignedAgentId?: string;
  createdAt: string;
  updatedAt: string;
}
