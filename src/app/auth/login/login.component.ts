// import { Component } from '@angular/core';
// import { Router, RouterLink } from '@angular/router';
// import { FormsModule } from '@angular/forms';
// import { CommonModule } from '@angular/common';
// import { AuthService, LoginRequest, AuthResponse } from '../services/auth.service';
//
// @Component({
//   selector: 'app-login',
//   standalone: true,
//   imports: [FormsModule, RouterLink, CommonModule],
//   templateUrl: './login.component.html',
//   styleUrls: ['./login.component.css']
// })
// export class LoginComponent {
//   email: string = '';
//   password: string = '';
//   errorMessage: string = '';
//
//   constructor(
//     private router: Router,
//     private authService: AuthService
//   ) {}
//
//   handleLogin() {
//     if (!this.email || !this.password) {
//       this.errorMessage = 'Vui lòng nhập email và mật khẩu';
//       return;
//     }
//
//     const loginData: LoginRequest = { email: this.email, password: this.password };
//
//     this.authService.login(loginData).subscribe({
//       next: (res: AuthResponse) => {
//         console.log('Đăng nhập thành công! Token:', res.token);
//
//         this.router.navigate(['/welcome', this.email]);
//       },
//       error: (err: any) => {
//         console.error('Lỗi đăng nhập:', err);
//         if (err.status === 401) {
//           this.errorMessage = 'Email hoặc mật khẩu không đúng.';
//         } else {
//           this.errorMessage = err?.error?.message || 'Đã xảy ra lỗi khi đăng nhập. Vui lòng thử lại sau.';
//         }
//       }
//     });
//   }
// }



// src/app/auth/login/login.component.ts
import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormGroup, FormControl, Validators, ReactiveFormsModule } from '@angular/forms';

import { AuthService, LoginRequest, AuthResponse } from '../services/auth.service';

// Angular Material Modules
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar'; // Import MatSnackBar

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatSnackBarModule // <<< THÊM DÒNG NÀY

    // MatSnackBarModule // Chỉ cần nếu là standalone component và chưa import ở app.config.ts
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  // errorMessage = ''; // Không cần giữ errorMessage nếu dùng snackbar

  constructor(
    private router: Router,
    private authService: AuthService,
    private snackBar: MatSnackBar // Inject MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loginForm = new FormGroup({
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', Validators.required)
    });
  }

  get f() { return this.loginForm.controls; }

  handleLogin(): void {
    // this.errorMessage = ''; // Không cần reset nếu dùng snackbar

    if (this.loginForm.invalid) {
      this.snackBar.open('Vui lòng nhập đầy đủ email và mật khẩu hợp lệ.', 'Đóng', {
        duration: 3000,
        panelClass: ['snackbar-error'] // Thêm class CSS cho style riêng
      });
      this.loginForm.markAllAsTouched();
      return;
    }

    const loginData: LoginRequest = {
      email: this.f['email'].value,
      password: this.f['password'].value
    };

    this.authService.login(loginData).subscribe({
      next: (res: AuthResponse) => {
        console.log('Đăng nhập thành công! Token:', res.token);
        this.snackBar.open('Đăng nhập thành công!', 'Đóng', {
          duration: 3000,
          panelClass: ['snackbar-success'] // Thêm class CSS cho style riêng
        });
        this.router.navigate(['/home']);
      },
      error: (err: any) => {
        const message = err.status === 401
          ? 'Email hoặc mật khẩu không đúng.'
          : err?.error?.message || 'Đã xảy ra lỗi trong quá trình đăng nhập. Vui lòng thử lại.';

        this.snackBar.open(message, 'Đóng', {
          duration: 5000, // Hiển thị lâu hơn cho lỗi
          panelClass: ['snackbar-error'] // Thêm class CSS cho style riêng
        });
        console.error('Lỗi đăng nhập:', err);
      }
    });
  }
}
