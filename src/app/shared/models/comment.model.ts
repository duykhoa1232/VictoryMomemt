

// src/app/shared/models/comment.model.ts

export interface CommentRequest {
  content: string;
  parentCommentId?: string; // ID của comment cha (nếu là reply)
}

export interface CommentResponse {
  id: string;
  postId: string;
  userId: string;
  userEmail: string;
  userName: string;
  userAvatar?: string; // URL avatar người dùng (nếu có từ backend)
  content: string;
  parentCommentId?: string; // ID comment cha
  replyCount: number; // Số lượng reply trực tiếp cho comment này
  createdAt: Date; // Sử dụng Date thay vì LocalDateTime để dễ xử lý trong TypeScript
  updatedAt: Date; // Sử dụng Date thay vì LocalDateTime
  isActive: boolean;
  replies?: CommentResponse[]; // Danh sách các phản hồi cho bình luận này
}
