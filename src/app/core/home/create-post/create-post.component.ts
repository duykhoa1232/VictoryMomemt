// src/app/core/home/create-post/create-post.component.ts
import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
// THAY ĐỔI: Không cần import Validators nếu bạn muốn content không bắt buộc và không có validator nào khác
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';

// Angular Material Modules
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';

import { AuthService } from '../../../auth/services/auth.service';
import { PostService } from '../services/post.service';

@Component({
  selector: 'app-create-post',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatCardModule,
    MatSnackBarModule,
    MatDividerModule,
  ],
  templateUrl: './create-post.component.html',
  styleUrls: ['./create-post.component.css'],
})
export class CreatePostComponent implements OnInit {
  postForm!: FormGroup;
  selectedImages: File[] = [];
  currentUserEmail: string | null = null;
  currentUserName: string = 'Bạn';

  @Output() postCreated = new EventEmitter<void>();

  constructor(
    private postService: PostService,
    private snackBar: MatSnackBar,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.postForm = new FormGroup({
      // THAY ĐỔI: XÓA Validators.required để content không bắt buộc
      content: new FormControl(''),
      location: new FormControl(''),
    });

    const token = this.authService.getToken();
    if (token) {
      this.currentUserEmail = this.authService.getToken();
      if (this.currentUserEmail && this.currentUserEmail.includes('@')) {
        this.currentUserName = this.currentUserEmail.split('@')[0];
      } else {
        this.currentUserName = 'Người dùng';
      }
    }
  }

  get f() {
    return this.postForm.controls;
  }

  onFileSelected(event: any, type: 'images'): void {
    const files: FileList = event.target.files;
    if (files && files.length > 0) {
      const file = files[0];
      console.log(`File đã chọn: ${file.name}, kích thước: ${file.size} bytes`);

      const MAX_FILE_SIZE_MB = 10;
      const MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;

      if (file.size > MAX_FILE_SIZE_BYTES) {
        this.snackBar.open(
          `File ${file.name} quá lớn. Kích thước tối đa cho phép là ${MAX_FILE_SIZE_MB}MB.`,
          'Đóng',
          {
            duration: 5000,
            panelClass: ['warning-snackbar'],
          }
        );
        event.target.value = '';
        return;
      }

      this.selectedImages = [file];
      console.log('Mảng selectedImages hiện tại:', this.selectedImages);
    }
  }

  removeSelectedFile(index: number, type: 'images'): void {
    if (type === 'images') {
      this.selectedImages.splice(index, 1);
      console.log(
        'Đã xóa ảnh ở vị trí',
        index,
        '. selectedImages mới:',
        this.selectedImages
      );
    }
  }

  onSubmit(): void {
    this.postForm.markAllAsTouched();

    // THAY ĐỔI LOGIC VALIDATION TRƯỚC KHI GỬI:
    // Bài đăng hợp lệ nếu CÓ nội dung HOẶC CÓ ảnh.
    const contentValue = this.f['content'].value;
    const hasContent = contentValue && contentValue.trim().length > 0;
    const hasImages = this.selectedImages.length > 0;

    if (!hasContent && !hasImages) {
      console.log('Form không hợp lệ: Không có nội dung và không có ảnh.');
      this.snackBar.open('Vui lòng nhập nội dung hoặc thêm ảnh/video!', 'Đóng', {
        duration: 3000,
        panelClass: ['warning-snackbar'],
        horizontalPosition: 'center',
        verticalPosition: 'bottom',
      });
      return;
    }

    const postData = this.postForm.value;
    const formData = new FormData();
    formData.append('content', postData.content || ''); // Đảm bảo gửi chuỗi rỗng nếu content null/undefined
    formData.append('location', postData.location || '');

    if (this.selectedImages.length > 0) {
      formData.append(
        'images', // Sử dụng 'images' để tương thích với backend nếu nó mong đợi mảng (dù chỉ có 1 phần tử)
        this.selectedImages[0],
        this.selectedImages[0].name
      );
    }

    this.postService.createPost(formData).subscribe({
      next: (response) => {
        console.log('Bài đăng đã được tạo thành công:', response);
        this.snackBar.open('Bài đăng đã được tạo thành công!', 'Đóng', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'bottom',
        });
        this.postForm.reset();
        this.selectedImages = [];
        // Không cần reset validation errors cụ thể nếu không có required validator nữa
        // this.f['content'].setErrors(null);
        // this.f['content'].markAsUntouched();
        // this.f['content'].markAsPristine();

        this.postCreated.emit();
      },
      error: (error) => {
        console.error('Lỗi khi tạo bài đăng:', error);
        let errorMessage = 'Đã xảy ra lỗi khi tạo bài đăng.';
        if (error.error && typeof error.error === 'object') {
          const validationErrors = Object.values(error.error).join('; ');
          errorMessage = `Lỗi xác thực: ${validationErrors}`;
        } else if (error.error && error.error.message) {
          errorMessage = error.error.message;
        } else if (error.message) {
          errorMessage = error.message;
        }
        this.snackBar.open(errorMessage, 'Đóng', {
          duration: 5000,
          panelClass: ['error-snackbar'],
          horizontalPosition: 'center',
          verticalPosition: 'bottom',
        });
      },
    });
  }
}
