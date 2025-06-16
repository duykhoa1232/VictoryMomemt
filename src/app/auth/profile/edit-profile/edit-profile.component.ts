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
    private router: Router
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
        alert('Không thể tải thông tin hồ sơ. Vui lòng kiểm tra lại đăng nhập hoặc thử lại sau.');
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
        alert('Vui lòng chọn một file ảnh hợp lệ.');
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
          alert('Cập nhật thông tin hồ sơ thành công!');
          this.isLoading = false;
        },
        error: (err: any) => {
          console.error('Error updating profile (text fields):', err);
          this.isLoading = false;
          alert('Cập nhật thông tin hồ sơ thất bại: ' + (err.error?.message || 'Có lỗi xảy ra.'));
        }
      });
    } else {
      console.warn('Form is invalid or currentUserEmail is missing. Cannot save text fields.');
      alert('Vui lòng điền đầy đủ và đúng thông tin.');
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
          this.selectedAvatarFile = null;
          alert('Cập nhật ảnh đại diện thành công!');
          this.isLoading = false;
        },
        error: (err: any) => {
          console.error('Error uploading avatar:', err);
          this.isLoading = false;
          alert('Cập nhật ảnh đại diện thất bại: ' + (err.error?.message || 'Có lỗi xảy ra.'));
        }
      });
    } else {
      console.warn('No avatar file selected or currentUserEmail is missing. Cannot upload avatar.');
      alert('Vui lòng chọn ảnh đại diện để tải lên.');
    }
  }
}
