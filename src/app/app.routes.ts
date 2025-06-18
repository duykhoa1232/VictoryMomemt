
// src/app/app.routes.ts
import { Routes } from '@angular/router';
import { AuthGuard } from './auth/guards/AuthGuard.service'; // Đảm bảo đường dẫn này đúng
import { MainComponent } from './core/layout/main/main.component';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./auth/login/login.component').then(c => c.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./auth/register/register.component').then(c => c.RegisterComponent)
  },
  {
    path: 'logout',
    loadComponent: () => import('./auth/logout/logout.component').then(c => c.LogoutComponent),
    canActivate: [AuthGuard]
  },
  {
    path: '', // Main layout for authenticated users (or public content within layout)
    component: MainComponent,
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      {
        path: 'home',
        loadComponent: () => import('./core/home/home.component').then(c => c.HomeComponent),
        canActivate: [AuthGuard] // Giả định home cần đăng nhập
      },
      {
        path: 'profile', // Profile của người dùng hiện tại
        loadComponent: () => import('./auth/profile/profile/profile.component').then(c => c.ProfileComponent),
        canActivate: [AuthGuard]
      },
      {
        // ROUTE MỚI: Profile của người dùng khác, sử dụng cùng ProfileComponent
        // Tham số ':userEmail' sẽ được ProfileComponent đọc từ ActivatedRoute
        path: 'users/:userEmail/profile',
        loadComponent: () => import('./auth/profile/profile/profile.component').then(c => c.ProfileComponent),
        canActivate: [AuthGuard] // Có thể đổi thành không cần auth nếu profile public
      },
      {
        path:'profile-edit', // Đường dẫn cho EditProfileComponent
        loadComponent: () => import('./auth/profile/edit-profile/edit-profile.component').then(c => c.EditProfileComponent),
        canActivate: [AuthGuard]
      },
      {
        path: 'daily-podcast',
        loadComponent: () => import('./core/home/home.component').then(c => c.HomeComponent)
      },
      {
        path: 'expert-interviews',
        loadComponent: () => import('./core/home/home.component').then(c => c.HomeComponent)
      },
      {
        path: 'brian-ford',
        loadComponent: () => import('./core/home/home.component').then(c => c.HomeComponent)
      },
      {
        path: 'change-your-life',
        loadComponent: () => import('./core/home/home.component').then(c => c.HomeComponent)
      },
    ]
  },
  { path: '**', redirectTo: 'home' } // Wildcard route
];
