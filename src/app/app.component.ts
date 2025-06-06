// src/app/app.component.ts

import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
// import { NgIf } from '@angular/common'; // Không cần import NgIf nếu không dùng trực tiếp trong template này
// import { HardcodeAuthenticationService } from './auth/services/hardcode-authentication.service'; // LOẠI BỎ IMPORT NÀY

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    // NgIf // Loại bỏ NgIf nếu không dùng
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  // Loại bỏ constructor và mọi logic liên quan đến HardcodeAuthenticationService
  // constructor(public authService: HardcodeAuthenticationService) {}
  // app.component sẽ không cần biết về trạng thái đăng nhập nữa
  // vì MainComponent sẽ xử lý hiển thị header/footer
}
