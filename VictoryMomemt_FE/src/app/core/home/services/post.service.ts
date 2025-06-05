// src/app/core/home/services/post.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { delay, map, catchError } from 'rxjs/operators';

// THAY ĐỔI CÁC DÒNG IMPORT DƯỚI ĐÂY
// Từ '../../shared/models/post.model' thành '../../../shared/models/post.model'
import { PostRequest, PostResponse } from '../../../shared/models/post.model';
import { UserResponse } from '../../../shared/models/user.model';
// Từ '../../../../environments/environment' thành '../../../environments/environment'
// (Vì environment.ts cũng ở cấp src/environments, tức là từ post.service.ts lên app, rồi xuống environments)
import { environment } from '../../../../environments/environment'; // Đường dẫn này đã đúng rồi, nhưng kiểm tra lại cho chắc

@Injectable({
  providedIn: 'root',
})
export class PostService {
  private baseUrl = `${environment.apiUrl}/api/posts`;

  constructor(private http: HttpClient) {}

  createPost(formData: FormData): Observable<PostResponse> {
    return this.http.post<PostResponse>(this.baseUrl, formData).pipe(
      catchError(this.handleError)
    );
  }

  getAllPosts(): Observable<PostResponse[]> {
    return this.http.get<PostResponse[]>(this.baseUrl).pipe(
      catchError(this.handleError)
    );
  }

  getPostById(id: string): Observable<PostResponse> {
    return this.http.get<PostResponse>(`${this.baseUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  getMyPosts(): Observable<PostResponse[]> {
    return this.http.get<PostResponse[]>(`${this.baseUrl}/my-posts`).pipe(
      catchError(this.handleError)
    );
  }

  updatePost(postId: string, formData: FormData): Observable<PostResponse> {
    return this.http.put<PostResponse>(`${this.baseUrl}/${postId}`, formData).pipe(
      catchError(this.handleError)
    );
  }

  deletePost(postId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${postId}`).pipe(
      catchError(this.handleError)
    );
  }

  toggleLike(postId: string): Observable<PostResponse> {
    return this.http.post<PostResponse>(`${this.baseUrl}/${postId}/like`, {}).pipe(
      catchError(this.handleError)
    );
  }

  searchUsers(query: string): Observable<UserResponse[]> {
    const params = new HttpParams().set('query', query);
    return this.http.get<UserResponse[]>(`${this.baseUrl}/users/search`, { params }).pipe(
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
