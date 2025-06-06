// src/app/shared/models/comment.model.ts
import { UserResponse } from './user.model';

export interface CommentRequest {
  content: string;
}

export interface CommentResponse {
  id: string;
  content: string;
  createdAt: string;
  author: UserResponse;
}
