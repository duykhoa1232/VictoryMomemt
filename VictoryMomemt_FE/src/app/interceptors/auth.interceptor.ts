// src/app/interceptors/auth.interceptor.ts
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http'; // Thêm HttpErrorResponse
import { inject } from '@angular/core';
import { AuthService } from '../auth/services/auth.service';
import { Router } from '@angular/router';
import { catchError } from 'rxjs/operators'; // Thêm catchError
import { throwError } from 'rxjs'; // Thêm throwError

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();

  // Chỉ thêm token nếu nó tồn tại và request không phải là đến endpoint login/register
  if (token && !req.url.includes('/api/auth/login') && !req.url.includes('/api/auth/register')) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Xử lý lỗi 401 Unauthorized
      if (error.status === 401 && !req.url.includes('/api/auth/login')) {
        console.warn('Unauthorized request, logging out...');
        authService.logout(); // Đăng xuất người dùng
        router.navigate(['/login']); // Điều hướng về trang đăng nhập
      }
      return throwError(() => error); // Ném lại lỗi để các handler khác có thể xử lý
    })
  );
};
