
import { Component, OnInit, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { PostResponse, PostRequest } from '../../../shared/models/post.model';
import { UserResponse } from '../../../shared/models/profile.model';
import { UserSelectionDialogComponent } from '../../../shared/user-selection-dialog/user-selection-dialog.component';
import {I18nService} from '../services/i18n.service';
import {TranslatePipe} from '@ngx-translate/core';

export interface EditPostDialogData {
  post: PostResponse;
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
    MatChipsModule,
    TranslatePipe
  ],
  templateUrl: './edit-post-dialog.component.html',
  styleUrls: ['./edit-post-dialog.component.css']
})
export class EditPostDialogComponent implements OnInit {
  editForm!: FormGroup;
  selectedNewImages: File[] = [];
  selectedNewVideos: File[] = [];
  selectedNewAudios: File[] = [];
  selectedAllowedUsers: UserResponse[] = [];
  deletedImageUrls: string[] = [];
  deletedVideoUrls: string[] = [];
  deletedAudioUrls: string[] = [];

  constructor(
    public dialogRef: MatDialogRef<EditPostDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: EditPostDialogData,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private i18n: I18nService,

  ) {}

  ngOnInit(): void {
    this.editForm = new FormGroup({
      content: new FormControl(this.data.post.content || ''),
      location: new FormControl(this.data.post.location || ''),
      privacy: new FormControl(this.data.post.visibilityStatus, Validators.required),
      allowedUserIds: new FormControl(this.data.post.authorizedViewerIds || []),
    });

    if (this.data.post.visibilityStatus === 'PRIVATE' && this.data.post.authorizedViewerIds && this.data.post.authorizedViewerIds.length > 0) {
      if (this.data.post.author && this.data.post.authorizedViewerIds.includes(this.data.post.author.id)) {
        this.selectedAllowedUsers.push(this.data.post.author);
      }
    }

    this.editForm.get('privacy')?.valueChanges.subscribe(privacy => {
      const allowedUserIdsControl = this.editForm.get('allowedUserIds');
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
            this.i18n.instant('EDIT_POST_SNACKBAR.FILE_TOO_LARGE', {
              fileName: file.name,
              type: fileTypeDisplayName,
              maxSize: maxFileSize / (1024 * 1024),
            }),
            'Đóng',
            { duration: 5000, panelClass: ['warning-snackbar'] }
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

  removeOldMediaUrl(url: string, type: 'images' | 'videos' | 'audios'): void {
    if (type === 'images') {
      this.data.post.imageUrls = this.data.post.imageUrls?.filter(u => u !== url) || [];
      this.deletedImageUrls.push(url);
    } else if (type === 'videos') {
      this.data.post.videoUrls = this.data.post.videoUrls?.filter(u => u !== url) || [];
      this.deletedVideoUrls.push(url);
    } else if (type === 'audios') {
      this.data.post.audioUrls = this.data.post.audioUrls?.filter(u => u !== url) || [];
      this.deletedAudioUrls.push(url);
    }
    this.snackBar.open(
      this.i18n.instant('EDIT_POST_SNACKBAR.MEDIA_MARKED_FOR_DELETE', {
        mediaType: type === 'images' ? 'ảnh' : type === 'videos' ? 'video' : 'âm thanh',
      }),
      'Đóng',
      { duration: 2000 }
    );

  }

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
  getCurrentLocation(): void {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        position => {
          const lat = position.coords.latitude;
          const lon = position.coords.longitude;
          this.fetchAddressFromCoordinates(lat, lon);
        },
        error => {
          console.error('Lỗi khi lấy vị trí:', error);
          this.snackBar.open(
            this.i18n.instant('EDIT_POST_SNACKBAR.LOCATION_FETCH_FAIL'),
            'Đóng',
            { duration: 3000 }
          );
        }
      );
    } else {
      this.snackBar.open(
        this.i18n.instant('EDIT_POST_SNACKBAR.BROWSER_UNSUPPORTED'),
        'Đóng',
        { duration: 3000 }
      );

    }
  }

  fetchAddressFromCoordinates(lat: number, lon: number): void {
    const url = `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json&addressdetails=1`;

    fetch(url)
      .then(response => response.json())
      .then(data => {
        const address = data.address;

        const houseNumber = address.house_number || '';
        const road = address.road || '';
        const ward = address.suburb || address.neighbourhood || address.city_district || '';
        const city = address.city || address.town || address.village || address.county || '';

        // Gộp số nhà và đường
        const street = [houseNumber, road].filter(part => part).join(' ');

        // Gộp địa chỉ đầy đủ
        const formattedAddress = [street, ward, city].filter(part => part).join(', ');

        this.editForm.get('location')?.setValue(formattedAddress);

        this.snackBar.open(
          this.i18n.instant('EDIT_POST_SNACKBAR.ADDRESS_AUTO_FILLED'),
          'Đóng',
          { duration: 2000 }
        );
      })
      .catch(error => {
        console.error('Lỗi khi gọi Nominatim:', error);
        this.snackBar.open(
          this.i18n.instant('EDIT_POST_SNACKBAR.COORDINATE_FAIL'),
          'Đóng',
          { duration: 3000 }
        );

      });
  }



  onCancel(): void {
    this.dialogRef.close(null);
  }

  onSave(): void {
    this.editForm.markAllAsTouched();

    if (this.editForm.invalid) {
      this.snackBar.open(
        this.i18n.instant('EDIT_POST_SNACKBAR.INVALID_FORM'),
        'Đóng',
        { duration: 3000, panelClass: ['warning-snackbar'] }
      );

      return;
    }

    const updatedContent = this.editForm.get('content')?.value;
    const updatedLocation = this.editForm.get('location')?.value;
    const updatedPrivacy = this.editForm.get('privacy')?.value;
    const updatedAllowedUserIds = this.editForm.get('allowedUserIds')?.value;

    const postData: PostRequest = {
      content: updatedContent || '',
      location: updatedLocation || '',
      privacy: updatedPrivacy ? updatedPrivacy.toString() : '',
      sharedWithUserIds: updatedAllowedUserIds
    };

    const formData = new FormData();
    formData.append('request', new Blob([JSON.stringify(postData)], { type: 'application/json' }));

    this.selectedNewImages.forEach(file => {
      formData.append('images', file, file.name);
    });
    this.selectedNewVideos.forEach(file => {
      formData.append('videos', file, file.name);
    });
    this.selectedNewAudios.forEach(file => {
      formData.append('audios', file, file.name);
    });

    const deletedMediaUrls = {
      images: this.deletedImageUrls,
      videos: this.deletedVideoUrls,
      audios: this.deletedAudioUrls
    };
    formData.append('deletedMediaUrls', new Blob([JSON.stringify(deletedMediaUrls)], { type: 'application/json' }));

    this.dialogRef.close({
      formData: formData,
      deletedMediaUrls: deletedMediaUrls
    });
  }

}
