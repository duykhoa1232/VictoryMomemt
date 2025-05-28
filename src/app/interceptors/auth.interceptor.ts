// src/app/core/interceptors/auth.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../auth/services/auth.service'; // Sửa đường dẫn import cho AuthService
import { Router } from '@angular/router'; // Import Router nếu bạn muốn xử lý điều hướng

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router); // Có thể inject Router để điều hướng khi lỗi token
  const token = authService.getToken();

  // Chỉ thêm token nếu nó tồn tại và request không phải là đến endpoint login/register
  // để tránh gửi token cũ khi đang cố gắng đăng nhập lại hoặc đăng ký
  if (token && !req.url.includes('/api/auth/login') && !req.url.includes('/api/auth/register')) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  // Bạn có thể thêm logic xử lý lỗi tại đây nếu muốn, ví dụ bắt lỗi 401 Unauthorized
  // return next(req).pipe(
  //   catchError((error: HttpErrorResponse) => {
  //     if (error.status === 401 && !req.url.includes('/api/auth/login')) {
  //       authService.logout(); // Đăng xuất người dùng
  //       router.navigate(['/login']); // Điều hướng về trang đăng nhập
  //     }
  //     return throwError(() => error);
  //   })
  // );

  return next(req);
};
