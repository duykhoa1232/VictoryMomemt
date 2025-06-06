// src/app/shared/models/user.model.ts
export interface ProfileRequest {
  name: string;
  phoneNumber?: string;
}

export interface ProfileResponse {
  id: string;
  name: string;
  email: string;
  phoneNumber?: string;
  avatarUrl?: string;
}

export interface UserResponse {
  id: string;
  name: string;
  email: string;
  avatarUrl?: string;
}
