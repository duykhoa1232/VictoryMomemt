// src/app/shared/models/share.model.ts
import { PostResponse } from './post.model'; // Đảm bảo đường dẫn này đúng
import { UserResponse } from './profile.model'; // Đảm bảo đường dẫn này đúng

export interface ShareRequest {
  originalPostId: string; // ID của bài đăng gốc được chia sẻ
  content?: string; // Nội dung tùy chỉnh được người chia sẻ thêm vào (tùy chọn)
}

export interface ShareResponse {
  id: string; // ID của chính đối tượng chia sẻ
  originalPostId: string; // ID của bài đăng gốc
  sharedBy: UserResponse; // Thông tin về người dùng đã chia sẻ bài đăng
  content?: string; // Nội dung tùy chỉnh của chia sẻ này
  originalPost: PostResponse; // Thông tin đầy đủ về bài đăng gốc đang được chia sẻ
  createdAt: string; // Dấu thời gian khi chia sẻ được tạo (chuỗi ISO 8601)
  updatedAt: string; // Dấu thời gian khi chia sẻ được cập nhật lần cuối (chuỗi ISO 8601)
  isActive: boolean; // Trạng thái của chia sẻ (ví dụ: nếu bài đăng gốc vẫn còn hoạt động)
}

export interface Page<T> {
  content: T[]; // Danh sách các mục trong trang hiện tại
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
  last: boolean; // True nếu đây là trang cuối
  totalElements: number; // Tổng số phần tử trên tất cả các trang
  totalPages: number; // Tổng số trang
  size: number; // Số phần tử trên mỗi trang
  number: number; // Số trang hiện tại (0-indexed)
  first: boolean; // True nếu đây là trang đầu tiên
  numberOfElements: number; // Số phần tử trong trang hiện tại
  empty: boolean; // True nếu trang hiện tại trống
}
