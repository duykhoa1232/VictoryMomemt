// src/app/shared/services/comment.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CommentRequest, CommentResponse } from '../../../shared/models/comment.model';
import { AuthService } from '../../../auth/services/auth.service';

@Injectable({
  providedIn: 'root',
})
export class CommentService {
  private baseUrl = 'http://localhost:8080/api'; // ĐẢM BẢO ĐÚNG VỚI CỔNG BACKEND CỦA BẠN

  constructor(private http: HttpClient, private authService: AuthService) {}

  createComment(postId: string, request: CommentRequest): Observable<CommentResponse> {
    const userEmail = this.authService.getCurrentUserEmail();
    if (!userEmail) {
      throw new Error('User email not found. Authentication required.');
    }
    return this.http.post<CommentResponse>(`${this.baseUrl}/posts/${postId}/comments?userEmail=${userEmail}`, request);
  }

  getCommentsByPostId(postId: string): Observable<CommentResponse[]> {
    return this.http.get<CommentResponse[]>(`${this.baseUrl}/posts/${postId}/comments`);
  }

  updateComment(commentId: string, request: CommentRequest): Observable<CommentResponse> {
    const userEmail = this.authService.getCurrentUserEmail();
    if (!userEmail) {
      throw new Error('User email not found. Authentication required.');
    }
    return this.http.put<CommentResponse>(`${this.baseUrl}/comments/${commentId}?userEmail=${userEmail}`, request);
  }

  deleteComment(commentId: string): Observable<void> {
    const userEmail = this.authService.getCurrentUserEmail();
    if (!userEmail) {
      throw new Error('User email not found. Authentication required.');
    }
    return this.http.delete<void>(`${this.baseUrl}/comments/${commentId}?userEmail=${userEmail}`);
  }
}
