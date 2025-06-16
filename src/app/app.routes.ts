import { Routes } from '@angular/router';
import { AuthGuard } from './auth/guards/AuthGuard.service';
import { MainComponent } from './core/layout/main/main.component';
// import { EditProfileComponent } from './auth/profile/edit-profile/edit-profile.component'; // No longer needed directly imported, handled by lazy load

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
        loadComponent: () => import('./core/home/home.component').then(c => c.HomeComponent)
      },
      {
        path: 'profile',
        loadComponent: () => import('./auth/profile/profile/profile.component').then(c => c.ProfileComponent)
      },
      {
        path:'profile-edit', // <-- Đây là đường dẫn bạn đã định nghĩa cho EditProfileComponent
        loadComponent: () => import('./auth/profile/edit-profile/edit-profile.component').then(c => c.EditProfileComponent)
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
