
export interface ProfileRequest {
  name: string;
  phoneNumber?: string;
}

export interface ProfileResponse {
  id: string;
  name: string;
  email: string;
  phoneNumber?: string;
  avatarUrl?: string; // URL của ảnh đại diện
  // Thêm các trường khác mà ProfileResponse của bạn trả về
}

export interface UserResponse { // Được sử dụng trong PostResponse và CommentResponse
  id: string;
  name: string;
  email: string;
  avatarUrl?: string;
  // Các trường khác nếu có
}
