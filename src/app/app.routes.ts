// src/app/app.routes.ts
import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { LogoutComponent } from './auth/logout/logout.component'; // Nếu có logout component riêng
import { HomeComponent } from './core/home/home.component';
import {AuthGuard} from './auth/guards/AuthGuard.service';
import {MainComponent} from './core/layout/main/main.component'; // Import HomeComponent

export const routes: Routes = [
  {path: '', redirectTo: 'login', pathMatch: 'full'},
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'logout', component: LogoutComponent, canActivate: [AuthGuard] }, // Ví dụ với Guard

  // Đây là route cho layout chính
  {
    path: '', // Route rỗng, có thể là base path của ứng dụng
    component: MainComponent, // Component layout chính
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' }, // Chuyển hướng đến home mặc định
      { path: 'home', component: HomeComponent }, // Trang chủ hiển thị nội dung
      { path: 'daily-podcast', component: HomeComponent }, // Hoặc component riêng cho Daily Podcast
      { path: 'expert-interviews', component: HomeComponent }, // Hoặc component riêng
      { path: 'brian-ford', component: HomeComponent }, // Hoặc component riêng
      { path: 'change-your-life', component: HomeComponent }, // Hoặc component riêng

      // Các route khác yêu cầu đăng nhập sẽ nằm ở đây và được bảo vệ bởi AuthGuard
      // ... thêm các route khác của ứng dụng bạn
    ]
  },

  // Route wildcard cho các đường dẫn không khớp, chuyển hướng về login nếu chưa đăng nhập
  { path: '**', redirectTo: 'login' }
];
