// src/app/shared/user-selection-dialog/user-selection-dialog.component.ts
import { Component, OnInit, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { debounceTime, distinctUntilChanged, switchMap, catchError, finalize } from 'rxjs/operators'; // Bỏ 'map' ở đây vì sẽ không cần nữa
import { of } from 'rxjs';

import { PostService } from '../../core/home/services/post.service'; // Chỉ import PostService
import { UserResponse } from '../models/user.model'; // Import UserResponse từ shared/models
import { MatChip, MatChipListbox } from '@angular/material/chips';

export interface UserSelectionDialogData {
  selectedUsers: UserResponse[]; // Sửa kiểu từ User[] sang UserResponse[]
}

@Component({
  selector: 'app-user-selection-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatChipListbox,
    MatChip
  ],
  templateUrl: './user-selection-dialog.component.html',
  styleUrls: ['./user-selection-dialog.component.css']
})
export class UserSelectionDialogComponent implements OnInit {
  searchControl = new FormControl('');
  users: UserResponse[] = []; // Sửa kiểu từ User[] sang UserResponse[]
  filteredUsers: UserResponse[] = []; // Sửa kiểu từ User[] sang UserResponse[]
  isLoading = false;
  selectedUsers: UserResponse[] = []; // Sửa kiểu từ User[] sang UserResponse[]
  // totalUsers: number = 0; // KHÔNG CẦN NỮA vì searchUsers trong PostService trả về UserResponse[] trực tiếp

  constructor(
    public dialogRef: MatDialogRef<UserSelectionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: UserSelectionDialogData,
    private postService: PostService,
    private snackBar: MatSnackBar
  ) {
    this.selectedUsers = [...data.selectedUsers];
  }

  ngOnInit(): void {
    this.searchControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(query => {
        if (query && query.trim().length > 0) {
          this.isLoading = true;
          // Gọi API tìm kiếm người dùng chỉ với đối số query
          return this.postService.searchUsers(query).pipe( // CHỈ 1 ĐỐI SỐ: query
            // Bỏ map(response => { this.totalUsers = response.total; return response.users; })
            // vì searchUsers giờ trả về UserResponse[] trực tiếp
            catchError(error => {
              console.error('Lỗi khi tìm kiếm người dùng:', error);
              this.snackBar.open('Lỗi khi tìm kiếm người dùng.', 'Đóng', { duration: 3000 });
              return of([]);
            }),
            finalize(() => this.isLoading = false)
          );
        } else {
          this.isLoading = false;
          return of([]); // Trả về mảng rỗng nếu query trống
        }
      })
    ).subscribe(users => {
      this.users = users;
      this.filterUsers();
    });

    // Ban đầu, hiển thị tất cả người dùng nếu không có query
    // Gọi API tìm kiếm người dùng chỉ với đối số query rỗng để lấy tất cả
    this.postService.searchUsers('').subscribe(users => { // CHỈ 1 ĐỐI SỐ: ''
      this.users = users;
      this.filterUsers();
    });
  }

  filterUsers(): void {
    const selectedUserIds = new Set(this.selectedUsers.map(u => u.id));
    this.filteredUsers = this.users.filter(user => !selectedUserIds.has(user.id));
  }

  isUserSelected(user: UserResponse): boolean { // Sửa kiểu từ User sang UserResponse
    return this.selectedUsers.some(u => u.id === user.id);
  }

  onUserCheckboxChange(user: UserResponse, event: any): void { // Sửa kiểu từ User sang UserResponse
    if (event.checked) {
      if (!this.isUserSelected(user)) {
        this.selectedUsers.push(user);
      }
    } else {
      this.selectedUsers = this.selectedUsers.filter(u => u.id !== user.id);
    }
    this.filterUsers();
  }

  removeSelectedUser(userToRemove: UserResponse): void { // Sửa kiểu từ User sang UserResponse
    this.selectedUsers = this.selectedUsers.filter(user => user.id !== userToRemove.id);
    this.filterUsers();
  }

  onSave(): void {
    this.dialogRef.close(this.selectedUsers);
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
