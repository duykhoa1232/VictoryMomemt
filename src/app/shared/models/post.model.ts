// // src/app/shared/models/post.model.ts
// import { UserResponse } from './user.model'; // Đảm bảo đường dẫn này đúng
//
// export interface PostRequest {
//   content: string;
//   location?: string;
//   privacy: string;
//   sharedWithUserIds?: string[];
//   tags?: string[];
//   images?: File[]; // Thêm nếu cần upload
//   videos?: File[]; // Thêm nếu cần upload
//   audios?: File[]; // Thêm nếu cần upload
// }
//
// export interface PostResponse {
//   id: string;
//   author: UserResponse;
//   content: string;
//   imageUrls?: string[];
//   videoUrls?: string[];
//   audioUrls?: string[];
//   location?: string;
//   visibilityStatus: string;
//   authorizedViewerIds?: string[];
//   tags?: string[];
//   likeCount: number;
//   isLikedByCurrentUser: boolean;
//   commentCount: number;
//   shareCount: number;
//   createdAt: string;
//   updatedAt: string;
//   isActive: boolean;
//   isOwnedByCurrentUser: boolean; // Đã thêm từ backend
// }
//
// export interface Page<T> {
//   content: T[];
//   pageable: {
//     pageNumber: number;
//     pageSize: number;
//     sort: {
//       empty: boolean;
//       sorted: boolean;
//       unsorted: boolean;
//     };
//     offset: number;
//     paged: boolean;
//     unpaged: boolean;
//   };
//   last: boolean;
//   totalElements: number;
//   totalPages: number;
//   size: number;
//   number: number;
//   sort: {
//     empty: boolean;
//     sorted: boolean;
//     unsorted: boolean;
//   };
//   first: boolean;
//   numberOfElements: number;
//   empty: boolean;
// }



import { UserResponse } from './profile.model'; // Đảm bảo đường dẫn này đúng với UserResponse của bạn
import { CommentResponse } from './comment.model'; // Thêm import này

export interface PostRequest {
  content: string;
  location?: string;
  privacy: string; // PUBLIC, PRIVATE, CUSTOM (điều chỉnh nếu bạn có enum cụ thể)
  sharedWithUserIds?: string[]; // Dùng cho privacy CUSTOM
  tags?: string[];
  images?: File[]; // Thêm nếu cần upload file mới (dùng với FormData)
  videos?: File[]; // Thêm nếu cần upload file mới
  audios?: File[]; // Thêm nếu cần upload file mới
  // Các trường này có thể cần nếu bạn muốn chỉ định file cũ nào cần xóa khi cập nhật bài đăng
  // imageUrlsToRemove?: string[];
  // videoUrlsToRemove?: string[];
  // audioUrlsToRemove?: string[];
}

export interface PostResponse {
  id: string;
  author: UserResponse;
  content: string;
  imageUrls?: string[];
  videoUrls?: string[];
  audioUrls?: string[];
  location?: string;
  visibilityStatus: string; // PUBLIC, PRIVATE, CUSTOM (đảm bảo khớp với backend)
  authorizedViewerIds?: string[]; // Danh sách người được xem (nếu visibilityStatus là CUSTOM)
  tags?: string[];
  likeCount: number;
  isLikedByCurrentUser: boolean;
  commentCount: number;
  shareCount: number;
  createdAt: string; // Giữ là string (ISO 8601) hoặc Date nếu bạn tự parse
  updatedAt: string; // Giữ là string (ISO 8601) hoặc Date nếu bạn tự parse
  isActive: boolean;
  isOwnedByCurrentUser: boolean; // Để biết người dùng hiện tại có sở hữu bài đăng này không
  comments?: CommentResponse[]; // THÊM TRƯỜNG NÀY ĐỂ LƯU DANH SÁCH BÌNH LUẬN (bao gồm cả replies)
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
