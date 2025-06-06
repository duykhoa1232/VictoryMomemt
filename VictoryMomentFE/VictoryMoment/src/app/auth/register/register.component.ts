// import { Component, OnInit } from '@angular/core';
// import { Router, RouterLink } from '@angular/router';
// import { FormsModule, ReactiveFormsModule, FormGroup, FormControl, Validators, ValidatorFn, AbstractControl } from '@angular/forms'; // Thêm AbstractControl
// import { CommonModule } from '@angular/common';
// import { AuthService, RegisterRequest } from '../services/auth.service';
//
// @Component({
//   selector: 'app-register',
//   standalone: true,
//   imports: [FormsModule, RouterLink, ReactiveFormsModule, CommonModule],
//   templateUrl: './register.component.html',
//   styleUrls: ['./register.component.css']
// })
// export class RegisterComponent implements OnInit {
//   registerForm!: FormGroup;
//   successMessage: string = '';
//   errorMessage: string = '';
//
//   constructor(private authService: AuthService, private router: Router) {}
//
//   ngOnInit(): void {
//     this.registerForm = new FormGroup({
//       username: new FormControl('', [Validators.required, Validators.minLength(3)]),
//       name: new FormControl('', Validators.required),
//       email: new FormControl('', [Validators.required, Validators.email]),
//       phoneNumber: new FormControl('', [Validators.required, Validators.pattern(/^\d{10}$/)]),
//       password: new FormControl('', [Validators.required, Validators.minLength(6)]),
//       confirmPassword: new FormControl('', Validators.required)
//     }, { validators: RegisterComponent.passwordMatchValidator }); // Gọi hàm tĩnh từ class
//   }
//
//   // Custom validator tĩnh để kiểm tra khớp mật khẩu
//   static passwordMatchValidator: ValidatorFn = (control: AbstractControl): { [key: string]: boolean } | null => {
//     const password = control.get('password');
//     const confirmPassword = control.get('confirmPassword');
//
//     if (!password || !confirmPassword) {
//       return null;
//     }
//
//     if (password.value !== confirmPassword.value) {
//       return { 'mismatch': true };
//     }
//     return null;
//   };
//
//   // Getter tiện lợi để truy cập các form control trong template
//   get f() { return this.registerForm.controls; }
//
//   onRegister(): void {
//     if (this.registerForm.invalid) {
//       this.errorMessage = 'Vui lòng điền đầy đủ và đúng thông tin.';
//       this.registerForm.markAllAsTouched();
//       return;
//     }
//
//     const request: RegisterRequest = {
//       username: this.registerForm.value.username,
//       name: this.registerForm.value.name,
//       email: this.registerForm.value.email,
//       phoneNumber: this.registerForm.value.phoneNumber,
//       password: this.registerForm.value.password
//     };
//
//     this.authService.register(request).subscribe({
//       next: (response: string) => {
//         console.log('Register successful', response);
//         this.successMessage = 'Đăng ký thành công! Bạn có thể đăng nhập ngay bây giờ.';
//         this.errorMessage = '';
//         this.router.navigate(['/login']);
//       },
//       error: (error: any) => {
//         console.error('Register failed', error);
//         this.successMessage = '';
//         this.errorMessage = error?.error || 'Đăng ký thất bại. Vui lòng thử lại.';
//       }
//     });
//   }
// }



import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormGroup, FormControl, Validators, ValidatorFn, AbstractControl, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService, RegisterRequest } from '../services/auth.service';

// Angular Material Modules
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar'; // Import MatSnackBar

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
    MatSnackBarModule // <<< THÊM DÒNG NÀY

    // MatSnackBarModule // Chỉ cần nếu là standalone component và chưa import ở app.config.ts
  ],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  // successMessage = ''; // Không cần giữ nếu dùng snackbar
  // errorMessage = '';   // Không cần giữ nếu dùng snackbar

  constructor(
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar // Inject MatSnackBar
  ) {}

  ngOnInit(): void {
    this.registerForm = new FormGroup({
      username: new FormControl('', [Validators.required, Validators.minLength(3)]),
      name: new FormControl('', Validators.required),
      email: new FormControl('', [Validators.required, Validators.email]),
      phoneNumber: new FormControl('', [Validators.required, Validators.pattern(/^\d{10}$/)]),
      password: new FormControl('', [Validators.required, Validators.minLength(6)]),
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
    // this.successMessage = ''; // Không cần reset
    // this.errorMessage = '';   // Không cần reset

    if (this.registerForm.invalid) {
      this.snackBar.open('Vui lòng điền đầy đủ và đúng thông tin.', 'Đóng', {
        duration: 3000,
        panelClass: ['snackbar-error']
      });
      this.registerForm.markAllAsTouched();
      return;
    }

    const request: RegisterRequest = {
      username: this.f['username'].value,
      name: this.f['name'].value,
      email: this.f['email'].value,
      phoneNumber: this.f['phoneNumber'].value,
      password: this.f['password'].value
    };

    this.authService.register(request).subscribe({
      next: () => {
        this.snackBar.open('Đăng ký thành công! Vui lòng đăng nhập.', 'Đóng', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });
        this.router.navigate(['/login']);
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
}
