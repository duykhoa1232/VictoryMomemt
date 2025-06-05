// src/app/auth/register/register.component.ts
import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormGroup, FormControl, Validators, ValidatorFn, AbstractControl, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../services/auth.service'; // Chỉ import AuthService
import { RegisterRequest } from '../../shared/models/auth.model'; // Import model từ shared
import emailjs from '@emailjs/browser';

// Angular Material Modules
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatSnackBarModule
  ],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;

  // Đảm bảo các giá trị này là chính xác từ tài khoản EmailJS của bạn
  private emailJsPublicKey: string = 'Ydy4qa1Dxag9ftXZZ';
  private emailJsServiceId: string = 'service_15paijq';
  private emailJsTemplateId: string = 'template_fcvi7u7';

  constructor(
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{6,}$/;

    this.registerForm = new FormGroup({
      name: new FormControl('', Validators.required),
      email: new FormControl('', [Validators.required, Validators.email]),
      phoneNumber: new FormControl('', [Validators.required, Validators.pattern(/^\d{10}$/)]),
      password: new FormControl('', [
        Validators.required,
        Validators.minLength(6),
        Validators.pattern(passwordPattern)
      ]),
      confirmPassword: new FormControl('', Validators.required)
    }, { validators: RegisterComponent.passwordMatchValidator });
  }

  static passwordMatchValidator: ValidatorFn = (control: AbstractControl): { [key: string]: boolean } | null => {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      return { 'mismatch': true };
    }
    return null;
  };

  get f() { return this.registerForm.controls; }

  onRegister(): void {
    if (this.registerForm.invalid) {
      this.snackBar.open('Vui lòng điền đầy đủ và đúng thông tin.', 'Đóng', {
        duration: 3000,
        panelClass: ['snackbar-error']
      });
      this.registerForm.markAllAsTouched();
      return;
    }

    const request: RegisterRequest = {
      name: this.f['name'].value,
      email: this.f['email'].value,
      phoneNumber: this.f['phoneNumber'].value,
      password: this.f['password'].value
    };

    this.authService.register(request).subscribe({
      next: () => {
        this.snackBar.open('Đăng ký thành công! Đang gửi email chào mừng...', 'Đóng', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });

        this.sendWelcomeEmail(request.name, request.email)
          .then(() => {
            this.snackBar.open('Email chào mừng đã được gửi thành công. Vui lòng đăng nhập.', 'Đóng', {
              duration: 5000,
              panelClass: ['snackbar-success']
            });
            this.router.navigate(['/login']);
          })
          .catch((emailError) => {
            console.error('Gửi email thất bại:', emailError);
            this.snackBar.open('Đăng ký thành công nhưng không thể gửi email chào mừng. Vui lòng đăng nhập.', 'Đóng', {
              duration: 7000,
              panelClass: ['snackbar-warning']
            });
            this.router.navigate(['/login']);
          });
      },
      error: (error: any) => {
        const message = error?.error?.message || 'Đăng ký thất bại. Vui lòng thử lại.';
        this.snackBar.open(message, 'Đóng', {
          duration: 5000,
          panelClass: ['snackbar-error']
        });
        console.error('Lỗi đăng ký:', error);
      }
    });
  }

  sendWelcomeEmail(name: string, email: string): Promise<void> {
    console.log('Sending email with these parameters to EmailJS:');
    console.log('Service ID:', this.emailJsServiceId);
    console.log('Template ID:', this.emailJsTemplateId);
    console.log('Public Key:', this.emailJsPublicKey);
    console.log('Data:', {
      from_name: name,
      email: email,
      message: 'Cảm ơn bạn đã đăng ký tài khoản tại hệ thống của chúng tôi!'
    });

    return emailjs.send(this.emailJsServiceId, this.emailJsTemplateId, {
      from_name: name,
      email: email,
      message: 'Cảm ơn bạn đã đăng ký tài khoản tại hệ thống của chúng tôi!'
    }, this.emailJsPublicKey)
      .then((response) => {
        console.log('Email chào mừng đã được gửi thành công!', response.status, response.text);
      })
      .catch((err) => {
        console.error('Gửi email thất bại:', err);
        throw err;
      });
  }
}
