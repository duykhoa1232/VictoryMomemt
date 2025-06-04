// src/app/core/home/services/post.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs'; // THÊM throwError
import { delay, map, catchError } from 'rxjs/operators'; // THÊM catchError

// THÊM: Định nghĩa User interface ngay trong PostService
export interface User {
  id: string;
  name: string;
  email?: string; // Tùy chọn
  avatarUrl?: string; // THÊM: Nếu bạn có avatarUrl cho User
}

// Định nghĩa Post interface ngay trong PostService
export interface Post {
  id: string;
  userId: string;
  userEmail: string;
  userName: string;
  content: string;
  location?: string;
  imageUrls?: string[];
  videoUrls?: string[];
  audioUrls?: string[];
  privacy: 'PUBLIC' | 'PRIVATE'; // ĐÃ SỬA: Chỉ còn PUBLIC và PRIVATE (PRIVATE bao gồm chia sẻ)
  allowedUserIds?: string[]; // Danh sách userId được phép xem (khi privacy = PRIVATE)
  createdAt: string;
  updatedAt: string;

  likeCount: number;
  commentCount: number;
  shareCount: number;
  isActive: boolean;
}

// Giữ lại Comment interface để hoàn chỉnh, mặc dù không trực tiếp dùng trong PostList để hiển thị comments.
export interface Comment {
  _id: string;
  userId: string;
  text: string;
  createdAt: string;
  userName?: string;
}


@Injectable({
  providedIn: 'root',
})
export class PostService {
  private baseUrl = 'http://localhost:8080/api/posts'; // Base URL cho Post API
  private userApiUrl = 'http://localhost:8080/api/users'; // Base URL cho User API (sẽ dùng để tìm kiếm người dùng)

  constructor(private http: HttpClient) {}

  // Hàm trợ giúp để lấy các header xác thực (giả định JWT hoặc token tương tự)
  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('jwt_token'); // Lấy token từ localStorage
    if (token) {
      return new HttpHeaders().set('Authorization', `Bearer ${token}`);
    }
    return new HttpHeaders();
  }

  // Phương thức tạo bài đăng mới
  createPost(formData: FormData): Observable<Post> {
    return this.http.post<Post>(this.baseUrl, formData, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  // Phương thức lấy tất cả bài đăng
  // LƯU Ý: Backend sẽ cần logic để lọc bài đăng dựa trên quyền riêng tư và người dùng hiện tại
  getAllPosts(): Observable<Post[]> {
    return this.http.get<Post[]>(this.baseUrl, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  // Phương thức cập nhật bài đăng
  updatePost(postId: string, formData: FormData): Observable<Post> {
    return this.http.put<Post>(`${this.baseUrl}/${postId}`, formData, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  // Phương thức xóa bài đăng
  deletePost(postId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${postId}`, {
      headers: this.getAuthHeaders(),
    }).pipe(
      catchError(this.handleError)
    );
  }

  // Phương thức lấy URL có thể chia sẻ của bài đăng (ví dụ)
  getShareablePostUrl(postId: string): string {
    // Trong ứng dụng thực tế, đây có thể là một URL công khai đến bài đăng
    // mà backend có thể xử lý để hiển thị bài đăng nếu người dùng có quyền.
    return `${window.location.origin}/posts/${postId}`;
  }

  // ĐÃ CẬP NHẬT: Phương thức tìm kiếm người dùng (sẽ gọi API backend)
  // Trả về một đối tượng có 'users' (mảng User) và 'total' (tổng số kết quả)
  searchUsers(query: string, pageIndex: number = 0, pageSize: number = 10): Observable<{ users: User[], total: number }> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', pageIndex.toString())
      .set('size', pageSize.toString());

    return this.http.get<{ users: User[], total: number }>(`${this.userApiUrl}/search`, {
      params: params,
      headers: this.getAuthHeaders()
    }).pipe(
      // delay(500), // Giữ delay để mô phỏng API nếu cần
      catchError(this.handleError)
    );
  }

  // ĐÃ CẬP NHẬT: Phương thức lấy thông tin người dùng từ một danh sách ID (sẽ gọi API backend)
  getUsersByIds(userIds: string[]): Observable<User[]> {
    if (!userIds || userIds.length === 0) {
      return of([]); // Trả về Observable rỗng nếu không có ID
    }
    // Bạn có thể dùng POST nếu danh sách ID quá dài cho GET request (URL length limits)
    return this.http.post<User[]>(`${this.userApiUrl}/by-ids`, { userIds }, { headers: this.getAuthHeaders() }).pipe(
      // delay(200), // Giữ delay để mô phỏng API nếu cần
      catchError(this.handleError)
    );
  }

  private handleError(error: any) {
    console.error('An error occurred:', error);
    // Bạn có thể phân tích lỗi chi tiết hơn ở đây
    let errorMessage = 'Đã xảy ra lỗi không xác định.';
    if (error.error instanceof ErrorEvent) {
      // Lỗi phía client-side hoặc lỗi mạng
      errorMessage = `Lỗi: ${error.error.message}`;
    } else {
      // Lỗi phía backend
      errorMessage = `Mã lỗi: ${error.status}\nThông báo: ${error.message}`;
      if (error.error && error.error.message) {
        errorMessage = error.error.message; // Lấy thông báo lỗi từ backend
      }
    }
    return throwError(() => new Error(errorMessage));
  }
}
