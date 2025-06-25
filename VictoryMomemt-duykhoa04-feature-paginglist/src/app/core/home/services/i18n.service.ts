// src/app/core/services/i18n.service.ts

import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Injectable({ providedIn: 'root' })
export class I18nService {
  constructor(public translate: TranslateService) {
    const lang = localStorage.getItem('lang') || 'vi';
    this.translate.setDefaultLang('vi');
    this.translate.use(lang);
  }

  switchLang(lang: string) {
    this.translate.use(lang);
    localStorage.setItem('lang', lang);
  }
  instant(key: string, interpolateParams?: Object): string {
    return this.translate.instant(key, interpolateParams);
  }
  get currentLang() {
    return this.translate.currentLang;
  }
}
