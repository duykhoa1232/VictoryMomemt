// src/app/shared/models/auth.model.ts
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  phoneNumber: string;
  password: string;
}

export interface AuthResponse {
  token: string;
}
export interface CurrentUser {
  id: string;
  email: string;
  name?: string;
  avatarUrl?: string; // Thêm trường này để lưu trữ URL avatar từ token (nếu có)
  // Thêm các trường khác của người dùng mà bạn muốn truy cập từ token
}
