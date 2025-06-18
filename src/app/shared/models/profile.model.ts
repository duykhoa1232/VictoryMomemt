// src/app/shared/models/profile.model.ts

// Định nghĩa interface cho ProfileRequest (tương ứng với ProfileRequest DTO bên Java)
export interface ProfileRequest {
  name: string;
  phoneNumber: string; // Đảm bảo khớp với tên trường trong DTO Java
  bio: string;
  // Thêm các trường khác nếu ProfileRequest DTO bên Java có
  // Ví dụ: gender?: string; // Sử dụng dấu '?' nếu trường này là optional trong request
  // Ví dụ: address?: string;
}

// Nếu bạn cũng muốn định nghĩa PostResponse, hãy thêm nó vào đây hoặc trong một file riêng
export interface PostResponse {
  id: string;
  author: UserResponse | null; // Cần định nghĩa UserResponse nếu có
  content: string;
  imageUrls: string[];
  videoUrls: string[];
  audioUrls: string[];
  location: string;
  visibilityStatus: string;
  authorizedViewerIds: string[];
  tags: string[];
  likeCount: number;
  likedByUsers: string[];
  commentCount: number;
  shareCount: number;
  createdAt: string;
  updatedAt: string;
  isActive: boolean;
  isLikedByCurrentUser: boolean;
}

// Nếu bạn muốn hiển thị thông tin tác giả trong PostResponse, bạn cần UserResponse
export interface UserResponse {
  id: string;
  name: string;
  email: string;
  avatarUrl: string;
  // Thêm các trường khác nếu UserResponse DTO bên Java có
}

// Định nghĩa interface cho ProfileResponse
export interface ProfileResponse {
  id: string;
  name: string;
  email: string;
  phoneNumber: string;
  avatarUrl?: string; // **Đảm bảo có dấu '?' ở đây nếu avatarUrl có thể là null/undefined từ backend.**
  bio: string;
  createdAt: string; // LocalDateTime từ Java sẽ được deserialize thành string ISO 8601
  updatedAt: string; // LocalDateTime từ Java sẽ được deserialize thành string ISO 8601
  enabled: boolean;
  userPosts: { // Đây là Page<PostResponse> từ Spring, cần định nghĩa cấu trúc của Page
    content: PostResponse[];
    pageable: any; // Bạn có thể định nghĩa interface cho Pageable nếu cần chi tiết
    last: boolean;
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
    sort: any; // Tương tự, có thể định nghĩa interface cho Sort
    first: boolean;
    numberOfElements: number;
    empty: boolean;
  } | null;
}
