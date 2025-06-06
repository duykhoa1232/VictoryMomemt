// src/app/auth/guards/auth.guard.ts (Đổi tên file và class)
import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs'; // Thêm Observable
import { AuthService } from '../services/auth.service'; // Đảm bảo đường dẫn đúng

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate { // Đổi tên class từ RouteGuardService
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    if (this.authService.isUserLoggedIn()) { // Gọi isUserLoggedIn từ AuthService
      return true; // Cho phép truy cập nếu đã đăng nhập
    } else {
      // Nếu chưa đăng nhập, điều hướng đến trang đăng nhập và trả về false
      this.router.navigate(['/login']);
      return false;
    }
  }
}
