//
//
//
//
// import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
// import { HttpClient } from '@angular/common/http';
// import { Observable, BehaviorSubject, throwError } from 'rxjs';
// import { tap, catchError } from 'rxjs/operators';
// import { Router } from '@angular/router';
// import { jwtDecode } from 'jwt-decode';
// import { MatSnackBar } from '@angular/material/snack-bar';
// import { isPlatformBrowser } from '@angular/common';
//
// import { LoginRequest, RegisterRequest, AuthResponse } from '../../shared/models/auth.model';
// import { environment } from '../../../environments/environment';
//
// @Injectable({
//   providedIn: 'root',
// })
// export class AuthService {
//   private baseUrl = `${environment.apiUrl}/api/auth`;
//   private tokenKey = 'jwt_token';
//
//   private _isLoggedIn = new BehaviorSubject<boolean>(false); // Khởi tạo false, chỉ cập nhật khi kiểm tra token
//   isLoggedIn$ = this._isLoggedIn.asObservable();
//
//   constructor(
//     private http: HttpClient,
//     private router: Router,
//     private snackBar: MatSnackBar,
//     @Inject(PLATFORM_ID) private platformId: Object
//   ) {
//     // Khởi tạo trạng thái đăng nhập khi ở client-side
//     if (isPlatformBrowser(this.platformId)) {
//       this._isLoggedIn.next(this.hasToken());
//     }
//   }
//
//   private hasToken(): boolean {
//     if (isPlatformBrowser(this.platformId)) {
//       const token = localStorage.getItem(this.tokenKey);
//       if (token) {
//         try {
//           const decodedToken: any = jwtDecode(token);
//           const currentTime = Date.now() / 1000; // Thời gian hiện tại (giây)
//           return decodedToken.exp && decodedToken.exp > currentTime;
//         } catch (error) {
//           console.error('Invalid token detected:', error);
//           this.clearToken();
//           return false;
//         }
//       }
//     }
//     return false;
//   }
//
//   private clearToken(): void {
//     if (isPlatformBrowser(this.platformId)) {
//       localStorage.removeItem(this.tokenKey);
//     }
//   }
//
//   login(data: LoginRequest): Observable<AuthResponse> {
//     return this.http.post<AuthResponse>(`${this.baseUrl}/login`, data).pipe(
// tap(response => {
//   if (response.token && isPlatformBrowser(this.platformId)) {
//     localStorage.setItem(this.tokenKey, response.token);
//     this._isLoggedIn.next(true);
//   }
// }),
//   catchError(error => {
//     this.snackBar.open('Đăng nhập thất bại. Vui lòng thử lại.', 'Đóng', {
//       duration: 3000,
//       panelClass: ['error-snackbar']
//     });
//     return throwError(() => error);
//   })
// );
// }
//
// register(data: RegisterRequest): Observable<string> {
//   return this.http.post(`${this.baseUrl}/register`, data, { responseType: 'text' }).pipe(
//     catchError(error => {
//       this.snackBar.open('Đăng ký thất bại. Vui lòng thử lại.', 'Đóng', {
//         duration: 3000,
//         panelClass: ['error-snackbar']
//       });
//       return throwError(() => error);
//     })
//   );
// }
//
// logout(): void {
//   if (isPlatformBrowser(this.platformId)) {
//   this.clearToken();
// }
// this._isLoggedIn.next(false);
// this.router.navigate(['/login']);
// }
//
// getToken(): string | null {
//   if (isPlatformBrowser(this.platformId)) {
//     const token = localStorage.getItem(this.tokenKey);
//     if (token) {
//       try {
//         const decodedToken: any = jwtDecode(token);
//         const currentTime = Date.now() / 1000; // Thời gian hiện tại (giây)
//         if (decodedToken.exp && decodedToken.exp < currentTime) {
//           this.logout(); // Token hết hạn, đăng xuất
//           return null;
//         }
//         return token;
//       } catch (error) {
//         console.error('Token không hợp lệ, xóa token:', error);
//         this.logout();
//         return null;
//       }
//     }
//   }
//   return null;
// }
//
// isUserLoggedIn(): boolean {
//   return this.hasToken();
// }
//
// getCurrentUserId(): string | null {
//   const token = this.getToken();
//   if (token) {
//     try {
//       const decodedToken: any = jwtDecode(token);
//       return decodedToken.userId || decodedToken.sub || null;
//     } catch (error) {
//       return null;
//     }
//   }
//   return null;
// }
//
// getCurrentUserName(): string | null {
//   const token = this.getToken();
//   if (token) {
//     try {
//       const decodedToken: any = jwtDecode(token);
//       return decodedToken.userName || decodedToken.name || (decodedToken.email ? decodedToken.email.split('@')[0] : decodedToken.sub);
//     } catch (error) {
//       return null;
//     }
//   }
//   return null;
// }
//
// getCurrentUserEmail(): string | null {
//   const token = this.getToken();
//   if (token) {
//     try {
//       const decodedToken: any = jwtDecode(token);
//       return decodedToken.email || decodedToken.sub || null; // Giả định email nằm trong token
//     } catch (error) {
//       return null;
//     }
//   }
//   return null;
// }
// }



// src/app/auth/services/auth.service.ts
import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';
import { MatSnackBar } from '@angular/material/snack-bar';
import { isPlatformBrowser } from '@angular/common';

import { LoginRequest, RegisterRequest, AuthResponse, CurrentUser } from '../../shared/models/auth.model'; // IMPORT CurrentUser
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private baseUrl = `${environment.apiUrl}/api/auth`;
  private tokenKey = 'jwt_token';

  private _isLoggedIn = new BehaviorSubject<boolean>(false);
  isLoggedIn$ = this._isLoggedIn.asObservable();

  // BehaviorSubject để lưu trữ thông tin người dùng hiện tại
  private _currentUser = new BehaviorSubject<CurrentUser | null>(null);
  currentUser$ = this._currentUser.asObservable(); // Observable để các component khác có thể subscribe

  // Thuộc tính getter để truy cập giá trị hiện tại (dùng cho HTML)
  get currentUser(): CurrentUser | null {
    return this._currentUser.value;
  }

  constructor(
    private http: HttpClient,
    private router: Router,
    private snackBar: MatSnackBar,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    // Khởi tạo trạng thái đăng nhập và thông tin người dùng khi dịch vụ được tạo
    if (isPlatformBrowser(this.platformId)) {
      this.initializeAuthStatus();
    }
  }

  private initializeAuthStatus(): void {
    const token = localStorage.getItem(this.tokenKey);
    if (token) {
      try {
        const decodedToken: any = jwtDecode(token);
        const currentTime = Date.now() / 1000;

        if (decodedToken.exp && decodedToken.exp > currentTime) {
          // Token còn hạn, cập nhật trạng thái đăng nhập và thông tin người dùng
          this.decodeAndSetCurrentUser(token); // Cập nhật currentUser
          this._isLoggedIn.next(true);
        } else {
          // Token hết hạn
          console.warn('Token expired during initialization, logging out.');
          this.clearAuthData(); // Xóa token và đặt lại trạng thái
        }
      } catch (error) {
        console.error('Invalid token detected during initialization:', error);
        this.clearAuthData(); // Xóa token và đặt lại trạng thái
      }
    } else {
      this.clearAuthData(); // Đảm bảo trạng thái sạch nếu không có token
    }
  }

  // Hàm tiện ích để giải mã JWT và đặt thông tin người dùng
  private decodeAndSetCurrentUser(token: string): void {
    try {
      const decodedToken: any = jwtDecode(token);
      const user: CurrentUser = {
        id: decodedToken.userId || decodedToken.sub, // Lấy ID người dùng (sub là subject trong JWT)
        email: decodedToken.email || decodedToken.sub, // Lấy email
        name: decodedToken.userName || decodedToken.name || (decodedToken.email ? decodedToken.email.split('@')[0] : decodedToken.sub),
        avatarUrl: decodedToken.avatarUrl || null, // Thêm trường avatarUrl nếu có trong token payload
        // Bạn có thể thêm các trường khác nếu JWT của bạn chứa chúng
      };
      this._currentUser.next(user);
    } catch (error) {
      console.error('Error decoding user info from token:', error);
      this._currentUser.next(null); // Đảm bảo currentUser là null nếu có lỗi giải mã
    }
  }

  // Hàm tiện ích để xóa tất cả dữ liệu auth và reset trạng thái
  private clearAuthData(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(this.tokenKey);
    }
    this._isLoggedIn.next(false);
    this._currentUser.next(null);
  }

  login(data: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, data).pipe(
      tap(response => {
        if (response.token && isPlatformBrowser(this.platformId)) {
          localStorage.setItem(this.tokenKey, response.token);
          this.decodeAndSetCurrentUser(response.token); // Cập nhật currentUser sau khi đăng nhập thành công
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
    this.clearAuthData(); // Sử dụng hàm tiện ích
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem(this.tokenKey);
      if (token) {
        try {
          const decodedToken: any = jwtDecode(token);
          const currentTime = Date.now() / 1000;
          if (decodedToken.exp && decodedToken.exp < currentTime) {
            console.warn('Token hết hạn, tự động đăng xuất từ getToken().');
            this.logout(); // Token hết hạn, đăng xuất
            return null;
          }
          return token;
        } catch (error) {
          console.error('Token không hợp lệ trong getToken(), xóa token:', error);
          this.logout(); // Token không hợp lệ, đăng xuất
          return null;
        }
      }
    }
    return null;
  }

  isUserLoggedIn(): boolean {
    return this._isLoggedIn.value; // Trả về giá trị hiện tại của BehaviorSubject
  }

  // Các phương thức lấy thông tin người dùng từ _currentUser
  getCurrentUserId(): string | null {
    return this._currentUser.value?.id || null;
  }

  getCurrentUserName(): string | null {
    return this._currentUser.value?.name || null;
  }

  getCurrentUserEmail(): string | null {
    return this._currentUser.value?.email || null;
  }
  updateCurrentUserAvatar(newAvatarUrl: string | null): void {
    const currentUser = this._currentUser.value;
    if (currentUser) {
      this._currentUser.next({ ...currentUser, avatarUrl: newAvatarUrl });
    }
  }

}
