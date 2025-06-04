// src/app/shared/qr-code-dialog/qr-code-dialog.component.ts
import { Component, OnInit, AfterViewInit, Inject, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

// Khai báo global QRCode nếu bạn dùng CDN
declare const QRCode: any;

export interface QrCodeDialogData {
  title: string;
  dataToEncode: string; // Dữ liệu (URL bài đăng) để mã hóa thành QR
}

@Component({
  selector: 'app-qr-code-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule
  ],
  templateUrl: './qr-code-dialog.component.html',
  styleUrls: ['./qr-code-dialog.component.css']
})
export class QrCodeDialogComponent implements OnInit, AfterViewInit {
  @ViewChild('qrcodeCanvas', { static: true }) qrcodeCanvas!: ElementRef;

  constructor(
    public dialogRef: MatDialogRef<QrCodeDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: QrCodeDialogData,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    // Initialization logic if any
  }

  ngAfterViewInit(): void {
    // Đảm bảo thư viện QRCode đã tải và phần tử canvas đã sẵn sàng
    if (typeof QRCode !== 'undefined' && this.qrcodeCanvas) {
      this.generateQrCode();
    } else {
      console.error('QRCode library not loaded or canvas element not found.');
      this.snackBar.open('Không thể tạo mã QR. Vui lòng thử lại.', 'Đóng', { duration: 3000, panelClass: ['error-snackbar'] });
    }
  }

  generateQrCode(): void {
    try {
      // Tạo mã QR trong phần tử canvas
      new QRCode(this.qrcodeCanvas.nativeElement, {
        text: this.data.dataToEncode,
        width: 256,
        height: 256,
        colorDark: "#000000",
        colorLight: "#ffffff",
        correctLevel: QRCode.CorrectLevel.H // Mức độ sửa lỗi cao
      });
    } catch (e) {
      console.error('Lỗi khi tạo mã QR:', e);
      this.snackBar.open('Lỗi khi tạo mã QR. Vui lòng thử lại.', 'Đóng', { duration: 3000, panelClass: ['error-snackbar'] });
    }
  }

  // Phương thức để đóng dialog
  onClose(): void {
    this.dialogRef.close();
  }

  // Phương thức để sao chép URL vào clipboard
  copyToClipboard(): void {
    // Sử dụng document.execCommand thay vì navigator.clipboard.writeText
    // vì navigator.clipboard có thể bị hạn chế trong môi trường iframe.
    const el = document.createElement('textarea');
    el.value = this.data.dataToEncode;
    document.body.appendChild(el);
    el.select();
    try {
      const successful = document.execCommand('copy');
      if (successful) {
        this.snackBar.open('Đã sao chép URL vào clipboard!', 'Đóng', { duration: 2000, panelClass: ['success-snackbar'] });
      } else {
        this.snackBar.open('Không thể sao chép URL.', 'Đóng', { duration: 3000, panelClass: ['warning-snackbar'] });
      }
    } catch (err) {
      console.error('Không thể sao chép URL:', err);
      this.snackBar.open('Không thể sao chép URL.', 'Đóng', { duration: 3000, panelClass: ['error-snackbar'] });
    }
    document.body.removeChild(el);
  }
}
