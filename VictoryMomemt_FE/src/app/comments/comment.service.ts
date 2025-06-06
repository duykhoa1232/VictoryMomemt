// src/app/comments/comment.service.ts (Tạo file này)
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CommentRequest, CommentResponse } from '../shared/models/comment.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private baseUrl = `${environment.apiUrl}/api/posts`; // Base URL cho CommentController

  constructor(private http: HttpClient) { }

  createComment(postId: string, request: CommentRequest): Observable<CommentResponse> {
    return this.http.post<CommentResponse>(`${this.baseUrl}/${postId}/comments`, request).pipe(
      catchError(this.handleError)
    );
  }

  getCommentsByPostId(postId: string): Observable<CommentResponse[]> {
    return this.http.get<CommentResponse[]>(`${this.baseUrl}/${postId}/comments`).pipe(
      catchError(this.handleError)
    );
  }

  updateComment(postId: string, commentId: string, request: CommentRequest): Observable<CommentResponse> {
    // Controller của bạn chỉ có @PathVariable commentId, không có postId trong updateComment
    // Cần kiểm tra lại nếu muốn có cả postId ở URL
    return this.http.put<CommentResponse>(`${this.baseUrl}/${postId}/comments/${commentId}`, request).pipe(
      catchError(this.handleError)
    );
  }

  deleteComment(postId: string, commentId: string): Observable<void> {
    // Controller của bạn chỉ có @PathVariable commentId, không có postId trong deleteComment
    // Cần kiểm tra lại nếu muốn có cả postId ở URL
    return this.http.delete<void>(`${this.baseUrl}/${postId}/comments/${commentId}`).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: any) {
    console.error('An error occurred in CommentService:', error);
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
