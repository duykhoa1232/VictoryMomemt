// import { Component, OnInit } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms'; // Thêm Validators
// import { MatCardModule } from '@angular/material/card';
// import { MatFormFieldModule } from '@angular/material/form-field';
// import { MatInputModule } from '@angular/material/input';
// import { MatButtonModule } from '@angular/material/button';
// import { MatIconModule } from '@angular/material/icon';
// import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
// import { Router } from '@angular/router'; // Import Router để điều hướng
//
// @Component({
//   selector: 'app-edit-profile',
//   standalone: true,
//   imports: [
//     CommonModule,
//     ReactiveFormsModule,
//     MatCardModule,
//     MatFormFieldModule,
//     MatInputModule,
//     MatButtonModule,
//     MatIconModule,
//     MatSnackBarModule
//   ],
//   templateUrl: './edit-profile.component.html',
//   styleUrls: ['./edit-profile.component.css']
// })
// export class EditProfileComponent implements OnInit {
//   editForm!: FormGroup;
//   currentProfileImageUrl: string = 'https://placehold.co/150x150/8BC34A/FFFFFF?text=NV'; // Ảnh placeholder ban đầu
//   newProfileImageFile: File | null = null;
//
//   // Dữ liệu hồ sơ ban đầu (tĩnh, để khởi tạo form và reset)
//   initialProfileData = {
//     name: 'Nguyễn Văn A',
//     username: 'nguyenvana',
//     bio: 'Chào mừng bạn đến với trang hồ sơ của tôi! Tôi là một người yêu thích công nghệ và chia sẻ những khoảnh khắc đáng nhớ.',
//     location: 'Hồ Chí Minh, Việt Nam',
//     website: 'https://example.com',
//     profileImageUrl: 'https://placehold.co/150x150/8BC34A/FFFFFF?text=NV' // Đảm bảo thuộc tính này tồn tại
//   };
//
//   constructor(
//     private snackBar: MatSnackBar,
//     private router: Router // Inject Router để điều hướng
//   ) { }
//
//   ngOnInit(): void {
//     // Khởi tạo FormGroup với FormControl cho từng trường
//     this.editForm = new FormGroup({
//       name: new FormControl(this.initialProfileData.name, Validators.required), // Thêm Validators.required
//       username: new FormControl(this.initialProfileData.username, Validators.required),
//       bio: new FormControl(this.initialProfileData.bio),
//       location: new FormControl(this.initialProfileData.location),
//       website: new FormControl(this.initialProfileData.website, Validators.pattern('^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$')), // Thêm Validators.pattern cho URL
//     });
//
//     // Khởi tạo ảnh xem trước với ảnh ban đầu
//     this.currentProfileImageUrl = this.initialProfileData.profileImageUrl;
//   }
//
//   onFileSelected(event: Event): void {
//     const input = event.target as HTMLInputElement;
//     if (input.files && input.files[0]) {
//       const file = input.files[0];
//       this.newProfileImageFile = file;
//
//       const reader = new FileReader();
//       reader.onload = () => {
//         this.currentProfileImageUrl = reader.result as string; // Cập nhật ảnh xem trước
//       };
//       reader.readAsDataURL(file);
//     }
//   }
//
//   onSubmit(): void {
//     // Đánh dấu tất cả các trường là đã chạm để hiển thị lỗi validation
//     this.editForm.markAllAsTouched();
//
//     if (this.editForm.valid) {
//       const updatedData = this.editForm.value;
//       console.log('Dữ liệu hồ sơ đã cập nhật:', updatedData);
//       console.log('File ảnh đại diện mới:', this.newProfileImageFile);
//
//       this.snackBar.open('Hồ sơ đã được cập nhật thành công!', 'Đóng', { duration: 3000 });
//       this.router.navigate(['/profile']); // Điều hướng người dùng trở lại trang hồ sơ sau khi lưu
//     } else {
//       this.snackBar.open('Vui lòng kiểm tra lại thông tin nhập!', 'Đóng', { duration: 3000 });
//     }
//   }
//
//   onCancel(): void {
//     // Reset form về dữ liệu ban đầu
//     this.editForm.patchValue(this.initialProfileData);
//     this.currentProfileImageUrl = this.initialProfileData.profileImageUrl;
//     this.newProfileImageFile = null;
//     this.snackBar.open('Đã hủy bỏ thay đổi.', 'Đóng', { duration: 2000 });
//     this.router.navigate(['/profile']); // Điều hướng người dùng trở lại trang hồ sơ
//   }
// }
