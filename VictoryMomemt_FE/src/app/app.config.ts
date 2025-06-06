// src/app/app.config.ts
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';

import { routes } from './app.routes';
import { provideClientHydration } from '@angular/platform-browser';
import { provideAnimations } from '@angular/platform-browser/animations';

import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { AuthService } from './auth/services/auth.service';
import { AuthGuard } from './auth/guards/AuthGuard.service';
import {authInterceptor} from './interceptors/auth.interceptor';

// !!! THÊM DÒNG IMPORT NÀY VÀ ĐẢM BẢO TÊN BIẾN KHỚP VỚI TÊN EXPORT TỪ INTERCEPTOR FILE !!!
// Nếu interceptor được export là `export const authInterceptor` (viết thường),
// thì import cũng phải là `{ authInterceptor }`.
// Dựa vào code bạn cung cấp, tên biến là 'authInterceptor' (viết thường).


export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding()),
    provideClientHydration(),
    provideAnimations(),

    // Cấu hình HttpClient với Interceptor
    // Đảm bảo tên biến ở đây khớp với tên bạn đã import
    provideHttpClient(withInterceptors([authInterceptor])), // Đã sửa tên biến

    AuthService,
    AuthGuard,
  ]
};
