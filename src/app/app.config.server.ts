// src/app/app.config.server.ts
import { ApplicationConfig } from '@angular/core';
import { provideServerRendering } from '@angular/platform-server';
import { provideRouter } from '@angular/router'; // Import provideRouter nếu bạn dùng nó trong app.config.ts
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './interceptors/auth.interceptor'; // Import authInterceptor

// Giả sử routes của bạn được định nghĩa trong app.routes.ts
import { routes } from './app.routes'; // THAY ĐỔI: Đảm bảo đường dẫn này đúng với routes của bạn

export const config: ApplicationConfig = {
  providers: [
    provideServerRendering(),
    provideRouter(routes), // Nếu bạn đã dùng provideRouter trong app.config.ts chính
    provideHttpClient(withInterceptors([authInterceptor])), // Đảm bảo HttpClient và interceptor được cung cấp
    // Thêm bất kỳ providers nào khác mà bạn cần trong môi trường server rendering
  ]
};
