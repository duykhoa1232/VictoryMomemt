



// src/app/auth/services/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode'; // <<< THÊM DÒNG NÀY

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  phoneNumber: string;
  password: string;
}

export interface AuthResponse {
  token: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private baseUrl = 'http://localhost:8080/api/auth';
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

  // <<< BỔ SUNG CÁC PHƯƠNG THỨC NÀY ĐỂ LẤY USER ID VÀ USER NAME TỪ TOKEN
  getCurrentUserId(): string | null {
    const token = this.getToken();
    if (token) {
      try {
        const decodedToken: any = jwtDecode(token);
        // Giả sử userId được lưu trong payload của token dưới key 'userId' hoặc 'sub'
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
        // Giả sử tên người dùng được lưu trong payload của token dưới key 'userName' hoặc 'name'
        // Hoặc lấy từ email nếu bạn lưu email trong token và muốn hiển thị phần trước @
        return decodedToken.userName || decodedToken.name || (decodedToken.email ? decodedToken.email.split('@')[0] : null);
      } catch (error) {
        console.error('Error decoding token for userName:', error);
        return null;
      }
    }
    return null;
  }
  // <<< KẾT THÚC CÁC PHƯƠNG THỨC BỔ SUNG
}
