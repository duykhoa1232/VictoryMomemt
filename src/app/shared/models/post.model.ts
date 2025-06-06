// src/app/shared/models/post.model.ts
import { UserResponse } from './user.model';

export interface PostRequest {
  content: string;
  location?: string;
  privacy: string; // <-- Đã đổi từ VisibilityStatus sang String
  sharedWithUserIds?: string[];
  tags?: string[];
}

export interface PostResponse {
  id: string;
  author: UserResponse;
  content: string;
  imageUrl?: string;
  videoUrl?: string;
  audioUrl?: string;
  imageUrls?: string[]; // Sửa lại thành số nhiều
  videoUrls?: string[]; // Sửa lại thành số nhiều
  audioUrls?: string[]; // Sửa lại thành số nhiều
  location?: string;
  visibilityStatus: string; // <-- Đã đổi từ VisibilityStatus sang String
  authorizedViewerIds?: string[];
  tags?: string[];
  likeCount: number;
  isLikedByCurrentUser: boolean;
  commentCount: number;
  shareCount: number;
  createdAt: string;
  updatedAt: string;
  isActive: boolean;
}
