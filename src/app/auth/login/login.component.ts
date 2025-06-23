// // src/app/auth/login/login.component.ts
// import { Component, OnInit } from '@angular/core';
// import { Router, RouterLink } from '@angular/router';
// import { CommonModule } from '@angular/common';
// import { FormGroup, FormControl, Validators, ReactiveFormsModule } from '@angular/forms';
//
// import { AuthService } from '../services/auth.service';
// import { LoginRequest, AuthResponse } from '../../shared/models/auth.model';
//
// import { MatFormFieldModule } from '@angular/material/form-field';
// import { MatInputModule } from '@angular/material/input';
// import { MatButtonModule } from '@angular/material/button';
// import { MatCardModule } from '@angular/material/card';
// import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
// import {MatIcon} from '@angular/material/icon';
// import {TranslatePipe} from '@ngx-translate/core';
// import {I18nService} from '../../core/home/services/i18n.service';
//
// @Component({
//   selector: 'app-login',
//   standalone: true,
//   imports: [
//     CommonModule,
//     ReactiveFormsModule,
//     RouterLink,
//     MatFormFieldModule,
//     MatInputModule,
//     MatButtonModule,
//     MatCardModule,
//     MatSnackBarModule,
//     MatIcon,
//     TranslatePipe
//   ],
//   templateUrl: './login.component.html',
//   styleUrls: ['./login.component.css']
// })
// export class LoginComponent implements OnInit {
//   loginForm!: FormGroup;
//   fieldTextType?:boolean=false
//
//   constructor(
//     private router: Router,
//     private authService: AuthService,
//     private snackBar: MatSnackBar,
//     public i18n: I18nService // ✅ Dùng service bạn đã tạo
//
//   ) {
//     this.loginForm = new FormGroup({
//       email: new FormControl('', [Validators.required, Validators.email]),
//       password: new FormControl('', Validators.required)
//     });
//   }
//   switchLang(lang: string): void {
//     this.i18n.switchLang(lang);
//   }
//   toggleFieldTextType(){
//     this.fieldTextType=!this.fieldTextType;
//   }
//   ngOnInit(): void {
//     // Kiểm tra trạng thái đăng nhập khi vào trang
//     if (this.authService.isUserLoggedIn()) {
//       this.router.navigate(['/home']).then(() => {
//         console.log('Đã chuyển hướng về /home vì đã đăng nhập');
//       });
//     }
//   }
//
//   get f() { return this.loginForm.controls; }
//
//   handleLogin(): void {
//     if (this.loginForm.invalid) {
//       this.snackBar.open('Please enter a valid email and password.', 'Đóng', {
//         duration: 3000,
//         panelClass: ['snackbar-error']
//       });
//       this.loginForm.markAllAsTouched();
//       return;
//     }
//
//     const loginData: LoginRequest = {
//       email: this.f['email'].value,
//       password: this.f['password'].value
//     };
//
//     this.authService.login(loginData).subscribe({
//       next: (res: AuthResponse) => {
//         console.log('Login successful! Token:', res.token);
//         this.snackBar.open('Login successful!', 'Đóng', {
//           duration: 3000,
//           panelClass: ['snackbar-success']
//         });
//         this.router.navigate(['/home']);
//       },
//       error: (err: any) => {
//         const message = err.status === 401
//           ? 'Incorrect email or password'
//           : err?.error?.message || 'An error occurred while logging in. Please try again.';
//
//         this.snackBar.open(message, 'Đóng', {
//           duration: 5000,
//           panelClass: ['snackbar-error']
//         });
//         console.error('Login error:', err);
//       }
//     });
//   }
// }


import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormGroup, FormControl, Validators, ReactiveFormsModule } from '@angular/forms';

import { AuthService } from '../services/auth.service';
import { LoginRequest, AuthResponse } from '../../shared/models/auth.model';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatIcon } from '@angular/material/icon';
import { TranslatePipe } from '@ngx-translate/core';
import { I18nService } from '../../core/home/services/i18n.service';

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
    MatSnackBarModule,
    MatIcon,
    TranslatePipe
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  fieldTextType?: boolean = false;

  constructor(
    private router: Router,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    public i18n: I18nService
  ) {
    this.loginForm = new FormGroup({
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', Validators.required)
    });
  }

  switchLang(lang: string): void {
    this.i18n.switchLang(lang);
  }

  toggleFieldTextType() {
    this.fieldTextType = !this.fieldTextType;
  }

  ngOnInit(): void {
    if (this.authService.isUserLoggedIn()) {
      this.router.navigate(['/home']).then(() => {
        console.log('Đã chuyển hướng về /home vì đã đăng nhập');
      });
    }
  }

  get f() { return this.loginForm.controls; }

  handleLogin(): void {
    if (this.loginForm.invalid) {
      this.snackBar.open(
        this.i18n.instant('AUTH_LOGIN_SNACKBAR.INVALID_CREDENTIALS'),
        'Đóng',
        { duration: 3000, panelClass: ['error-snackbar'] }
      );
      this.loginForm.markAllAsTouched();
      return;
    }

    const loginData: LoginRequest = {
      email: this.f['email'].value,
      password: this.f['password'].value
    };

    this.authService.login(loginData).subscribe({
      next: (res: AuthResponse) => {
        console.log('Login successful! Token:', res.token);
        this.snackBar.open(
          this.i18n.instant('AUTH_LOGIN_SNACKBAR.LOGIN_SUCCESS'),
          'Đóng',
          { duration: 3000, panelClass: ['error-snackbar'] }
        );
        this.router.navigate(['/home']);
      },
      error: (err: any) => {
        const message = err.status === 401
          ? this.i18n.instant('AUTH_LOGIN_SNACKBAR.INVALID_CREDENTIALS')
          : this.i18n.instant('AUTH_LOGIN_SNACKBAR.LOGIN_ERROR');
        this.snackBar.open(
          message,
          'Đóng',
          { duration: 5000, panelClass: ['error-snackbar'] }
        );
        console.error('Login error:', err);
      }
    });
  }
}
