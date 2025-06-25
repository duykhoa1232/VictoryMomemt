




import { Component, OnInit, Output, EventEmitter, Inject } from '@angular/core';
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
import { MatDialogRef, MAT_DIALOG_DATA, MatDialog } from '@angular/material/dialog';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

import { AuthService } from '../../../auth/services/auth.service';
import { PostService } from '../services/post.service';
import { UserResponse } from '../../../shared/models/profile.model';
import { PostRequest, PostResponse } from '../../../shared/models/post.model';
import { UserSelectionDialogComponent } from '../../../shared/user-selection-dialog/user-selection-dialog.component';
import { environment } from '../../../../environments/environment';
import { TranslatePipe } from '@ngx-translate/core';
import { I18nService } from '../services/i18n.service';

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
    TranslatePipe,
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
  sanitizedUrls: SafeUrl[] = [];

  selectedAllowedUsers: UserResponse[] = [];
  @Output() postCreated = new EventEmitter<void>();

  constructor(
    private postService: PostService,
    private snackBar: MatSnackBar,
    private authService: AuthService,
    public dialogRef: MatDialogRef<CreatePostComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private sanitizer: DomSanitizer,
    private dialog: MatDialog,
    private i18n: I18nService
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
          fileTypeDisplayName = this.i18n.instant('CREATE_POST.IMAGE');
          break;
        case 'videos':
          maxFileSize = MAX_VIDEO_SIZE_MB * 1024 * 1024;
          fileTypeDisplayName = this.i18n.instant('CREATE_POST.VIDEO');
          break;
        case 'audios':
          maxFileSize = MAX_AUDIO_SIZE_MB * 1024 * 1024;
          fileTypeDisplayName = this.i18n.instant('CREATE_POST.AUDIO');
          break;
      }

      const newFiles: File[] = [];
      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        if (file.size > maxFileSize) {
          this.snackBar.open(
            this.i18n.instant('CREATE_POST_SNACKBAR.FILE_TOO_LARGE', { fileName: file.name, type: fileTypeDisplayName, maxSize: maxFileSize / (1024 * 1024) }),
            'Đóng',
            { duration: 3000, panelClass: ['error-snackbar'] }
          );
          continue;
        }
        newFiles.push(file);
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
      this.sanitizedUrls.splice(index, 1);
    } else if (type === 'videos') {
      this.selectedVideos.splice(index, 1);
      this.sanitizedUrls.splice(index + this.selectedImages.length, 1);
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
    this.dialogRef.close();
    this.postForm.reset({ content: '', location: '', privacy: 'PUBLIC', allowedUserIds: [] });
    this.selectedImages = [];
    this.selectedVideos = [];
    this.selectedAudios = [];
    this.sanitizedUrls = [];
    this.selectedAllowedUsers = [];
    this.snackBar.open(
      this.i18n.instant('CREATE_POST_SNACKBAR.CANCELLED'),
      'Đóng',
      { duration: 3000, panelClass: ['error-snackbar'] }
    );
  }

  onSubmit(): void {
    this.postForm.markAllAsTouched();

    const contentValue = this.f['content'].value;
    const hasContent = contentValue && contentValue.trim().length > 0;
    const hasImages = this.selectedImages.length > 0;
    const hasVideos = this.selectedVideos.length > 0;
    const hasAudios = this.selectedAudios.length > 0;

    if (!hasContent && !hasImages && !hasVideos && !hasAudios) {
      this.snackBar.open(
        this.i18n.instant('CREATE_POST_SNACKBAR.MISSING_CONTENT'),
        'Đóng',
        { duration: 3000, panelClass: ['error-snackbar'] }
      );
      return;
    }

    if (this.postForm.invalid) {
      this.snackBar.open(
        this.i18n.instant('CREATE_POST_SNACKBAR.INVALID_FORM'),
        'Đóng',
        { duration: 3000, panelClass: ['error-snackbar'] }
      );
      return;
    }

    const postData: PostRequest = {
      content: this.f['content'].value || '',
      location: this.f['location'].value || '',
      privacy: this.f['privacy'].value ? this.f['privacy'].value.toString() : '',
      sharedWithUserIds: this.selectedAllowedUsers.map(u => u.id),
      tags: []
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

    this.snackBar.open(
      this.i18n.instant('CREATE_POST_SNACKBAR.POSTING'),
      'Đóng',
      { duration: 3000, panelClass: ['error-snackbar'] }
    );

    this.postService.createPost(formData).subscribe({
      next: (response) => {
        console.log('Post created successfully:', response);
        this.snackBar.open(
          this.i18n.instant('CREATE_POST_SNACKBAR.POST_SUCCESS'),
          'Đóng',
          { duration: 3000, panelClass: ['error-snackbar'] }
        );
        this.dialogRef.close(true);
        if (this.data?.postCreated) {
          this.data.postCreated();
        }
      },
      error: (error) => {
        console.error('Error creating post:', error);
        let errorMessage = this.i18n.instant('CREATE_POST_SNACKBAR.POST_FAILED');
        if (error.status === 403) {
          errorMessage = this.i18n.instant('CREATE_POST_SNACKBAR.NO_PERMISSION');
        } else if (error.error && typeof error.error === 'object') {
          const validationErrors = Object.values(error.error).join('; ');
          errorMessage = `Authentication error: ${validationErrors}`;
        } else if (error.error && error.error.message) {
          errorMessage = error.error.message;
        } else if (error.message) {
          errorMessage = error.message;
        }
        this.snackBar.open(
          errorMessage,
          'Đóng',
          { duration: 5000, panelClass: ['error-snackbar'] }
        );
      },
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
            this.i18n.instant('CREATE_POST_SNACKBAR.FETCH_LOCATION_FAIL'),
            'Đóng',
            { duration: 3000, panelClass: ['error-snackbar'] }
          );
        }
      );
    } else {
      this.snackBar.open(
        this.i18n.instant('CREATE_POST_SNACKBAR.BROWSER_UNSUPPORTED'),
        'Đóng',
        { duration: 3000, panelClass: ['error-snackbar'] }
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

        const street = [houseNumber, road].filter(part => part).join(' ');
        const formattedAddress = [street, ward, city].filter(part => part).join(', ');

        this.postForm.get('location')?.setValue(formattedAddress);

        this.snackBar.open(
          this.i18n.instant('CREATE_POST_SNACKBAR.LOCATION_FILLED'),
          'Đóng',
          { duration: 3000, panelClass: ['error-snackbar'] }
        );
      })
      .catch(error => {
        console.error('Lỗi khi gọi Nominatim:', error);
        this.snackBar.open(
          this.i18n.instant('CREATE_POST_SNACKBAR.COORDINATE_FAIL'),
          'Đóng',
          { duration: 3000, panelClass: ['error-snackbar'] }
        );
      });
  }
}
