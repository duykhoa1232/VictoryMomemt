//
// import {CommentRequest, CommentResponse} from '../../../shared/models/comment.model';
// import {AuthService} from '../../../auth/services/auth.service';
// import {environment} from '../../../../environments/environment';
// import { Injectable } from '@angular/core';
// import { HttpClient, HttpHeaders } from '@angular/common/http';
// import { Observable } from 'rxjs';
//
//
// @Injectable({
//   providedIn: 'root'
// })
// export class CommentService {
//   private apiUrl = `${environment.apiUrl}/api/posts`; // Đảm bảo khớp với backend
//
//   constructor(private http: HttpClient, private authService: AuthService) {}
//
//   private getHeaders(): HttpHeaders {
//     const token = this.authService.getToken();
//     if (!token) {
//       console.warn('No token found, authentication may fail');
//     }
//     return new HttpHeaders().set('Authorization', `Bearer ${token}`);
//   }
//
//   createComment(postId: string, request: CommentRequest): Observable<CommentResponse> {
//     return this.http.post<CommentResponse>(`${this.apiUrl}/${postId}/comments`, request, { headers: this.getHeaders() });
//   }
//
//   getCommentsByPostId(postId: string): Observable<CommentResponse[]> {
//     return this.http.get<CommentResponse[]>(`${this.apiUrl}/${postId}/comments`, { headers: this.getHeaders() });
//   }
//
//   updateComment(postId: string, commentId: string, request: CommentRequest): Observable<CommentResponse> {
//     return this.http.put<CommentResponse>(`${this.apiUrl}/${postId}/comments/${commentId}`, request, { headers: this.getHeaders() });
//   }
//
//   deleteComment(postId: string, commentId: string): Observable<void> {
//     return this.http.delete<void>(`${this.apiUrl}/${postId}/comments/${commentId}`, { headers: this.getHeaders() });
//   }
// }



//
// import { Injectable } from '@angular/core';
// import { HttpClient, HttpHeaders } from '@angular/common/http';
// import { Observable } from 'rxjs';
// import { CommentRequest, CommentResponse } from '../../../shared/models/comment.model';
// import { AuthService } from '../../../auth/services/auth.service';
// import { environment } from '../../../../environments/environment';
//
// @Injectable({
//   providedIn: 'root'
// })
// export class CommentService {
//   private apiUrl = `${environment.apiUrl}/api/posts`;
//
//   constructor(private http: HttpClient, private authService: AuthService) {}
//
//   private getHeaders(): HttpHeaders {
//     const token = this.authService.getToken();
//     if (!token) {
//       console.warn('No token found, authentication may fail');
//     }
//     return new HttpHeaders().set('Authorization', `Bearer ${token}`);
//   }
//
//   createComment(postId: string, request: CommentRequest): Observable<CommentResponse> {
//     return this.http.post<CommentResponse>(`${this.apiUrl}/${postId}/comments`, request, { headers: this.getHeaders() });
//   }
//
//   getCommentsByPostId(postId: string): Observable<CommentResponse[]> {
//     return this.http.get<CommentResponse[]>(`${this.apiUrl}/${postId}/comments`, { headers: this.getHeaders() });
//   }
//
//   updateComment(postId: string, commentId: string, request: CommentRequest): Observable<CommentResponse> {
//     return this.http.put<CommentResponse>(`${this.apiUrl}/${postId}/comments/${commentId}`, request, { headers: this.getHeaders() });
//   }
//
//   deleteComment(postId: string, commentId: string): Observable<void> {
//     return this.http.delete<void>(`${this.apiUrl}/${postId}/comments/${commentId}`, { headers: this.getHeaders() });
//   }
// }
