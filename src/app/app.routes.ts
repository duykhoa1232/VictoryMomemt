
import { Routes } from '@angular/router';
import { AuthGuard } from './auth/guards/AuthGuard.service';
import { MainComponent } from './core/layout/main/main.component';

export const routes: Routes = [
  // Route mặc định, chuyển hướng đến home
  { path: '', redirectTo: 'home', pathMatch: 'full' },

  // Lazy load các component liên quan đến Auth
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
  // {
  //   path: 'profile',
  //   loadComponent: () => import('./auth/profile/profile/profile.component').then(c => c.ProfileComponent),
  //   canActivate: [AuthGuard]
  // },

  // Lazy load layout chính và các route con (không cần AuthGuard)
  {
    path: '',
    component: MainComponent,
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      {
        path: 'home',
        loadComponent: () => import('./core/home/home.component').then(c => c.HomeComponent)
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

  // Route wildcard cho các đường dẫn không khớp
  { path: '**', redirectTo: 'home' }
];
