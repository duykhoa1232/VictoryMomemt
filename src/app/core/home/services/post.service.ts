// src/app/core/home/services/post.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Định nghĩa Interface cho một bài đăng (Post)
// Cập nhật để phù hợp với PostResponse DTO từ backend
export interface Post {
  id: string;      // Đổi từ _id sang id để khớp với backend DTO (PostResponse)
  userId: string;
  userEmail: string; // Thêm trường này nếu backend gửi về
  userName: string;  // Thêm trường này vì backend đã gửi về userName
  content: string;
  location?: string; // Địa điểm (tùy chọn)
  imageUrls?: string[]; // Đổi từ 'images' sang 'imageUrls' để khớp với backend DTO
  videoUrls?: string[]; // Thêm nếu backend gửi về
  audioUrls?: string[]; // Thêm nếu backend gửi về

  createdAt: string; // Thời gian tạo bài đăng (backend gửi LocalDateTime, Angular xử lý string và pipe date)
  updatedAt: string; // Thời gian cập nhật bài đăng

  // Các trường này cần phải khớp với cách backend trả về
  // Nếu backend chỉ trả về likeCount và commentCount (số nguyên), thì interface phải là số nguyên
  // Nếu backend trả về mảng các ID người dùng đã like, thì vẫn giữ string[]
  // Dựa trên PostResponse của bạn, backend chỉ gửi likeCount và commentCount là số nguyên.
  // Tuy nhiên, vì `PostListComponent` của bạn không dùng `likes.length` hay `comments.length` nữa
  // (mà đã comment các nút like/comment), chúng ta có thể giữ interface này đơn giản hơn.
  // Nhưng để khớp với PostResponse, hãy sửa lại:
  likeCount: number; // Đổi từ 'likes: string[]' sang 'likeCount: number'
  commentCount: number; // Đổi từ 'comments: Comment[]' sang 'commentCount: number'
  shareCount: number; // Thêm trường này nếu backend gửi về
  isActive: boolean; // Thêm trường này nếu backend gửi về
}

// Định nghĩa Interface cho Comment (chỉ cần nếu backend trả về chi tiết comment object)
// Hiện tại PostResponse chỉ có commentCount, không có mảng Comment objects.
// Nếu bạn muốn hiển thị chi tiết comment, backend cần trả về mảng này.
// Tạm thời có thể giữ lại hoặc đơn giản hóa nó.
export interface Comment {
  _id: string; // ID của comment (nếu có từ backend)
  userId: string;
  text: string;
  createdAt: string;
  userName?: string; // Tên người dùng bình luận (có thể lấy từ userId hoặc backend gửi trực tiếp)
}


@Injectable({
  providedIn: 'root',
})
export class PostService {
  private apiUrl = 'http://localhost:8080/api/posts';

  constructor(private http: HttpClient) {}

  createPost(postData: FormData): Observable<any> {
    return this.http.post<any>(this.apiUrl, postData);
  }

  // Kiểu dữ liệu trả về từ getAllPosts() sẽ được Angular tự động map
  // Đảm bảo kiểu Post[] đã được cập nhật ở trên.
  getAllPosts(): Observable<Post[]> {
    return this.http.get<Post[]>(this.apiUrl);
  }

  likePost(postId: string): Observable<Post> {
    // Sau khi thay đổi `Post` interface, bạn cần đảm bảo backend trả về đúng Post object khi like
    // (tức là có 'id' thay vì '_id', 'imageUrls' thay vì 'images', v.v.)
    return this.http.put<Post>(`${this.apiUrl}/${postId}/like`, {});
  }
}
