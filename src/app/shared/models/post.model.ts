// src/app/shared/models/post.model.ts
import { UserResponse } from './user.model'; // Đảm bảo đường dẫn này đúng

export interface PostRequest {
  content: string;
  location?: string;
  privacy: string;
  sharedWithUserIds?: string[];
  tags?: string[];
  images?: File[]; // Thêm nếu cần upload
  videos?: File[]; // Thêm nếu cần upload
  audios?: File[]; // Thêm nếu cần upload
}

export interface PostResponse {
  id: string;
  author: UserResponse;
  content: string;
  imageUrls?: string[];
  videoUrls?: string[];
  audioUrls?: string[];
  location?: string;
  visibilityStatus: string;
  authorizedViewerIds?: string[];
  tags?: string[];
  likeCount: number;
  isLikedByCurrentUser: boolean;
  commentCount: number;
  shareCount: number;
  createdAt: string;
  updatedAt: string;
  isActive: boolean;
  isOwnedByCurrentUser: boolean; // Đã thêm từ backend
}

export interface Page<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}
