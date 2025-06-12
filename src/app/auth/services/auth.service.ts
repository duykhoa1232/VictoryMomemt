import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';
import { MatSnackBar } from '@angular/material/snack-bar';
import { isPlatformBrowser } from '@angular/common';

import { LoginRequest, RegisterRequest, AuthResponse } from '../../shared/models/auth.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private baseUrl = `${environment.apiUrl}/api/auth`;
  private tokenKey = 'jwt_token';

  private _isLoggedIn = new BehaviorSubject<boolean>(this.hasToken());
  isLoggedIn$ = this._isLoggedIn.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router,
    private snackBar: MatSnackBar,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    // Khởi tạo _isLoggedIn chỉ khi ở client-side
    if (isPlatformBrowser(this.platformId)) {
      this._isLoggedIn = new BehaviorSubject<boolean>(this.hasToken());
      this.isLoggedIn$ = this._isLoggedIn.asObservable();
    }
  }

  private hasToken(): boolean {
    if (isPlatformBrowser(this.platformId)) {
      return !!localStorage.getItem(this.tokenKey);
    }
    return false; // Trả về false nếu ở server-side
  }

  login(data: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, data).pipe(
      tap(response => {
        if (response.token && isPlatformBrowser(this.platformId)) {
          localStorage.setItem(this.tokenKey, response.token);
          this._isLoggedIn.next(true);
        }
      }),
      catchError(error => {
        this.snackBar.open('Đăng nhập thất bại. Vui lòng thử lại.', 'Đóng', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
        return throwError(() => error);
      })
    );
  }

  register(data: RegisterRequest): Observable<string> {
    return this.http.post(`${this.baseUrl}/register`, data, { responseType: 'text' }).pipe(
      catchError(error => {
        this.snackBar.open('Đăng ký thất bại. Vui lòng thử lại.', 'Đóng', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
        return throwError(() => error);
      })
    );
  }

  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(this.tokenKey);
    }
    this._isLoggedIn.next(false);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem(this.tokenKey);
      if (token) {
        try {
          const decodedToken: any = jwtDecode(token);
          const currentTime = Date.now() / 1000; // Thời gian hiện tại (giây)
          if (decodedToken.exp && decodedToken.exp < currentTime) {
            this.logout(); // Token hết hạn, đăng xuất
            return null;
          }
          return token;
        } catch (error) {
          console.error('Token không hợp lệ, xóa token:', error);
          this.logout();
          return null;
        }
      }
    }
    return null;
  }

  isUserLoggedIn(): boolean {
    return this.hasToken();
  }

  getCurrentUserId(): string | null {
    const token = this.getToken();
    if (token) {
      try {
        const decodedToken: any = jwtDecode(token);
        return decodedToken.userId || decodedToken.sub || null;
      } catch (error) {
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
        return decodedToken.userName || decodedToken.name || (decodedToken.email ? decodedToken.email : decodedToken.sub);
      } catch (error) {
        return null;
      }
    }
    return null;
  }
}
