// src/app/core/home/edit-post-dialog/edit-post-dialog.component.ts
import { Component, OnInit, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';

// Angular Material Modules
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips'; // Import MatChipsModule
import { MatDialog } from '@angular/material/dialog'; // Import MatDialog

import { PostResponse, PostRequest } from '../../../shared/models/post.model'; // Import PostResponse và PostRequest
import { UserResponse } from '../../../shared/models/user.model'; // Import UserResponse
import { UserSelectionDialogComponent } from '../../../shared/user-selection-dialog/user-selection-dialog.component'; // Import UserSelectionDialogComponent

// Định nghĩa interface cho dữ liệu truyền vào dialog
export interface EditPostDialogData {
  post: PostResponse; // Bài đăng hiện tại cần chỉnh sửa
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
    MatCardModule,
    MatSnackBarModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatSelectModule,
    MatChipsModule // Thêm MatChipsModule
  ],
  templateUrl: './edit-post-dialog.component.html',
  styleUrls: ['./edit-post-dialog.component.css']
})
export class EditPostDialogComponent implements OnInit {
  editForm!: FormGroup;
  selectedNewImages: File[] = [];
  selectedNewVideos: File[] = [];
  selectedNewAudios: File[] = [];
  selectedAllowedUsers: UserResponse[] = []; // Để lưu người dùng được phép xem

  constructor(
    public dialogRef: MatDialogRef<EditPostDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: EditPostDialogData,
    private snackBar: MatSnackBar,
    private dialog: MatDialog // Inject MatDialog
  ) {}

  ngOnInit(): void {
    // Khởi tạo form với dữ liệu hiện có của bài đăng
    this.editForm = new FormGroup({
      content: new FormControl(this.data.post.content || '', Validators.required),
      location: new FormControl(this.data.post.location || ''),
      privacy: new FormControl(this.data.post.visibilityStatus, Validators.required), // Sửa từ visibility sang privacy
      allowedUserIds: new FormControl(this.data.post.authorizedViewerIds || []), // Khởi tạo với IDs hiện có
    });

    // Nếu có authorizedViewerIds, cần tải thông tin UserResponse của họ
    if (this.data.post.visibilityStatus === 'PRIVATE' && this.data.post.authorizedViewerIds && this.data.post.authorizedViewerIds.length > 0) {
      // Logic này sẽ cần UserService hoặc PostService để lấy thông tin User từ ID
      // Hiện tại, chúng ta chỉ có ID, nên có thể cần thêm một service method để lấy UserResponse[] từ ID
      // Tạm thời, giả định bạn có thể có một cách để lấy chúng. Hoặc bạn có thể chỉ hiển thị ID.
      // Để đơn giản, nếu PostResponse đã có đủ author, thì có thể dùng cách dưới
      // Nếu bạn muốn hiển thị tên người dùng đã có sẵn trong PostResponse, hãy sử dụng nó:
      if (this.data.post.authorizedViewerIds && this.data.post.author) {
        // Đây là một giả định, vì PostResponse chỉ có một author.
        // Nếu authorizedViewerIds là nhiều người, bạn cần gọi API để lấy thông tin của từng người.
        // Để demo, giả định chỉ thêm author nếu author nằm trong authorizedViewerIds
        const ownerId = this.data.post.author.id;
        if (this.data.post.authorizedViewerIds.includes(ownerId)) {
          this.selectedAllowedUsers.push(this.data.post.author);
        }
        // Nếu có nhiều người khác được chia sẻ, bạn sẽ cần gọi API để lấy chi tiết của họ
      }
    }

    this.editForm.get('privacy')?.valueChanges.subscribe(privacy => {
      const allowedUserIdsControl = this.editForm.get('allowedUserIds');
      if (privacy === 'PRIVATE') {
        // KHÔNG ĐẶT Validators.required Ở ĐÂY NỮA
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

  // Phương thức xử lý khi người dùng chọn file media mới
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
        this.selectedNewImages = [...this.selectedNewImages, ...newFiles];
      } else if (type === 'videos') {
        this.selectedNewVideos = [...this.selectedNewVideos, ...newFiles];
      } else if (type === 'audios') {
        this.selectedNewAudios = [...this.selectedNewAudios, ...newFiles];
      }
    }
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

  // Hàm để xóa URL media cũ (nếu muốn xóa ảnh cũ khỏi bài đăng)
  // Bạn sẽ cần UI để cho phép người dùng chọn xóa ảnh/video/audio cũ
  // và truyền các URL đó vào mảng deletedMediaUrls để gửi lên backend
  removeOldMediaUrl(url: string, type: 'images' | 'videos' | 'audios'): void {
    // Logic để loại bỏ URL khỏi mảng this.data.post.imageUrls/videoUrls/audioUrls
    // và thêm vào mảng deletedMediaUrls (cần tạo một mảng này trong component và gửi lên)
    this.snackBar.open(`Chức năng xóa media cũ chưa được triển khai đầy đủ.`, 'Đóng', {duration: 2000});
  }

  // THÊM: Logic cho phần chọn người dùng cho bài Private (giống create-post.component.ts)
  removeAllowedUser(userToRemove: UserResponse): void {
    this.selectedAllowedUsers = this.selectedAllowedUsers.filter(user => user.id !== userToRemove.id);
    this.editForm.get('allowedUserIds')?.setValue(this.selectedAllowedUsers.map(u => u.id));
    this.editForm.get('allowedUserIds')?.markAsDirty();
    this.editForm.get('allowedUserIds')?.updateValueAndValidity();
  }

  openUserSelectionDialog(): void {
    const dialogRef = this.dialog.open(UserSelectionDialogComponent, {
      width: '600px',
      data: { selectedUsers: this.selectedAllowedUsers }
    });

    dialogRef.afterClosed().subscribe((result: UserResponse[] | undefined) => {
      if (result) {
        this.selectedAllowedUsers = result;
        this.editForm.get('allowedUserIds')?.setValue(this.selectedAllowedUsers.map(u => u.id));
        this.editForm.get('allowedUserIds')?.markAsDirty();
        this.editForm.get('allowedUserIds')?.updateValueAndValidity();
      }
    });
  }
  // KẾT THÚC THÊM LOGIC CHỌN NGƯỜI DÙNG

  // Phương thức khi người dùng nhấp vào nút "Hủy"
  onCancel(): void {
    this.dialogRef.close(null); // Đóng dialog và trả về null (không có thay đổi)
  }

  // Phương thức khi người dùng nhấp vào nút "Lưu"
  onSave(): void {
    this.editForm.markAllAsTouched();

    if (this.editForm.invalid) {
      this.snackBar.open('Vui lòng kiểm tra lại thông tin bài viết và quyền riêng tư!', 'Đóng', {
        duration: 3000,
        panelClass: ['warning-snackbar'],
      });
      return;
    }

    const updatedContent = this.editForm.get('content')?.value;
    const updatedLocation = this.editForm.get('location')?.value;
    const updatedPrivacy = this.editForm.get('privacy')?.value; // Lấy giá trị privacy từ form
    const updatedAllowedUserIds = this.editForm.get('allowedUserIds')?.value; // Lấy ID người dùng được phép xem

    // Tạo PostRequest DTO object
    const postData: PostRequest = {
      content: updatedContent || '',
      location: updatedLocation || '',
      privacy: updatedPrivacy ? updatedPrivacy.toString() : '', // Đảm bảo là string
      sharedWithUserIds: updatedAllowedUserIds // Gán mảng ID người dùng
      // tags: [] // Thêm nếu bạn có trường tags và cần cập nhật
    };

    const formData = new FormData();
    // Gửi postData dưới dạng JSON trong một part "request"
    formData.append('request', new Blob([JSON.stringify(postData)], { type: 'application/json' }));

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

    // TODO: Nếu bạn muốn hỗ trợ xóa media cũ, bạn cần thêm logic vào đây
    // để thu thập các URL media cũ đã bị xóa và gửi chúng lên backend
    // Ví dụ: const deletedMediaUrls = { images: [], videos: [], audios: [] };
    // và truyền nó vào dialogRef.close({ formData: formData, deletedMediaUrls: deletedMediaUrls });


    this.dialogRef.close({
      formData: formData,
      deletedMediaUrls: { images: [], videos: [], audios: [] } // Trả về rỗng vì dialog này không xử lý xóa media cũ
    });
  }
}
