// src/app/auth/services/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';

// Import các interfaces từ file models riêng
import { LoginRequest, RegisterRequest, AuthResponse } from '../../shared/models/auth.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  // private baseUrl = 'http://localhost:8080/api/auth'; // Sửa thành dùng environment
  private baseUrl = `${environment.apiUrl}/api/auth`;
  private tokenKey = 'jwt_token';

  private _isLoggedIn = new BehaviorSubject<boolean>(this.hasToken());
  isLoggedIn$ = this._isLoggedIn.asObservable();

  constructor(private http: HttpClient, private router: Router) { }

  private hasToken(): boolean {
    return !!localStorage.getItem(this.tokenKey);
  }

  login(data: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, data).pipe(
      tap(response => {
        if (response.token) {
          localStorage.setItem(this.tokenKey, response.token);
          this._isLoggedIn.next(true);
          console.log('Đăng nhập thành công, token đã được lưu.');
        }
      }),
      catchError(error => {
        console.error('Đăng nhập thất bại trong AuthService:', error);
        return throwError(() => error);
      })
    );
  }

  register(data: RegisterRequest): Observable<string> {
    return this.http.post(`${this.baseUrl}/register`, data, { responseType: 'text' }).pipe(
      catchError(error => {
        console.error('Đăng ký thất bại trong AuthService:', error);
        return throwError(() => error);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this._isLoggedIn.next(false);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isUserLoggedIn(): boolean {
    return this.hasToken();
  }

  getCurrentUserId(): string | null {
    const token = this.getToken();
    if (token) {
      try {
        const decodedToken: any = jwtDecode(token);
        // Giả sử userId được lưu trong payload của token dưới key 'userId' hoặc 'sub'
        // Kiểm tra đúng key mà BE của bạn dùng để mã hóa ID người dùng vào token
        return decodedToken.userId || decodedToken.sub || null;
      } catch (error) {
        console.error('Error decoding token for userId:', error);
        return null;
      }
    }
    return null;
  }

  getCurrentUserName(): string | null {
    const token = this.getToken();
    if (token) {
      try {
        const decodedToken: any = jwtDecode(token);
        // Giả sử tên người dùng được lưu trong payload của token dưới key 'userName', 'name', 'email' hoặc 'sub'
        // Kiểm tra đúng key mà BE của bạn dùng để mã hóa tên người dùng vào token
        return decodedToken.userName || decodedToken.name || (decodedToken.email ? decodedToken.email : decodedToken.sub);
      } catch (error) {
        console.error('Error decoding token for userName:', error);
        return null;
      }
    }
    return null;
  }
}
