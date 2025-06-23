import { Component, OnInit } from '@angular/core';
import { MatCard, MatCardContent, MatCardHeader, MatCardTitle } from '@angular/material/card';
import { MatButton, MatMiniFabButton } from '@angular/material/button';
import {MatError, MatFormField, MatInput, MatLabel} from '@angular/material/input';
import { MatIcon } from '@angular/material/icon';
import { MatNativeDateModule } from '@angular/material/core';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';

import {ProfileRequest, ProfileResponse} from '../../../shared/models/profile.model';
import {NgIf} from '@angular/common';
import {UserService} from '../../services/user.service';
import {AuthService} from '../../services/auth.service';
import {TranslatePipe} from '@ngx-translate/core';
import {I18nService} from '../../../core/home/services/i18n.service';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-edit-profile',
  standalone: true,
  imports: [
    MatIcon,
    MatCard,
    MatCardHeader,
    MatCardContent,
    MatFormField,
    MatLabel,
    MatMiniFabButton,
    MatError,
    MatButton,
    MatInput,
    MatCardTitle,
    RouterLink,
    MatNativeDateModule,
    ReactiveFormsModule,
    NgIf,
    TranslatePipe,
  ],
  templateUrl: './edit-profile.component.html',
  styleUrl: './edit-profile.component.css'
})
export class EditProfileComponent implements OnInit {

  profileForm: FormGroup;
  currentUserEmail: string | null = null;
  isLoading: boolean = true;
  selectedAvatarFile: File | null = null;
  userProfile: ProfileResponse | null = null;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private router: Router,
    private authService: AuthService,
    private snackBar: MatSnackBar,// ✅ Thêm dòng này
    public i18n: I18nService // ✅ Dùng service bạn đã tạo


  ) {
    this.profileForm = this.fb.group({
      name: ['', Validators.required],
      email: [{ value: '', disabled: true }, [Validators.required, Validators.email]],
      phoneNumber: ['', [Validators.required, Validators.pattern('^[0-9+\\-\\s()]{10,15}$')]],
      bio: ['']
    });
  }

  ngOnInit(): void {
    this.loadUserProfileForEdit();
  }

  loadUserProfileForEdit(): void {
    this.isLoading = true;
    // ĐÃ SỬA LỖI: Bỏ tham số khỏi getCurrentUserProfile()
    this.userService.getCurrentUserProfile().subscribe({
      next: (profile: ProfileResponse) => {
        this.userProfile = profile;
        this.currentUserEmail = profile.email;
        this.profileForm.patchValue({
          name: profile.name,
          email: profile.email,
          phoneNumber: profile.phoneNumber,
          bio: profile.bio
        });
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Error loading user profile for edit:', err);
        this.isLoading = false;
        this.snackBar.open(
          this.i18n.instant('EDIT-PROFILE_SNACKBAR.LOAD_ERROR'),
          'Đóng',
          { duration: 4000, panelClass: ['snackbar-error'] }
        );


      }
    });
  }

  onAvatarSelected(event: Event): void {
    const element = event.currentTarget as HTMLInputElement;
    let fileList: FileList | null = element.files;
    if (fileList && fileList.length > 0) {
      const file = fileList[0];
      if (file.type.startsWith('image/')) {
        this.selectedAvatarFile = file;
        console.log('Selected avatar file:', this.selectedAvatarFile.name);

        const reader = new FileReader();
        reader.onload = (e: any) => {
          if (this.userProfile) {
            this.userProfile.avatarUrl = e.target.result;
          }
        };
        reader.readAsDataURL(file);

      } else {
        this.selectedAvatarFile = null;
        this.snackBar.open(
          this.i18n.instant('EDIT-PROFILE_SNACKBAR.AVATAR_INVALID'),
          'Đóng',
          { duration: 3000, panelClass: ['snackbar-error'] }
        );

      }
    } else {
      this.selectedAvatarFile = null;
    }
  }

  onSave(): void {
    if (this.profileForm.valid && this.currentUserEmail) {
      const formValue = this.profileForm.getRawValue();

      const request: ProfileRequest = {
        name: formValue.name,
        phoneNumber: formValue.phoneNumber,
        bio: formValue.bio
      };

      this.isLoading = true;
      this.userService.updateUserProfile(this.currentUserEmail, request).subscribe({
        next: (updatedProfile: ProfileResponse) => {
          console.log('Profile updated successfully (text fields):', updatedProfile);
          this.userProfile = updatedProfile;
          this.snackBar.open(
            this.i18n.instant('EDIT-PROFILE_SNACKBAR.SAVE_SUCCESS'),
            'Đóng',
            { duration: 3000, panelClass: ['snackbar-success'] }
          );

          this.isLoading = false;
        },
        error: (err: any) => {
          console.error('Error updating profile (text fields):', err);
          this.isLoading = false;
          this.snackBar.open(
            this.i18n.instant('EDIT-PROFILE_SNACKBAR.SAVE_ERROR', {
              message: err.error?.message || 'Có lỗi xảy ra.'
            }),
            'Đóng',
            { duration: 4000, panelClass: ['snackbar-error'] }
          );


        }
      });
    } else {
      console.warn('Form is invalid or currentUserEmail is missing. Cannot save text fields.');
      this.snackBar.open(
        this.i18n.instant('EDIT-PROFILE_SNACKBAR.FORM_INVALID'),
        'Đóng',
        { duration: 3000, panelClass: ['snackbar-error'] }
      );


    }
  }

  onUploadAvatar(): void {
    if (this.selectedAvatarFile && this.currentUserEmail) {
      this.isLoading = true;
      // ĐÃ SỬA LỖI: Gọi đúng phương thức uploadUserAvatar từ UserService
      this.userService.uploadUserAvatar(this.currentUserEmail, this.selectedAvatarFile).subscribe({
        next: (updatedProfile: ProfileResponse) => {
          console.log('Avatar uploaded successfully:', updatedProfile);
          this.userProfile = updatedProfile;
          this.authService.updateCurrentUserAvatar(updatedProfile.avatarUrl || null);

          this.selectedAvatarFile = null;
          this.snackBar.open(
            this.i18n.instant('EDIT-PROFILE_SNACKBAR.AVATAR_SUCCESS'),
            'Đóng',
            { duration: 3000, panelClass: ['snackbar-success'] }
          );

          this.isLoading = false;
        },
        error: (err: any) => {
          console.error('Error uploading avatar:', err);
          this.isLoading = false;
          this.snackBar.open(
            this.i18n.instant('EDIT-PROFILE_SNACKBAR.AVATAR_ERROR', {
              message: err.error?.message || 'Có lỗi xảy ra.'
            }),
            'Đóng',
            { duration: 4000, panelClass: ['snackbar-error'] }
          );

        }
      });
    } else {
      console.warn('No avatar file selected or currentUserEmail is missing. Cannot upload avatar.');
      this.snackBar.open(
        this.i18n.instant('EDIT-PROFILE_SNACKBAR.AVATAR_REQUIRED'),
        'Đóng',
        { duration: 3000, panelClass: ['snackbar-error'] }
      );


    }
  }
}
