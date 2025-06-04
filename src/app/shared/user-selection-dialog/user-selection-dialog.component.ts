// src/app/shared/components/user-selection-dialog/user-selection-dialog.component.ts
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
import { debounceTime, distinctUntilChanged, switchMap, catchError, finalize, map } from 'rxjs/operators'; // THÊM map
import { of } from 'rxjs';

import { PostService, User } from '../../core/home/services/post.service';
import {MatChip, MatChipListbox} from '@angular/material/chips'; // ĐÃ SỬA: Import User từ PostService

export interface UserSelectionDialogData {
  selectedUsers: User[]; // Danh sách người dùng đã chọn từ trước
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
  users: User[] = [];
  filteredUsers: User[] = [];
  isLoading = false;
  selectedUsers: User[] = []; // Danh sách người dùng được chọn trong dialog
  totalUsers: number = 0; // THÊM: Để lưu tổng số người dùng cho phân trang (nếu cần)

  constructor(
    public dialogRef: MatDialogRef<UserSelectionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: UserSelectionDialogData,
    private postService: PostService,
    private snackBar: MatSnackBar
  ) {
    // Khởi tạo selectedUsers với dữ liệu truyền vào
    this.selectedUsers = [...data.selectedUsers];
  }

  ngOnInit(): void {
    // Lắng nghe thay đổi trên ô tìm kiếm
    this.searchControl.valueChanges.pipe(
      debounceTime(300), // Đợi 300ms sau khi gõ xong
      distinctUntilChanged(), // Chỉ phát ra nếu giá trị thay đổi
      switchMap(query => {
        if (query && query.trim().length > 0) {
          this.isLoading = true;
          // Gọi API tìm kiếm người dùng, truyền pageIndex và pageSize
          return this.postService.searchUsers(query, 0, 10).pipe( // ĐÃ SỬA: Thêm pageIndex và pageSize
            map(response => {
              this.totalUsers = response.total; // Cập nhật totalUsers
              return response.users; // Chỉ lấy mảng users từ response
            }),
            catchError(error => {
              console.error('Lỗi khi tìm kiếm người dùng:', error);
              this.snackBar.open('Lỗi khi tìm kiếm người dùng.', 'Đóng', { duration: 3000 });
              return of([]); // Trả về mảng rỗng nếu có lỗi
            }),
            finalize(() => this.isLoading = false) // Đảm bảo isLoading được đặt lại
          );
        } else {
          this.isLoading = false;
          return of([]); // Trả về mảng rỗng nếu query trống
        }
      })
    ).subscribe(users => {
      this.users = users;
      this.filterUsers(); // Lọc lại danh sách hiển thị
    });

    // Ban đầu, hiển thị tất cả người dùng nếu không có query
    // Gọi API tìm kiếm người dùng, truyền pageIndex và pageSize
    this.postService.searchUsers('', 0, 10).pipe( // ĐÃ SỬA: Thêm pageIndex và pageSize
      map(response => {
        this.totalUsers = response.total; // Cập nhật totalUsers
        return response.users; // Chỉ lấy mảng users từ response
      })
    ).subscribe(users => {
      this.users = users;
      this.filterUsers();
    });
  }

  // Phương thức lọc người dùng để tránh trùng lặp với người đã chọn
  filterUsers(): void {
    const selectedUserIds = new Set(this.selectedUsers.map(u => u.id));
    this.filteredUsers = this.users.filter(user => !selectedUserIds.has(user.id));
  }

  // Kiểm tra xem người dùng đã được chọn hay chưa
  isUserSelected(user: User): boolean {
    return this.selectedUsers.some(u => u.id === user.id);
  }

  // Xử lý khi checkbox của người dùng thay đổi
  onUserCheckboxChange(user: User, event: any): void {
    if (event.checked) {
      // Thêm người dùng vào danh sách đã chọn nếu chưa có
      if (!this.isUserSelected(user)) {
        this.selectedUsers.push(user);
      }
    } else {
      // Xóa người dùng khỏi danh sách đã chọn
      this.selectedUsers = this.selectedUsers.filter(u => u.id !== user.id);
    }
    this.filterUsers(); // Cập nhật lại danh sách hiển thị
  }

  // Xóa người dùng khỏi danh sách đã chọn (từ chip)
  removeSelectedUser(userToRemove: User): void {
    this.selectedUsers = this.selectedUsers.filter(user => user.id !== userToRemove.id);
    this.filterUsers(); // Cập nhật lại danh sách hiển thị
  }

  // Đóng dialog và trả về danh sách người dùng đã chọn
  onSave(): void {
    this.dialogRef.close(this.selectedUsers);
  }

  // Đóng dialog mà không lưu thay đổi
  onCancel(): void {
    this.dialogRef.close();
  }
}
