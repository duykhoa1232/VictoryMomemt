// src/app/shared/models/comment.model.ts
import { UserResponse } from './user.model'; // Import UserResponse

export interface CommentRequest {
  content: string;
}

export interface CommentResponse {
  id: string;
  content: string;
  createdAt: string;
  author: UserResponse; // Người bình luận
  // Thêm các trường khác nếu CommentResponse của bạn có
}
