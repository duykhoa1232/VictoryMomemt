// import { Injectable } from '@angular/core';
// import { HttpClient } from '@angular/common/http';
// import { Observable, throwError } from 'rxjs';
// import { catchError } from 'rxjs/operators';
// import { CommentRequest, CommentResponse } from '../../shared/models/comment.model';
// import { environment } from '../../../environments/environment';
//
// @Injectable({
//   providedIn: 'root'
// })
// export class CommentService {
//   private baseUrl = `${environment.apiUrl}/api/posts`; // Base URL cho CommentController
//
//   constructor(private http: HttpClient) { }
//
//   createComment(postId: string, request: CommentRequest): Observable<CommentResponse> {
//     return this.http.post<CommentResponse>(`${this.baseUrl}/${postId}/comments`, request).pipe(
//       catchError(this.handleError)
//     );
//   }
//
//   getCommentsByPostId(postId: string): Observable<CommentResponse[]> {
//     return this.http.get<CommentResponse[]>(`${this.baseUrl}/${postId}/comments`).pipe(
//       catchError(this.handleError)
//     );
//   }
//
//   updateComment(postId: string, commentId: string, request: CommentRequest): Observable<CommentResponse> {
//     return this.http.put<CommentResponse>(`${this.baseUrl}/${postId}/comments/${commentId}`, request).pipe(
//       catchError(this.handleError)
//     );
//   }
//
//   deleteComment(postId: string, commentId: string): Observable<void> {
//     return this.http.delete<void>(`${this.baseUrl}/${postId}/comments/${commentId}`).pipe(
//       catchError(this.handleError)
//     );
//   }
//
//   private handleError(error: any) {
//     console.error('An error occurred in CommentService:', error);
//     let errorMessage = 'Đã xảy ra lỗi không xác định.';
//     if (error.error instanceof ErrorEvent) {
//       errorMessage = `Lỗi phía client: ${error.error.message}`;
//     } else {
//       errorMessage = `Lỗi từ server: ${error.status} - ${error.error || error.message}`;
//       if (error.error && typeof error.error === 'string') {
//         errorMessage = error.error;
//       } else if (error.error && error.error.message) {
//         errorMessage = error.error.message;
//       }
//     }
//     return throwError(() => new Error(errorMessage));
//   }
// }


import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { CommentRequest, CommentResponse } from '../../../shared/models/comment.model';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private baseUrl = `${environment.apiUrl}/api/posts`; // Base URL cho CommentController

  constructor(private http: HttpClient) { }

  createComment(postId: string, request: CommentRequest): Observable<CommentResponse> {
    console.log('Creating comment for postId:', postId, 'with request:', request);
    return this.http.post<CommentResponse>(`${this.baseUrl}/${postId}/comments`, request).pipe(
      tap(response => console.log('API createComment response:', response)),
      catchError(this.handleError)
    );
  }

  getCommentsByPostId(postId: string): Observable<CommentResponse[]> {
    console.log('Calling API getCommentsByPostId for postId:', postId);
    return this.http.get<CommentResponse[]>(`${this.baseUrl}/${postId}/comments`).pipe(
      tap(response => console.log('API getCommentsByPostId response:', response)),
      catchError(this.handleError)
    );
  }

  updateComment(postId: string, commentId: string, request: CommentRequest): Observable<CommentResponse> {
    console.log('Updating comment for postId:', postId, 'commentId:', commentId, 'with request:', request);
    return this.http.put<CommentResponse>(`${this.baseUrl}/${postId}/comments/${commentId}`, request).pipe(
      tap(response => console.log('API updateComment response:', response)),
      catchError(this.handleError)
    );
  }

  deleteComment(postId: string, commentId: string): Observable<void> {
    console.log('Deleting comment for postId:', postId, 'commentId:', commentId);
    return this.http.delete<void>(`${this.baseUrl}/${postId}/comments/${commentId}`).pipe(
      tap(() => console.log('API deleteComment successful for commentId:', commentId)),
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
