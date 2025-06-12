// // src/app/users/user.service.ts (Tạo file này)
// import { Injectable } from '@angular/core';
// import { HttpClient } from '@angular/common/http';
// import { Observable, throwError } from 'rxjs';
// import { catchError } from 'rxjs/operators';
// import {ProfileRequest, ProfileResponse} from '../../shared/models/user.model';
// import {environment} from '../../../environments/environment';
//
// @Injectable({
//   providedIn: 'root'
// })
// export class UserService {
//   private apiUrl = `${environment.apiUrl}/api/users`; // Base URL cho UserController
//
//   constructor(private http: HttpClient) { }
//
//   getUserProfile(): Observable<ProfileResponse> {
//     return this.http.get<ProfileResponse>(`${this.apiUrl}/profile`).pipe(
//       catchError(this.handleError)
//     );
//   }
//
//   updateUserProfile(request: ProfileRequest): Observable<ProfileResponse> {
//     return this.http.put<ProfileResponse>(`${this.apiUrl}/profile`, request).pipe(
//       catchError(this.handleError)
//     );
//   }
//
//   updateUserAvatar(avatarFile: File): Observable<ProfileResponse> {
//     const formData = new FormData();
//     formData.append('avatarFile', avatarFile); // "avatarFile" phải khớp với @RequestParam("avatarFile")
//     return this.http.post<ProfileResponse>(`${this.apiUrl}/profile/avatar`, formData).pipe(
//       catchError(this.handleError)
//     );
//   }
//
//   private handleError(error: any) {
//     console.error('An error occurred in UserService:', error);
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
