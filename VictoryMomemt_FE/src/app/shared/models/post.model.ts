// src/app/shared/models/post.model.ts
import { UserResponse } from './user.model'; // Import UserResponse

export interface PostRequest {
  title: string;
  content: string;
  location?: string;
  privacy: 'PUBLIC' | 'PRIVATE';
  // Không bao gồm file ở đây vì nó sẽ được thêm vào FormData
  // allowedUserIds?: string[]; // Nếu bạn có một trường để chỉ định người dùng được phép xem
}

export interface PostResponse {
  id: string;
  userId: string;
  userEmail: string;
  userName: string;
  content: string;
  location?: string;
  imageUrls?: string[];
  videoUrls?: string[];
  audioUrls?: string[];
  privacy: 'PUBLIC' | 'PRIVATE';
  allowedUserIds?: string[];
  createdAt: string;
  updatedAt: string;

  likeCount: number;
  commentCount: number;
  shareCount: number;
  isActive: boolean;

  // Thêm các trường từ BE của bạn
  author: UserResponse; // Thay thế userId, userEmail, userName bằng object UserResponse
  isLikedByCurrentUser: boolean; // Để biết người dùng hiện tại đã like bài này chưa
}
