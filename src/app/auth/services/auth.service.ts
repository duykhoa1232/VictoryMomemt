// src/app/auth/services/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  name: string;
  email: string;
  phoneNumber: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  // Thêm các trường khác từ phản hồi API nếu có, ví dụ: userId, username
  // userId?: string;
  // username?: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private baseUrl = 'http://localhost:8080/api/auth';
  private tokenKey = 'jwt_token'; // Khóa lưu trữ token trong localStorage

  // BehaviorSubject để theo dõi trạng thái đăng nhập của người dùng
  private _isLoggedIn = new BehaviorSubject<boolean>(this.hasToken());
  isLoggedIn$ = this._isLoggedIn.asObservable(); // Observable để các component subscribe

  constructor(private http: HttpClient, private router: Router) {
    // Khi service được khởi tạo, kiểm tra xem có token trong localStorage không
    // và cập nhật trạng thái _isLoggedIn
    // if (this.hasToken()) {
    //   // Nếu cần, bạn có thể giải mã token ở đây để lấy thông tin user
    //   // hoặc gọi API để lấy profile người dùng
    // }
  }

  // Phương thức kiểm tra xem có token trong localStorage không
  private hasToken(): boolean {
    return !!localStorage.getItem(this.tokenKey);
  }

  // Xử lý đăng nhập
  login(data: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, data).pipe(
      tap(response => {
        if (response.token) {
          localStorage.setItem(this.tokenKey, response.token); // Lưu token
          this._isLoggedIn.next(true); // Cập nhật trạng thái đăng nhập
          console.log('Đăng nhập thành công, token đã được lưu.');
          // Nếu có thêm thông tin user trong response, bạn có thể lưu chúng ở đây
          // Ví dụ: localStorage.setItem('user_info', JSON.stringify(response.user));
        }
      }),
      catchError(error => {
        console.error('Đăng nhập thất bại trong AuthService:', error);
        // Ném lỗi để component gọi có thể xử lý
        return throwError(() => error);
      })
    );
  }

  // Xử lý đăng ký
  register(data: RegisterRequest): Observable<string> {
    return this.http.post(`${this.baseUrl}/register`, data, { responseType: 'text' }).pipe(
      catchError(error => {
        console.error('Đăng ký thất bại trong AuthService:', error);
        return throwError(() => error);
      })
    );
  }

  // Xử lý đăng xuất
  logout(): void {
    localStorage.removeItem(this.tokenKey); // Xóa token
    // Nếu bạn lưu thêm thông tin user, hãy xóa chúng ở đây
    // localStorage.removeItem('user_info');
    this._isLoggedIn.next(false); // Cập nhật trạng thái đăng nhập
    this.router.navigate(['/login']); // Điều hướng về trang đăng nhập
  }

  // Lấy token
  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  // Kiểm tra trạng thái đăng nhập (được sử dụng chủ yếu bởi Guards)
  isUserLoggedIn(): boolean {
    return this.hasToken();
  }
}
