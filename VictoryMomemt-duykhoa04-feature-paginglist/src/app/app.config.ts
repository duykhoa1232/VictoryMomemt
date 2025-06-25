//
// import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
// import { provideRouter, withComponentInputBinding } from '@angular/router';
// import { routes } from './app.routes';
// import { provideClientHydration } from '@angular/platform-browser';
// import { provideAnimations } from '@angular/platform-browser/animations';
// import { provideHttpClient, withInterceptors } from '@angular/common/http';
// import { AuthService } from './auth/services/auth.service';
// import { AuthGuard } from './auth/guards/AuthGuard.service';
// import {authInterceptor} from './interceptors/auth.interceptor';
//
// export const appConfig: ApplicationConfig = {
//   providers: [
//     provideZoneChangeDetection({ eventCoalescing: true }),
//     provideRouter(routes, withComponentInputBinding()),
//     provideClientHydration(),
//     provideAnimations(),
//     provideHttpClient(withInterceptors([authInterceptor])),
//     AuthService,
//     AuthGuard,
//   ]
// };
//
//
//
//


import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { routes } from './app.routes';
import { provideClientHydration } from '@angular/platform-browser';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideHttpClient, withInterceptors, withFetch } from '@angular/common/http'; // THÊM withFetch
import { AuthService } from './auth/services/auth.service';
import { AuthGuard } from './auth/guards/AuthGuard.service';
import {authInterceptor} from './interceptors/auth.interceptor';
import {provideI18n} from './i18n.config';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding()),
    provideClientHydration(),
    provideAnimations(),
    // THÊM withFetch() VÀO provideHttpClient()
    provideHttpClient(withFetch(), withInterceptors([authInterceptor])),
    AuthService,
    AuthGuard,
    provideI18n(), // 👈 thêm dòng này để tích hợp i18n
    provideAnimations(), // ✅ OK

  ]
};
