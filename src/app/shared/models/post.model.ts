




// src/app/shared/models/post.model.ts
import { UserResponse } from './user.model'; // Đảm bảo đường dẫn này đúng

export interface PostRequest {
  content: string;
  location?: string;
  privacy: string;
  sharedWithUserIds?: string[];
  tags?: string[];
}

export interface PostResponse {
  id: string;
  author: UserResponse;
  content: string;
  // XÓA BỎ CÁC TRƯỜNG NÀY NẾU BẠN CHỈ DÙNG MẢNG (imageUrls, videoUrls, audioUrls)
  // imageUrl?: string;
  // videoUrl?: string;
  // audioUrl?: string;
  imageUrls?: string[]; // Đảm bảo là mảng string
  videoUrls?: string[]; // Đảm bảo là mảng string
  audioUrls?: string[]; // Đảm bảo là mảng string
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
}

// THÊM ĐỊNH NGHĨA INTERFACE PAGE NÀY VÀO ĐÂY
export interface Page<T> {
  content: T[]; // Đây là danh sách dữ liệu thực tế của trang hiện tại
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
  last: boolean; // Có phải trang cuối cùng không
  totalElements: number; // Tổng số phần tử trong toàn bộ tập dữ liệu
  totalPages: number; // Tổng số trang
  size: number; // Kích thước trang được yêu cầu
  number: number; // Số trang hiện tại (0-indexed)
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  first: boolean; // Có phải trang đầu tiên không
  numberOfElements: number; // Số phần tử trên trang hiện tại
  empty: boolean; // Trang hiện tại có rỗng không
}
