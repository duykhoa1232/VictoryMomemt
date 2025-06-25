// // src/app/core/home/services/language.service.ts
// import { Injectable } from '@angular/core';
// import { TranslateService } from '@ngx-translate/core';
// import { HttpClient } from '@angular/common/http';
//
// @Injectable({
//   providedIn: 'root'
// })
// export class LanguageService {
//   constructor(
//     private http: HttpClient
//   ) {}
//
//   initializeTranslations(translate: TranslateService): void {
//     translate.addLangs(['en', 'vi']);
//     translate.setDefaultLang('en');
//
//     // Định nghĩa bản dịch trực tiếp
//     translate.setTranslation('en', {
//       greeting: 'Hello, {{name}}',
//       welcome: 'Welcome to the app, {{name}}'
//     }, true);
//
//     translate.setTranslation('vi', {
//       greeting: 'Chào, {{name}}',
//       welcome: 'Chào mừng đến với ứng dụng, {{name}}'
//     }, true);
//   }
//
//   switchLanguage(lang: string, translate: TranslateService): void {
//     translate.use(lang);
//   }
//
//   getCurrentLang(translate: TranslateService): string {
//     return translate.currentLang || 'en';
//   }
// }
