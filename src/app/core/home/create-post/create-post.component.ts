import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

import { AuthService } from '../../../auth/services/auth.service';
import { PostService } from '../services/post.service';
import { UserResponse } from '../../../shared/models/user.model';
import { PostRequest, PostResponse } from '../../../shared/models/post.model';
import { UserSelectionDialogComponent } from '../../../shared/user-selection-dialog/user-selection-dialog.component';
import { environment } from '../../../../environments/environment';

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
    MatSelectModule,
    MatChipsModule,
  ],
  templateUrl: './create-post.component.html',
  styleUrls: ['./create-post.component.css'],
})
export class CreatePostComponent implements OnInit {
  postForm!: FormGroup;
  selectedImages: File[] = [];
  selectedVideos: File[] = [];
  selectedAudios: File[] = [];
  currentUserEmail: string | null = null;
  currentUserName: string = 'Bạn';
  sanitizedUrls: SafeUrl[] = []; // Mảng để lưu URL đã được sanitize

  selectedAllowedUsers: UserResponse[] = [];
  @Output() postCreated = new EventEmitter<void>();

  constructor(
    private postService: PostService,
    private snackBar: MatSnackBar,
    private authService: AuthService,
    private dialog: MatDialog,
    private sanitizer: DomSanitizer // Thêm DomSanitizer
  ) {}

  ngOnInit(): void {
    this.postForm = new FormGroup({
      content: new FormControl(''),
      location: new FormControl(''),
      privacy: new FormControl('PUBLIC', Validators.required),
      allowedUserIds: new FormControl([]),
    });

    this.currentUserName = this.authService.getCurrentUserName() || 'Người dùng';

    this.postForm.get('privacy')?.valueChanges.subscribe(privacy => {
      const allowedUserIdsControl = this.postForm.get('allowedUserIds');
      if (privacy === 'PRIVATE') {
        allowedUserIdsControl?.markAsUntouched();
        allowedUserIdsControl?.markAsPristine();
      } else {
        allowedUserIdsControl?.clearValidators();
        allowedUserIdsControl?.setValue([]);
        this.selectedAllowedUsers = [];
      }
      allowedUserIdsControl?.updateValueAndValidity();
    });
  }

  get f() {
    return this.postForm.controls;
  }

  onFileSelected(event: any, type: 'images' | 'videos' | 'audios'): void {
    const files: FileList = event.target.files;
    if (files && files.length > 0) {
      const MAX_IMAGE_SIZE_MB = 10;
      const MAX_VIDEO_SIZE_MB = 100;
      const MAX_AUDIO_SIZE_MB = 20;

      let maxFileSize = 0;
      let fileTypeDisplayName = '';

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
        if (file.size > maxFileSize) {
          this.snackBar.open(
            `File ${file.name} quá lớn. Kích thước tối đa cho phép cho ${fileTypeDisplayName} là ${maxFileSize / (1024 * 1024)}MB.`,
            'Đóng',
            {
              duration: 5000,
              panelClass: ['warning-snackbar'],
            }
          );
          continue;
        }
        newFiles.push(file);
        // Tạo URL tạm thời và sanitize
        if (type === 'images' || type === 'videos') {
          this.sanitizedUrls.push(this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(file)));
        }
      }

      if (type === 'images') {
        this.selectedImages = [...this.selectedImages, ...newFiles];
      } else if (type === 'videos') {
        this.selectedVideos = [...this.selectedVideos, ...newFiles];
      } else if (type === 'audios') {
        this.selectedAudios = [...this.selectedAudios, ...newFiles];
      }
      console.log(`Files đã chọn (${type}):`, newFiles);
    }
    event.target.value = '';
  }

  removeSelectedFile(index: number, type: 'images' | 'videos' | 'audios'): void {
    if (type === 'images') {
      this.selectedImages.splice(index, 1);
      this.sanitizedUrls.splice(index, 1); // Xóa URL tương ứng
    } else if (type === 'videos') {
      this.selectedVideos.splice(index, 1);
      this.sanitizedUrls.splice(index + this.selectedImages.length, 1); // Xóa URL tương ứng
    } else if (type === 'audios') {
      this.selectedAudios.splice(index, 1);
    }
    console.log(`Đã xóa file (${type}) ở vị trí ${index}.`);
  }

  removeAllowedUser(userToRemove: UserResponse): void {
    this.selectedAllowedUsers = this.selectedAllowedUsers.filter(user => user.id !== userToRemove.id);
    this.postForm.get('allowedUserIds')?.setValue(this.selectedAllowedUsers.map(u => u.id));
    this.postForm.get('allowedUserIds')?.markAsDirty();
    this.postForm.get('allowedUserIds')?.updateValueAndValidity();
  }

  openUserSelectionDialog(): void {
    const dialogRef = this.dialog.open(UserSelectionDialogComponent, {
      width: '600px',
      data: { selectedUsers: this.selectedAllowedUsers }
    });

    dialogRef.afterClosed().subscribe((result: UserResponse[] | undefined) => {
      if (result) {
        this.selectedAllowedUsers = result;
        this.postForm.get('allowedUserIds')?.setValue(this.selectedAllowedUsers.map(u => u.id));
        this.postForm.get('allowedUserIds')?.markAsDirty();
        this.postForm.get('allowedUserIds')?.updateValueAndValidity();
      }
    });
  }

  onCancel(): void {
    this.postForm.reset({ content: '', location: '', privacy: 'PUBLIC', allowedUserIds: [] });
    this.selectedImages = [];
    this.selectedVideos = [];
    this.selectedAudios = [];
    this.sanitizedUrls = []; // Xóa URL khi hủy
    this.selectedAllowedUsers = [];
    this.snackBar.open('Đã hủy tạo bài đăng.', 'Đóng', { duration: 2000 });
  }

  onSubmit(): void {
    this.postForm.markAllAsTouched();

    const contentValue = this.f['content'].value;
    const hasContent = contentValue && contentValue.trim().length > 0;
    const hasImages = this.selectedImages.length > 0;
    const hasVideos = this.selectedVideos.length > 0;
    const hasAudios = this.selectedAudios.length > 0;

    if (!hasContent && !hasImages && !hasVideos && !hasAudios) {
      console.log('Invalid form: No content and no media file.');
      this.snackBar.open('Please enter text or add photo/video/audio!', 'Close', {
        duration: 3000,
        panelClass: ['warning-snackbar'],
        horizontalPosition: 'center',
        verticalPosition: 'bottom',
      });
      return;
    }

    if (this.postForm.invalid) {
      this.snackBar.open('Please check your post information and privacy!', 'Close', {
        duration: 3000,
        panelClass: ['warning-snackbar'],
        horizontalPosition: 'center',
        verticalPosition: 'bottom',
      });
      return;
    }

    const postData: PostRequest = {
      content: this.f['content'].value || '',
      location: this.f['location'].value || '',
      privacy: this.f['privacy'].value ? this.f['privacy'].value.toString() : '',
      sharedWithUserIds: this.selectedAllowedUsers.map(u => u.id),
      tags: [] // Nếu bạn có trường tags, hãy lấy từ formControl tương ứng
    };

    console.log('DEBUG (Frontend): postData.privacy before sending:', postData.privacy);

    const formData = new FormData();
    formData.append('request', new Blob([JSON.stringify(postData)], { type: 'application/json' }));

    this.selectedImages.forEach((file) => {
      formData.append('images', file, file.name);
    });
    this.selectedVideos.forEach((file) => {
      formData.append('videos', file, file.name);
    });
    this.selectedAudios.forEach((file) => {
      formData.append('audios', file, file.name);
    });

    this.snackBar.open('Posting article...', 'Đóng', { duration: 3000 });

    this.postService.createPost(formData).subscribe({
      next: (response) => {
        console.log('Post created successfully:', response);
        this.snackBar.open('Post created successfully!', 'Đóng', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'bottom',
        });
        this.postForm.reset({ content: '', location: '', privacy: 'PUBLIC', allowedUserIds: [] });
        this.selectedImages = [];
        this.selectedVideos = [];
        this.selectedAudios = [];
        this.sanitizedUrls = [];
        this.selectedAllowedUsers = [];
        this.postCreated.emit();
      },
      error: (error) => {
        console.error('Error creating post:', error);
        let errorMessage = 'An error occurred while creating the post.';
        if (error.status === 403) {
          errorMessage = 'You do not have permission to post. Please log in or check permissions.';
        } else if (error.error && typeof error.error === 'object') {
          const validationErrors = Object.values(error.error).join('; ');
          errorMessage = `Authentication error: ${validationErrors}`;
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










// // src/app/core/home/create-post/create-post.component.ts
// import { Component, OnInit, Output, EventEmitter } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
//
// import { MatFormFieldModule } from '@angular/material/form-field';
// import { MatInputModule } from '@angular/material/input';
// import { MatIconModule } from '@angular/material/icon';
// import { MatButtonModule } from '@angular/material/button';
// import { MatCardModule } from '@angular/material/card';
// import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
// import { MatDividerModule } from '@angular/material/divider';
// import { MatSelectModule } from '@angular/material/select';
// import { MatChipsModule } from '@angular/material/chips';
// import { MatDialog } from '@angular/material/dialog';
//
// import { AuthService } from '../../../auth/services/auth.service';
// import { PostService } from '../services/post.service';
// import { UserResponse } from '../../../shared/models/user.model';
// import { PostRequest, PostResponse } from '../../../shared/models/post.model';
// import { UserSelectionDialogComponent } from '../../../shared/user-selection-dialog/user-selection-dialog.component';
//
// @Component({
//   selector: 'app-create-post',
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
//     MatDividerModule,
//     MatSelectModule,
//     MatChipsModule,
//   ],
//   templateUrl: './create-post.component.html',
//   styleUrls: ['./create-post.component.css'],
// })
// export class CreatePostComponent implements OnInit {
//   postForm!: FormGroup;
//   selectedImages: File[] = [];
//   selectedVideos: File[] = [];
//   selectedAudios: File[] = [];
//   currentUserEmail: string | null = null;
//   currentUserName: string = 'Bạn';
//
//   selectedAllowedUsers: UserResponse[] = [];
//
//   @Output() postCreated = new EventEmitter<void>();
//
//   constructor(
//     private postService: PostService,
//     private snackBar: MatSnackBar,
//     private authService: AuthService,
//     private dialog: MatDialog
//   ) {}
//
//   ngOnInit(): void {
//     this.postForm = new FormGroup({
//       content: new FormControl(''),
//       location: new FormControl(''),
//       privacy: new FormControl('PUBLIC', Validators.required),
//       allowedUserIds: new FormControl([]),
//     });
//
//     this.currentUserName = this.authService.getCurrentUserName() || 'Người dùng';
//
//     this.postForm.get('privacy')?.valueChanges.subscribe(privacy => {
//       const allowedUserIdsControl = this.postForm.get('allowedUserIds');
//       if (privacy === 'PRIVATE') {
//         allowedUserIdsControl?.markAsUntouched();
//         allowedUserIdsControl?.markAsPristine();
//       } else {
//         allowedUserIdsControl?.clearValidators();
//         allowedUserIdsControl?.setValue([]);
//         this.selectedAllowedUsers = [];
//       }
//       allowedUserIdsControl?.updateValueAndValidity();
//     });
//   }
//
//   get f() {
//     return this.postForm.controls;
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
//         this.selectedImages = [...this.selectedImages, ...newFiles];
//       } else if (type === 'videos') {
//         this.selectedVideos = [...this.selectedVideos, ...newFiles];
//       } else if (type === 'audios') {
//         this.selectedAudios = [...this.selectedAudios, ...newFiles];
//       }
//       console.log(`Files đã chọn (${type}):`, newFiles);
//     }
//     event.target.value = '';
//   }
//
//   removeSelectedFile(index: number, type: 'images' | 'videos' | 'audios'): void {
//     if (type === 'images') {
//       this.selectedImages.splice(index, 1);
//     } else if (type === 'videos') {
//       this.selectedVideos.splice(index, 1);
//     } else if (type === 'audios') {
//       this.selectedAudios.splice(index, 1);
//     }
//     console.log(`Đã xóa file (${type}) ở vị trí ${index}.`);
//   }
//
//   removeAllowedUser(userToRemove: UserResponse): void {
//     this.selectedAllowedUsers = this.selectedAllowedUsers.filter(user => user.id !== userToRemove.id);
//     this.postForm.get('allowedUserIds')?.setValue(this.selectedAllowedUsers.map(u => u.id));
//     this.postForm.get('allowedUserIds')?.markAsDirty();
//     this.postForm.get('allowedUserIds')?.updateValueAndValidity();
//   }
//
//   openUserSelectionDialog(): void {
//     const dialogRef = this.dialog.open(UserSelectionDialogComponent, {
//       width: '600px',
//       data: { selectedUsers: this.selectedAllowedUsers }
//     });
//
//     dialogRef.afterClosed().subscribe((result: UserResponse[] | undefined) => {
//       if (result) {
//         this.selectedAllowedUsers = result;
//         this.postForm.get('allowedUserIds')?.setValue(this.selectedAllowedUsers.map(u => u.id));
//         this.postForm.get('allowedUserIds')?.markAsDirty();
//         this.postForm.get('allowedUserIds')?.updateValueAndValidity();
//       }
//     });
//   }
//
//   onCancel(): void {
//     this.postForm.reset({ content: '', location: '', privacy: 'PUBLIC', allowedUserIds: [] });
//     this.selectedImages = [];
//     this.selectedVideos = [];
//     this.selectedAudios = [];
//     this.selectedAllowedUsers = [];
//     this.snackBar.open('Đã hủy tạo bài đăng.', 'Đóng', { duration: 2000 });
//   }
//
//   onSubmit(): void {
//     this.postForm.markAllAsTouched();
//
//     const contentValue = this.f['content'].value;
//     const hasContent = contentValue && contentValue.trim().length > 0;
//     const hasImages = this.selectedImages.length > 0;
//     const hasVideos = this.selectedVideos.length > 0;
//     const hasAudios = this.selectedAudios.length > 0;
//
//     if (!hasContent && !hasImages && !hasVideos && !hasAudios) {
//       console.log('Form không hợp lệ: Không có nội dung và không có file media.');
//       this.snackBar.open('Vui lòng nhập nội dung hoặc thêm ảnh/video/audio!', 'Đóng', {
//         duration: 3000,
//         panelClass: ['warning-snackbar'],
//         horizontalPosition: 'center',
//         verticalPosition: 'bottom',
//       });
//       return;
//     }
//
//     if (this.postForm.invalid) {
//       this.snackBar.open('Vui lòng kiểm tra lại thông tin bài viết và quyền riêng tư!', 'Đóng', {
//         duration: 3000,
//         panelClass: ['warning-snackbar'],
//         horizontalPosition: 'center',
//         verticalPosition: 'bottom',
//       });
//       return;
//     }
//
//     const postData: PostRequest = {
//       content: this.f['content'].value || '',
//       location: this.f['location'].value || '',
//       privacy: this.f['privacy'].value ? this.f['privacy'].value.toString() : '',
//       sharedWithUserIds: this.selectedAllowedUsers.map(u => u.id),
//       tags: [] // Nếu bạn có trường tags, hãy lấy từ formControl tương ứng
//     };
//
//     console.log('DEBUG (Frontend): postData.privacy before sending:', postData.privacy);
//
//     const formData = new FormData();
//
//     // Gửi postData dưới dạng JSON trong một part "request"
//     formData.append('request', new Blob([JSON.stringify(postData)], { type: 'application/json' }));
//
//     // Gửi các files
//     this.selectedImages.forEach((file) => {
//       formData.append('images', file, file.name);
//     });
//     this.selectedVideos.forEach((file) => {
//       formData.append('videos', file, file.name);
//     });
//     this.selectedAudios.forEach((file) => {
//       formData.append('audios', file, file.name);
//     });
//
//     this.snackBar.open('Đang đăng bài viết...', 'Đóng', { duration: 3000 });
//
//     this.postService.createPost(formData).subscribe({
//       next: (response) => {
//         console.log('Bài đăng đã được tạo thành công:', response);
//         this.snackBar.open('Bài đăng đã được tạo thành công!', 'Đóng', {
//           duration: 3000,
//           horizontalPosition: 'center',
//           verticalPosition: 'bottom',
//         });
//         this.postForm.reset({ content: '', location: '', privacy: 'PUBLIC', allowedUserIds: [] });
//         this.selectedImages = [];
//         this.selectedVideos = [];
//         this.selectedAudios = [];
//         this.selectedAllowedUsers = [];
//         this.postCreated.emit();
//       },
//       error: (error) => {
//         console.error('Lỗi khi tạo bài đăng:', error);
//         let errorMessage = 'Đã xảy ra lỗi khi tạo bài đăng.';
//         if (error.status === 403) {
//           errorMessage = 'Bạn không có quyền đăng bài. Vui lòng đăng nhập hoặc kiểm tra quyền.';
//         } else if (error.error && typeof error.error === 'object') {
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
//           horizontalPosition: 'center',
//           verticalPosition: 'bottom',
//         });
//       },
//     });
//   }
// }
