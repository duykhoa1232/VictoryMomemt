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

import { PostResponse } from '../../../shared/models/post.model'; // Import PostResponse từ shared

// Định nghĩa interface cho dữ liệu truyền vào dialog
export interface EditPostDialogData {
  post: PostResponse; // Bài đăng hiện tại cần chỉnh sửa (Sửa từ Post sang PostResponse)
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
    MatDialogActions
  ],
  templateUrl: './edit-post-dialog.component.html',
  styleUrls: ['./edit-post-dialog.component.css']
})
export class EditPostDialogComponent implements OnInit {
  editForm!: FormGroup;
  selectedNewImages: File[] = [];
  selectedNewVideos: File[] = [];
  selectedNewAudios: File[] = [];

  constructor(
    public dialogRef: MatDialogRef<EditPostDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: EditPostDialogData,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.editForm = new FormGroup({
      content: new FormControl(this.data.post.content || ''),
      location: new FormControl(this.data.post.location || ''),
    });
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
        this.selectedNewImages = [...this.selectedNewImages, ...newFiles];
      } else if (type === 'videos') {
        this.selectedNewVideos = [...this.selectedNewVideos, ...newFiles];
      } else if (type === 'audios') {
        this.selectedNewAudios = [...this.selectedNewAudios, ...newFiles];
      }
    }
    event.target.value = '';
  }

  removeSelectedNewFile(index: number, type: 'images' | 'videos' | 'audios'): void {
    if (type === 'images') {
      this.selectedNewImages.splice(index, 1);
    } else if (type === 'videos') {
      this.selectedNewVideos.splice(index, 1);
    } else if (type === 'audios') {
      this.selectedNewAudios.splice(index, 1);
    }
  }

  onCancel(): void {
    this.dialogRef.close(null);
  }

  onSave(): void {
    this.editForm.markAllAsTouched();

    const updatedContent = this.editForm.get('content')?.value;
    const updatedLocation = this.editForm.get('location')?.value;

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
      this.dialogRef.close(null);
      return;
    }

    const formData = new FormData();
    formData.append('content', updatedContent || '');
    formData.append('location', updatedLocation || '');

    this.selectedNewImages.forEach(file => {
      formData.append('images', file, file.name);
    });
    this.selectedNewVideos.forEach(file => {
      formData.append('videos', file, file.name);
    });
    this.selectedNewAudios.forEach(file => {
      formData.append('audios', file, file.name);
    });

    this.dialogRef.close({
      formData: formData,
      deletedMediaUrls: { images: [], videos: [], audios: [] }
    });
  }
}
