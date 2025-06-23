// // src/app/shared/share-dialog/share-dialog.component.ts
// import { Component, Inject, OnInit } from '@angular/core';
// import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
// import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
// import { CommonModule } from '@angular/common';
// import { MatButtonModule } from '@angular/material/button';
// import { MatFormFieldModule } from '@angular/material/form-field';
// import { MatInputModule } from '@angular/material/input';
// import { PostResponse } from '../../shared/models/post.model';
// import {MatCard, MatCardContent, MatCardHeader, MatCardSubtitle, MatCardTitle} from '@angular/material/card'; // Đảm bảo đường dẫn đúng
//
// // Định nghĩa interface cho dữ liệu truyền vào dialog
// export interface ShareDialogData {
//   originalPost: PostResponse; // Bài đăng gốc sẽ được chia sẻ
// }
//
// @Component({
//   selector: 'app-share-dialog',
//   standalone: true,
//   imports: [
//     CommonModule,
//     ReactiveFormsModule,
//     MatDialogModule,
//     MatButtonModule,
//     MatFormFieldModule,
//     MatInputModule,
//     MatCard,
//     MatCardHeader,
//     MatCardContent,
//     MatCardSubtitle,
//     MatCardTitle
//   ],
//   templateUrl: './share-dialog.component.html',
//   styleUrls: ['./share-dialog.component.css']
// })
// export class ShareDialogComponent implements OnInit {
//   shareForm: FormGroup;
//   originalPost: PostResponse;
//
//   constructor(
//     public dialogRef: MatDialogRef<ShareDialogComponent>,
//     @Inject(MAT_DIALOG_DATA) public data: ShareDialogData,
//     private fb: FormBuilder
//   ) {
//     this.originalPost = data.originalPost; // Lấy bài đăng gốc từ dữ liệu truyền vào
//     this.shareForm = this.fb.group({
//       content: ['', [Validators.maxLength(500)]] // Nội dung chia sẻ, có thể để trống
//     });
//   }
//
//   ngOnInit(): void {
//     // Không cần logic khởi tạo phức tạp ở đây, form đã được khởi tạo trong constructor
//   }
//
//   // Đóng dialog mà không lưu
//   onCancel(): void {
//     this.dialogRef.close();
//   }
//
//   // Đóng dialog và trả về dữ liệu chia sẻ
//   onShare(): void {
//     if (this.shareForm.valid) {
//       // Trả về đối tượng chứa originalPostId và content
//       this.dialogRef.close({
//         originalPostId: this.originalPost.id,
//         content: this.shareForm.get('content')?.value || '' // Lấy giá trị content
//       });
//     }
//   }
// }
