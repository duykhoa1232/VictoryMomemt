// src/app/auth/login/login.component.ts
import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormGroup, FormControl, Validators, ReactiveFormsModule } from '@angular/forms';

import { AuthService } from '../services/auth.service'; // Chỉ import AuthService
import { LoginRequest, AuthResponse } from '../../shared/models/auth.model'; // Import models từ shared

// Angular Material Modules
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';

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
    MatSnackBarModule
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;

  constructor(
    private router: Router,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loginForm = new FormGroup({
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', Validators.required)
    });
  }

  get f() { return this.loginForm.controls; }

  handleLogin(): void {
    if (this.loginForm.invalid) {
      this.snackBar.open('Vui lòng nhập đầy đủ email và mật khẩu hợp lệ.', 'Đóng', {
        duration: 3000,
        panelClass: ['snackbar-error']
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
          panelClass: ['snackbar-success']
        });
        this.router.navigate(['/home']);
      },
      error: (err: any) => {
        const message = err.status === 401
          ? 'Email hoặc mật khẩu không đúng.'
          : err?.error?.message || 'Đã xảy ra lỗi khi đăng nhập. Vui lòng thử lại.';

        this.snackBar.open(message, 'Đóng', {
          duration: 5000,
          panelClass: ['snackbar-error']
        });
        console.error('Lỗi đăng nhập:', err);
      }
    });
  }
}
