import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';

// Angular Material Modules
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips'; // THÊM: MatChipsModule
import { MatDialog } from '@angular/material/dialog'; // THÊM: MatDialog

import { AuthService } from '../../../auth/services/auth.service';
import { PostService, User } from '../services/post.service';
import {UserSelectionDialogComponent} from '../../../shared/user-selection-dialog/user-selection-dialog.component'; // ĐÃ CẬP NHẬT: Import User từ PostService

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
    MatChipsModule, // THÊM
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

  selectedAllowedUsers: User[] = []; // THÊM: Mảng chứa người dùng được phép xem

  @Output() postCreated = new EventEmitter<void>();

  constructor(
    private postService: PostService,
    private snackBar: MatSnackBar,
    private authService: AuthService,
    private dialog: MatDialog // THÊM: Inject MatDialog
  ) {}

  ngOnInit(): void {
    this.postForm = new FormGroup({
      content: new FormControl('', Validators.required),
      location: new FormControl(''),
      privacy: new FormControl('PUBLIC', Validators.required), // Mặc định là PUBLIC
      // allowedUserIdsInput: new FormControl(''), // ĐÃ XÓA: Không còn dùng input thủ công
      allowedUserIds: new FormControl([], Validators.required), // THÊM: FormControl cho mảng ID người dùng
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

    this.postForm.get('privacy')?.valueChanges.subscribe(privacy => {
      const allowedUserIdsControl = this.postForm.get('allowedUserIds');
      if (privacy === 'PRIVATE') { // ĐÃ CẬP NHẬT: Áp dụng validator khi privacy là PRIVATE
        allowedUserIdsControl?.setValidators(Validators.required); // Vẫn bắt buộc nếu PRIVATE
        allowedUserIdsControl?.markAsUntouched();
        allowedUserIdsControl?.markAsPristine();
      } else {
        allowedUserIdsControl?.clearValidators();
        allowedUserIdsControl?.setValue([]); // Xóa giá trị khi không phải PRIVATE
        this.selectedAllowedUsers = []; // Xóa danh sách người dùng đã chọn
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
    } else if (type === 'videos') {
      this.selectedVideos.splice(index, 1);
    } else if (type === 'audios') {
      this.selectedAudios.splice(index, 1);
    }
    console.log(`Đã xóa file (${type}) ở vị trí ${index}.`);
  }

  removeAllowedUser(userToRemove: User): void { // ĐÃ CẬP NHẬT: Sử dụng User interface
    this.selectedAllowedUsers = this.selectedAllowedUsers.filter(user => user.id !== userToRemove.id);
    this.postForm.get('allowedUserIds')?.setValue(this.selectedAllowedUsers.map(u => u.id));
    this.postForm.get('allowedUserIds')?.markAsDirty(); // Đánh dấu là đã thay đổi
    this.postForm.get('allowedUserIds')?.updateValueAndValidity();
  }

  openUserSelectionDialog(): void {
    const dialogRef = this.dialog.open(UserSelectionDialogComponent, {
      width: '600px',
      data: { selectedUsers: this.selectedAllowedUsers } // Truyền người dùng đã chọn hiện tại
    });

    dialogRef.afterClosed().subscribe((result: User[] | undefined) => { // ĐÃ CẬP NHẬT: Sử dụng User interface
      if (result) {
        this.selectedAllowedUsers = result;
        this.postForm.get('allowedUserIds')?.setValue(this.selectedAllowedUsers.map(u => u.id));
        this.postForm.get('allowedUserIds')?.markAsDirty(); // Đánh dấu là đã thay đổi
        this.postForm.get('allowedUserIds')?.updateValueAndValidity();
      }
    });
  }

  onCancel(): void {
    this.postForm.reset({ content: '', location: '', privacy: 'PUBLIC', allowedUserIds: [] });
    this.selectedImages = [];
    this.selectedVideos = [];
    this.selectedAudios = [];
    this.selectedAllowedUsers = []; // Reset người dùng được phép xem
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
      console.log('Form không hợp lệ: Không có nội dung và không có file media.');
      this.snackBar.open('Vui lòng nhập nội dung hoặc thêm ảnh/video/audio!', 'Đóng', {
        duration: 3000,
        panelClass: ['warning-snackbar'],
        horizontalPosition: 'center',
        verticalPosition: 'bottom',
      });
      return;
    }

    if (this.postForm.invalid) {
      this.snackBar.open('Vui lòng kiểm tra lại thông tin bài viết và quyền riêng tư!', 'Đóng', {
        duration: 3000,
        panelClass: ['warning-snackbar'],
        horizontalPosition: 'center',
        verticalPosition: 'bottom',
      });
      return;
    }

    const postData = this.postForm.value;
    const formData = new FormData();
    formData.append('content', postData.content || '');
    formData.append('location', postData.location || '');
    formData.append('privacy', postData.privacy);

    // Gửi allowedUserIds khi privacy là PRIVATE
    if (postData.privacy === 'PRIVATE') {
      // Đảm bảo allowedUserIds là một mảng các ID string
      const allowedUserIds = this.selectedAllowedUsers.map(user => user.id);
      formData.append('allowedUserIds', JSON.stringify(allowedUserIds));
    }

    this.selectedImages.forEach((file) => {
      formData.append('images', file, file.name);
    });
    this.selectedVideos.forEach((file) => {
      formData.append('videos', file, file.name);
    });
    this.selectedAudios.forEach((file) => {
      formData.append('audios', file, file.name);
    });

    this.snackBar.open('Đang đăng bài viết...', 'Đóng', { duration: 3000 });

    this.postService.createPost(formData).subscribe({
      next: (response) => {
        console.log('Bài đăng đã được tạo thành công:', response);
        this.snackBar.open('Bài đăng đã được tạo thành công!', 'Đóng', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'bottom',
        });
        this.postForm.reset({ content: '', location: '', privacy: 'PUBLIC', allowedUserIds: [] });
        this.selectedImages = [];
        this.selectedVideos = [];
        this.selectedAudios = [];
        this.selectedAllowedUsers = []; // Reset người dùng được phép xem
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
