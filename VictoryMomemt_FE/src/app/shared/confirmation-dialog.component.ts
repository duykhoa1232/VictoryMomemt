import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';

export interface ConfirmationDialogData {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  confirmButtonColor?: 'primary' | 'accent' | 'warn';
}

@Component({
  selector: 'app-confirmation-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogModule
  ],
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content>
      <p>{{ data.message }}</p>
    </mat-dialog-content>
    <div mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="false">{{ data.cancelText || 'Hủy' }}</button>
      <button mat-flat-button [color]="data.confirmButtonColor || 'warn'" [mat-dialog-close]="true">{{ data.confirmText || 'Xóa' }}</button>
    </div>
  `,
  styles: [`
    h2[mat-dialog-title] {
      color: #d32f2f;
      font-size: 1.25rem;
      font-weight: 600;
    }
    mat-dialog-content {
      padding-bottom: 20px;
      font-size: 1rem;
      line-height: 1.5;
    }
    div[mat-dialog-actions] {
      padding-top: 0;
      padding-bottom: 16px;
      padding-right: 24px;
    }
    button[mat-flat-button] {
      margin-left: 8px;
    }
  `]
})
export class ConfirmationDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ConfirmationDialogData
  ) {}
}
