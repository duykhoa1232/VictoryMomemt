// src/main.server.ts
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { config } from './app/app.config.server'; // Đảm bảo import 'config' là named import

const bootstrap = () => bootstrapApplication(AppComponent, config);

export default bootstrap;
