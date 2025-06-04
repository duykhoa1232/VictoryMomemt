// src/app/core/home/edit-post-dialog/edit-post-dialog.component.ts
import { Component, OnInit, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl } from '@angular/forms';

// Angular Material Modules
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';

import { Post } from '../services/post.service'; // Import Post interface

// Định nghĩa interface cho dữ liệu truyền vào dialog
export interface EditPostDialogData {
  post: Post; // Bài đăng hiện tại cần chỉnh sửa
}

@Component({
  selector: 'app-edit-post-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatCardModule, // Có thể dùng MatCard để tạo giao diện form trong dialog
    MatSnackBarModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions
  ],
  templateUrl: './edit-post-dialog.component.html', // Trỏ đến file HTML
  styleUrls: ['./edit-post-dialog.component.css'] // Trỏ đến file CSS
})
export class EditPostDialogComponent implements OnInit {
  editForm!: FormGroup;
  selectedNewImages: File[] = []; // Mảng chứa các ảnh MỚI được chọn
  selectedNewVideos: File[] = []; // Mảng chứa các video MỚI được chọn
  selectedNewAudios: File[] = []; // Mảng chứa các audio MỚI được chọn

  constructor(
    public dialogRef: MatDialogRef<EditPostDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: EditPostDialogData,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    // Khởi tạo form với dữ liệu hiện có của bài đăng
    this.editForm = new FormGroup({
      content: new FormControl(this.data.post.content || ''),
      location: new FormControl(this.data.post.location || ''),
    });
  }

  // Phương thức xử lý khi người dùng chọn file media mới
  onFileSelected(event: any, type: 'images' | 'videos' | 'audios'): void {
    const files: FileList = event.target.files;
    if (files && files.length > 0) {
      // Định nghĩa giới hạn kích thước cho từng loại file (MB)
      const MAX_IMAGE_SIZE_MB = 10;
      const MAX_VIDEO_SIZE_MB = 100;
      const MAX_AUDIO_SIZE_MB = 20;

      let maxFileSize = 0;
      let fileTypeDisplayName = '';

      // Xác định giới hạn kích thước và tên hiển thị dựa trên loại file
      switch (type) {
        case 'images':
          maxFileSize = MAX_IMAGE_SIZE_MB * 1024 * 1024;
          fileTypeDisplayName = 'ảnh';
          break;
        case 'videos':
          maxFileSize = MAX_VIDEO_SIZE_MB * 1024 * 1024;
          fileTypeDisplayName = 'video';
          break;
        case 'audios':
          maxFileSize = MAX_AUDIO_SIZE_MB * 1024 * 1024;
          fileTypeDisplayName = 'audio';
          break;
      }

      const newFiles: File[] = [];
      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        // Kiểm tra kích thước file
        if (file.size > maxFileSize) {
          this.snackBar.open(
            `File ${file.name} quá lớn. Kích thước tối đa cho phép cho ${fileTypeDisplayName} là ${maxFileSize / (1024 * 1024)}MB.`,
            'Đóng',
            {
              duration: 5000,
              panelClass: ['warning-snackbar'],
            }
          );
          continue; // Bỏ qua file quá lớn
        }
        newFiles.push(file); // Thêm file hợp lệ vào mảng tạm thời
      }

      // Gán các file mới đã chọn vào mảng tương ứng
      if (type === 'images') {
        this.selectedNewImages = [...this.selectedNewImages, ...newFiles];
      } else if (type === 'videos') {
        this.selectedNewVideos = [...this.selectedNewVideos, ...newFiles];
      } else if (type === 'audios') {
        this.selectedNewAudios = [...this.selectedNewAudios, ...newFiles];
      }
    }
    // Xóa giá trị của input file để có thể chọn lại cùng một file
    event.target.value = '';
  }

  // Phương thức xóa file media mới đã chọn khỏi danh sách preview
  removeSelectedNewFile(index: number, type: 'images' | 'videos' | 'audios'): void {
    if (type === 'images') {
      this.selectedNewImages.splice(index, 1);
    } else if (type === 'videos') {
      this.selectedNewVideos.splice(index, 1);
    } else if (type === 'audios') {
      this.selectedNewAudios.splice(index, 1);
    }
  }

  // Phương thức khi người dùng nhấp vào nút "Hủy"
  onCancel(): void {
    this.dialogRef.close(null); // Đóng dialog và trả về null (không có thay đổi)
  }

  // Phương thức khi người dùng nhấp vào nút "Lưu"
  onSave(): void {
    // Đánh dấu tất cả các trường form là đã chạm để hiển thị lỗi validation (nếu có)
    this.editForm.markAllAsTouched();

    // Lấy giá trị nội dung và địa điểm từ form
    const updatedContent = this.editForm.get('content')?.value;
    const updatedLocation = this.editForm.get('location')?.value;

    // Kiểm tra nếu không có thay đổi nào về nội dung/địa điểm và không có media mới được chọn
    if (
      updatedContent === this.data.post.content &&
      updatedLocation === this.data.post.location &&
      this.selectedNewImages.length === 0 &&
      this.selectedNewVideos.length === 0 &&
      this.selectedNewAudios.length === 0
    ) {
      this.snackBar.open('Không có thay đổi nào để lưu.', 'Đóng', {
        duration: 3000,
        panelClass: ['info-snackbar'],
      });
      this.dialogRef.close(null); // Đóng dialog mà không lưu
      return;
    }

    // Tạo FormData để gửi dữ liệu và file media lên backend
    const formData = new FormData();
    formData.append('content', updatedContent || ''); // Đảm bảo gửi chuỗi rỗng nếu null/undefined
    formData.append('location', updatedLocation || '');

    // Thêm các file media MỚI được chọn vào FormData
    this.selectedNewImages.forEach(file => {
      formData.append('images', file, file.name);
    });
    this.selectedNewVideos.forEach(file => {
      formData.append('videos', file, file.name);
    });
    this.selectedNewAudios.forEach(file => {
      formData.append('audios', file, file.name);
    });

    // ĐÃ SỬA: Đóng dialog và trả về một đối tượng chứa formData và deletedMediaUrls rỗng
    this.dialogRef.close({
      formData: formData,
      deletedMediaUrls: { images: [], videos: [], audios: [] } // Trả về rỗng vì dialog này không xử lý xóa media cũ
    });
  }
}













// // src/app/core/home/edit-post-dialog/edit-post-dialog.component.ts
// import { Component, OnInit, Inject } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
//
// // Angular Material Modules
// import { MatFormFieldModule } from '@angular/material/form-field';
// import { MatInputModule } from '@angular/material/input';
// import { MatIconModule } from '@angular/material/icon';
// import { MatButtonModule } from '@angular/material/button';
// import { MatCardModule } from '@angular/material/card';
// import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
// import { MatDialogRef, MAT_DIALOG_DATA, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
// import { MatSelectModule } from '@angular/material/select'; // THÊM: MatSelectModule
//
// import {Post, PostService} from '../services/post.service'; // Import PostService
//
// // Định nghĩa interface cho dữ liệu truyền vào dialog
// export interface EditPostDialogData {
//   post: Post; // Bài đăng hiện tại cần chỉnh sửa
// }
//
// @Component({
//   selector: 'app-edit-post-dialog',
//   standalone: true,
//   imports: [
//     CommonModule,
//     ReactiveFormsModule,
//     MatFormFieldModule,
//     MatInputModule,
//     MatIconModule,
//     MatButtonModule,
//     MatCardModule,
//     MatSnackBarModule,
//     MatDialogTitle,
//     MatDialogContent,
//     MatDialogActions,
//     MatSelectModule // THÊM
//   ],
//   templateUrl: './edit-post-dialog.component.html',
//   styleUrls: ['./edit-post-dialog.component.css']
// })
// export class EditPostDialogComponent implements OnInit {
//   editForm!: FormGroup;
//   selectedNewImages: File[] = [];
//   selectedNewVideos: File[] = [];
//   selectedNewAudios: File[] = [];
//
//   constructor(
//     public dialogRef: MatDialogRef<EditPostDialogComponent>,
//     @Inject(MAT_DIALOG_DATA) public data: EditPostDialogData,
//     private snackBar: MatSnackBar,
//     private postService: PostService // Inject PostService để gọi API update
//   ) {}
//
//   ngOnInit(): void {
//     // Chuyển đổi allowedUserIds từ mảng sang chuỗi cách nhau bởi dấu phẩy
//     const initialAllowedUserIdsInput = this.data.post.allowedUserIds ? this.data.post.allowedUserIds.join(', ') : '';
//
//     // Khởi tạo form với dữ liệu hiện có của bài đăng
//     this.editForm = new FormGroup({
//       content: new FormControl(this.data.post.content || '', Validators.required),
//       location: new FormControl(this.data.post.location || ''),
//       privacy: new FormControl(this.data.post.privacy, Validators.required), // ĐÃ CẬP NHẬT: Đổi từ 'visibility' sang 'privacy'
//       allowedUserIdsInput: new FormControl(initialAllowedUserIdsInput), // THÊM: Trường nhập liệu cho ID người dùng
//     });
//
//     // Theo dõi sự thay đổi của trường privacy để thêm/xóa validator cho allowedUserIdsInput
//     this.editForm.get('privacy')?.valueChanges.subscribe(privacy => { // ĐÃ CẬP NHẬT: Đổi từ 'visibility' sang 'privacy'
//       const allowedUserIdsInputControl = this.editForm.get('allowedUserIdsInput');
//       if (privacy === 'CUSTOM') {
//         allowedUserIdsInputControl?.setValidators(Validators.required);
//       } else {
//         allowedUserIdsInputControl?.clearValidators();
//         allowedUserIdsInputControl?.setValue(''); // Xóa giá trị khi không phải CUSTOM
//       }
//       allowedUserIdsInputControl?.updateValueAndValidity();
//     });
//   }
//
//   onFileSelected(event: any, type: 'images' | 'videos' | 'audios'): void {
//     const files: FileList = event.target.files;
//     if (files && files.length > 0) {
//       const MAX_IMAGE_SIZE_MB = 10;
//       const MAX_VIDEO_SIZE_MB = 100;
//       const MAX_AUDIO_SIZE_MB = 20;
//
//       let maxFileSize = 0;
//       let fileTypeDisplayName = '';
//
//       switch (type) {
//         case 'images':
//           maxFileSize = MAX_IMAGE_SIZE_MB * 1024 * 1024;
//           fileTypeDisplayName = 'ảnh';
//           break;
//         case 'videos':
//           maxFileSize = MAX_VIDEO_SIZE_MB * 1024 * 1024;
//           fileTypeDisplayName = 'video';
//           break;
//         case 'audios':
//           maxFileSize = MAX_AUDIO_SIZE_MB * 1024 * 1024;
//           fileTypeDisplayName = 'audio';
//           break;
//       }
//
//       const newFiles: File[] = [];
//       for (let i = 0; i < files.length; i++) {
//         const file = files[i];
//         if (file.size > maxFileSize) {
//           this.snackBar.open(
//             `File ${file.name} quá lớn. Kích thước tối đa cho phép cho ${fileTypeDisplayName} là ${maxFileSize / (1024 * 1024)}MB.`,
//             'Đóng',
//             {
//               duration: 5000,
//               panelClass: ['warning-snackbar'],
//             }
//           );
//           continue;
//         }
//         newFiles.push(file);
//       }
//
//       if (type === 'images') {
//         this.selectedNewImages = [...this.selectedNewImages, ...newFiles];
//       } else if (type === 'videos') {
//         this.selectedNewVideos = [...this.selectedNewVideos, ...newFiles];
//       } else if (type === 'audios') {
//         this.selectedNewAudios = [...this.selectedNewAudios, ...newFiles];
//       }
//     }
//     event.target.value = '';
//   }
//
//   removeSelectedNewFile(index: number, type: 'images' | 'videos' | 'audios'): void {
//     if (type === 'images') {
//       this.selectedNewImages.splice(index, 1);
//     } else if (type === 'videos') {
//       this.selectedNewVideos.splice(index, 1);
//     } else if (type === 'audios') {
//       this.selectedNewAudios.splice(index, 1);
//     }
//   }
//
//   onCancel(): void {
//     this.dialogRef.close(null);
//   }
//
//   onSave(): void {
//     this.editForm.markAllAsTouched();
//
//     if (this.editForm.invalid) {
//       this.snackBar.open('Vui lòng kiểm tra lại thông tin bài viết và quyền riêng tư!', 'Đóng', {
//         duration: 3000,
//         panelClass: ['warning-snackbar'],
//       });
//       return;
//     }
//
//     const postId = this.data.post.id;
//     const updatedContent = this.editForm.get('content')?.value;
//     const updatedLocation = this.editForm.get('location')?.value;
//     const updatedPrivacy = this.editForm.get('privacy')?.value; // ĐÃ CẬP NHẬT: Đổi từ 'visibility' sang 'privacy'
//     const updatedAllowedUserIdsInput = this.editForm.get('allowedUserIdsInput')?.value;
//
//     // Kiểm tra nếu không có thay đổi nào về nội dung/địa điểm/privacy/allowedUserIds và không có media mới được chọn
//     const isContentChanged = updatedContent !== this.data.post.content;
//     const isLocationChanged = updatedLocation !== this.data.post.location;
//     const isPrivacyChanged = updatedPrivacy !== this.data.post.privacy; // ĐÃ CẬP NHẬT: Đổi từ 'visibility' sang 'privacy'
//
//     let isAllowedUserIdsChanged = false;
//     if (updatedPrivacy === 'CUSTOM') { // ĐÃ CẬP NHẬT: Đổi từ 'visibility' sang 'privacy'
//       const currentAllowedIds = this.data.post.allowedUserIds ? this.data.post.allowedUserIds.join(', ') : '';
//       isAllowedUserIdsChanged = updatedAllowedUserIdsInput !== currentAllowedIds;
//     } else if (this.data.post.privacy === 'CUSTOM') { // ĐÃ CẬP NHẬT: Đổi từ 'visibility' sang 'privacy'
//       isAllowedUserIdsChanged = true;
//     }
//
//     if (
//       !isContentChanged &&
//       !isLocationChanged &&
//       !isPrivacyChanged && // ĐÃ CẬP NHẬT
//       !isAllowedUserIdsChanged &&
//       this.selectedNewImages.length === 0 &&
//       this.selectedNewVideos.length === 0 &&
//       this.selectedNewAudios.length === 0
//     ) {
//       this.snackBar.open('Không có thay đổi nào để lưu.', 'Đóng', {
//         duration: 3000,
//         panelClass: ['info-snackbar'],
//       });
//       this.dialogRef.close(null);
//       return;
//     }
//
//     const formData = new FormData();
//     formData.append('content', updatedContent || '');
//     formData.append('location', updatedLocation || '');
//     formData.append('privacy', updatedPrivacy); // ĐÃ CẬP NHẬT: Gửi 'privacy'
//
//     // Xử lý allowedUserIds nếu privacy là CUSTOM
//     if (updatedPrivacy === 'CUSTOM') { // ĐÃ CẬP NHẬT: Đổi từ 'visibility' sang 'privacy'
//       const allowedUserIds = updatedAllowedUserIdsInput.split(',').map((id: string) => id.trim()).filter((id: string) => id.length > 0);
//       formData.append('allowedUserIds', JSON.stringify(allowedUserIds));
//     }
//
//     this.selectedNewImages.forEach(file => {
//       formData.append('images', file, file.name);
//     });
//     this.selectedNewVideos.forEach(file => {
//       formData.append('videos', file, file.name);
//     });
//     this.selectedNewAudios.forEach(file => {
//       formData.append('audios', file, file.name);
//     });
//
//     this.snackBar.open('Đang cập nhật bài viết...', 'Đóng', { duration: 3000 });
//
//     this.postService.updatePost(postId, formData).subscribe({
//       next: (response) => {
//         console.log('Bài viết đã được cập nhật thành công:', response);
//         this.snackBar.open('Bài viết đã được cập nhật thành công!', 'Đóng', { duration: 3000 });
//         this.dialogRef.close(true);
//       },
//       error: (error) => {
//         console.error('Lỗi khi cập nhật bài viết:', error);
//         let errorMessage = 'Đã xảy ra lỗi khi cập nhật bài viết.';
//         if (error.error && typeof error.error === 'object') {
//           const validationErrors = Object.values(error.error).join('; ');
//           errorMessage = `Lỗi xác thực: ${validationErrors}`;
//         } else if (error.error && error.error.message) {
//           errorMessage = error.error.message;
//         } else if (error.message) {
//           errorMessage = error.message;
//         }
//         this.snackBar.open(errorMessage, 'Đóng', {
//           duration: 5000,
//           panelClass: ['error-snackbar'],
//         });
//       },
//     });
//   }
// }
