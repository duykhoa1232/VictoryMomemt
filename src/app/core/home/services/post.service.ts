



// src/app/core/home/services/post.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { delay, map, catchError } from 'rxjs/operators';

// Đảm bảo import Page từ đúng file model của bạn
import { PostRequest, PostResponse, Page } from '../../../shared/models/post.model';
import { UserResponse } from '../../../shared/models/profile.model';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PostService {
  private baseUrl = `${environment.apiUrl}/api/posts`;

  constructor(private http: HttpClient) { }

  createPost(formData: FormData): Observable<PostResponse> {
    return this.http.post<PostResponse>(this.baseUrl, formData).pipe(
      catchError(this.handleError)
    );
  }

  // SỬA ĐỔI QUAN TRỌNG: THAY ĐỔI CHỮ KÝ VÀ KIỂU TRẢ VỀ CỦA getAllPosts
  getAllPosts(page: number, size: number, sort?: string): Observable<Page<PostResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (sort) {
      params = params.set('sort', sort);
    }
    // Đảm bảo http.get cũng được gõ kiểu Page<PostResponse>
    return this.http.get<Page<PostResponse>>(this.baseUrl, { params }).pipe(
      catchError(this.handleError)
    );
  }

  getPostById(id: string): Observable<PostResponse> {
    return this.http.get<PostResponse>(`${this.baseUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  // TÙY CHỌN: Nếu bạn muốn phân trang cho "My Posts", sửa tương tự getAllPosts
  getMyPosts(page: number, size: number, sort?: string): Observable<Page<PostResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (sort) {
      params = params.set('sort', sort);
    }
    return this.http.get<Page<PostResponse>>(`${this.baseUrl}/my-posts`, { params }).pipe(
      catchError(this.handleError)
    );
  }

  updatePost(postId: string, formData: FormData): Observable<PostResponse> {
    return this.http.put<PostResponse>(`${this.baseUrl}/${postId}`, formData).pipe(
      catchError(this.handleError)
    );
  }

  deletePost(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  toggleLike(id: string): Observable<PostResponse> {
    return this.http.post<PostResponse>(`${this.baseUrl}/${id}/like`, {}).pipe(
      catchError(this.handleError)
    );
  }
  addComment(postId: string, content: string): Observable<PostResponse> {
    const body = {content};
    return this.http.post<PostResponse>(`${this.baseUrl}/${postId}/comments`, body).pipe(catchError(this.handleError));  }

  searchUsers(query: string): Observable<UserResponse[]> {
    const params = new HttpParams().set('query', query);
    // Sửa đường dẫn nếu cần: Đảm bảo endpoint này đúng với cấu hình backend của bạn
    // (ví dụ: nếu nó nằm ở /api/users/search thì không cần /api/posts/)
    return this.http.get<UserResponse[]>(`${environment.apiUrl}/api/users/search`, { params }).pipe(
      catchError(this.handleError)
    );
  }

  getShareablePostUrl(postId: string): string {
    return `${window.location.origin}/posts/${postId}`;
  }

  private handleError(error: any) {
    console.error('An error occurred:', error);
    let errorMessage = 'Đã xảy ra lỗi không xác định.';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Lỗi phía client: ${error.error.message}`;
    } else {
      errorMessage = `Lỗi từ server: ${error.status} - ${error.error || error.message}`;
      if (error.error && typeof error.error === 'string') {
        errorMessage = error.error;
      } else if (error.error && error.error.message) {
        errorMessage = error.error.message;
      }
    }
    return throwError(() => new Error(errorMessage));
  }
}




