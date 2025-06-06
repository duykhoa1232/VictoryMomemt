// src/app/shared/confirm-dialog/confirm-dialog.component.ts
import { Component, Inject } from '@angular/core';
import {MatDialogRef, MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogContent, MatDialogActions, MatDialogTitle } from '@angular/material/dialog';

// Định nghĩa interface cho dữ liệu truyền vào dialog
export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmButtonText?: string; // Tùy chọn: Văn bản cho nút xác nhận (mặc định là 'Yes')
  cancelButtonText?: string;  // Tùy chọn: Văn bản cho nút hủy (mặc định là 'No')
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true, // Component này là standalone
  imports: [
    MatButtonModule,        // Để sử dụng mat-button
    MatDialogContent,       // Để sử dụng mat-dialog-content
    MatDialogActions,       // Để sử dụng mat-dialog-actions
    MatDialogTitle,
    MatDialogModule // <<< THÊM DÒNG NÀY
// Để sử dụng mat-dialog-title
  ],
  // Template HTML inline cho dialog
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content>
      <p>{{ data.message }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close="false">{{ data.cancelButtonText || 'No' }}</button>
      <button mat-flat-button color="primary" [mat-dialog-close]="true">{{ data.confirmButtonText || 'Yes' }}</button>
    </mat-dialog-actions>
  `,
  // Styles CSS inline cho dialog
  styles: [`
    /* Có thể thêm các style tùy chỉnh cho dialog ở đây */
    mat-dialog-content {
      padding-top: 10px;
    }
    mat-dialog-actions {
      padding-bottom: 10px;
    }
  `]
})
export class ConfirmDialogComponent {
  constructor(
    // MatDialogRef dùng để tham chiếu đến dialog đang mở, cho phép đóng nó
    public dialogRef: MatDialogRef<ConfirmDialogComponent>,
    // MAT_DIALOG_DATA để inject dữ liệu được truyền vào khi mở dialog
    @Inject(MAT_DIALOG_DATA) public data: ConfirmDialogData
  ) {}

  // Phương thức này có thể không cần thiết vì mat-dialog-close đã xử lý,
  // nhưng bạn có thể giữ nếu muốn thêm logic trước khi đóng.
  // onNoClick(): void {
  //   this.dialogRef.close(false); // Đóng dialog và trả về false
  // }
}
