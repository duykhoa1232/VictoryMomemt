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
import { debounceTime, distinctUntilChanged, switchMap, catchError, finalize } from 'rxjs/operators';
import { of } from 'rxjs';

import { PostService } from '../../core/home/services/post.service';
import { UserResponse } from '../models/profile.model';
import { MatChip, MatChipListbox } from '@angular/material/chips';

export interface UserSelectionDialogData {
  selectedUsers: UserResponse[];
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
  users: UserResponse[] = [];
  filteredUsers: UserResponse[] = [];
  isLoading = false;
  selectedUsers: UserResponse[] = [];

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
          return this.postService.searchUsers(query).pipe(
            catchError(error => {
              console.error('Lỗi khi tìm kiếm người dùng:', error);
              this.snackBar.open('Lỗi khi tìm kiếm người dùng.', 'Đóng', { duration: 3000 });
              return of([]);
            }),
            finalize(() => this.isLoading = false)
          );
        } else {
          this.isLoading = false;
          return of([]);
        }
      })
    ).subscribe(users => {
      this.users = users;
      this.filterUsers();
    });

    this.postService.searchUsers('').subscribe(users => {
      this.users = users;
      this.filterUsers();
    });
  }

  filterUsers(): void {
    const selectedUserIds = new Set(this.selectedUsers.map(u => u.id));
    this.filteredUsers = this.users.filter(user => !selectedUserIds.has(user.id));
  }

  isUserSelected(user: UserResponse): boolean {
    return this.selectedUsers.some(u => u.id === user.id);
  }

  onUserCheckboxChange(user: UserResponse, event: any): void {
    if (event.checked) {
      if (!this.isUserSelected(user)) {
        this.selectedUsers.push(user);
      }
    } else {
      this.selectedUsers = this.selectedUsers.filter(u => u.id !== user.id);
    }
    this.filterUsers();
  }

  removeSelectedUser(userToRemove: UserResponse): void {
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
