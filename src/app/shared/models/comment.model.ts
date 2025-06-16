export interface CommentRequest {
  content: string;
  parentCommentId?: string; // Optional, vì có thể null trong DTO
}

export interface CommentResponse {
  id: string;
  postId: string;
  userId: string;
  userEmail: string;
  userName: string;
  content: string;
  parentCommentId?: string; // Optional, vì có thể null
  replyCount: number;
  createdAt: Date; // Sử dụng Date thay vì LocalDateTime để dễ xử lý trong TypeScript
  updatedAt: Date; // Sử dụng Date thay vì LocalDateTime
  isActive: boolean;
  replies?: CommentResponse[]; // Thêm trường replies để hỗ trợ cây bình luận
}


