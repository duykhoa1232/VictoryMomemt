import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

import { PostService, Post, Comment } from '../services/post.service';
import { AuthService } from '../../../auth/services/auth.service';
import { EditPostDialogComponent, EditPostDialogData } from '../edit-post-dialog/edit-post-dialog.component';
import { QrCodeDialogComponent, QrCodeDialogData } from '../../../shared/qr-code-dialog/qr-code-dialog.component';
import {ConfirmationDialogComponent, ConfirmationDialogData} from '../../../shared/confirmation-dialog.component'; // Đảm bảo đường dẫn đúng

@Component({
  selector: 'app-post-list',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatMenuModule,
    MatDialogModule,
    MatButtonModule,
    // THÊM CÁC COMPONENT SAU VÀO ĐÂY
    // ConfirmationDialogComponent,
    // EditPostDialogComponent,
    // QrCodeDialogComponent
  ],
  templateUrl: './list-post.component.html',
  styleUrls: ['./list-post.component.css'],
})
export class PostListComponent implements OnInit {
  posts: Post[] = [];
  isLoading: boolean = false;

  constructor(
    private postService: PostService,
    private snackBar: MatSnackBar,
    private authService: AuthService,
    private dialog: MatDialog
  ) {
  }

  ngOnInit(): void {
    this.getPosts();
  }

  getPosts(): void {
    this.isLoading = true;
    this.postService.getAllPosts().subscribe({
      next: (data: Post[]) => {
        this.posts = data.map(post => {
          const displayUserName = post.userName || (post.userId ? post.userId.split('@')[0] : 'Ẩn danh');
          return {
            ...post,
            userName: displayUserName,
            likeCount: post.likeCount || 0,
            commentCount: post.commentCount || 0,
            shareCount: post.shareCount || 0,
            imageUrls: post.imageUrls || [],
            videoUrls: post.videoUrls || [],
            audioUrls: post.audioUrls || []
          };
        });
        this.isLoading = false;
        console.log('Bài đăng đã tải:', this.posts);
      },
      error: (err) => {
        console.error('Lỗi khi tải bài đăng:', err);
        this.isLoading = false;
        this.snackBar.open('Không thể tải bài đăng. Vui lòng thử lại sau.', 'Đóng', {
          duration: 3000,
          panelClass: ['error-snackbar'],
        });
      },
    });
  }

  getUserName(post: Post): string {
    return post.userName || (post.userId ? post.userId.split('@')[0] : 'Ẩn danh');
  }

  onLike() {
    // Logic for liking
  }

  deletePost(post: Post): void {
    const dialogData: ConfirmationDialogData = {
      title: 'Xác nhận xóa bài đăng',
      message: `Bạn có chắc chắn muốn xóa bài đăng của ${this.getUserName(post)} có nội dung: "${post.content}" không? Hành động này không thể hoàn tác.`,
      confirmText: 'Xóa',
      cancelText: 'Hủy',
      confirmButtonColor: 'warn'
    };

    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      width: '350px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.postService.deletePost(post.id).subscribe({
          next: () => {
            this.posts = this.posts.filter(p => p.id !== post.id);
            this.snackBar.open('Bài đăng đã được xóa thành công!', 'Đóng', {
              duration: 3000,
              panelClass: ['success-snackbar'],
            });
            console.log('Bài đăng đã được xóa:', post.id);
          },
          error: (err) => {
            console.error('Lỗi khi xóa bài đăng:', err);
            this.snackBar.open('Không thể xóa bài đăng. Vui lòng thử lại.', 'Đóng', {
              duration: 3000,
              panelClass: ['error-snackbar'],
            });
          },
        });
      } else {
        console.log('Hủy xóa bài đăng.');
      }
    });
  }

  editPost(post: Post): void {
    const dialogRef = this.dialog.open(EditPostDialogComponent, {
      width: '600px', // Chiều rộng của dialog chỉnh sửa
      data: { post: { ...post } } // Truyền bản sao của bài đăng để tránh thay đổi trực tiếp
    });

    dialogRef.afterClosed().subscribe((result: { formData: FormData, deletedMediaUrls: { images: string[], videos: string[], audios: string[] } } | null) => {
      if (result) {
        const { formData, deletedMediaUrls } = result;

        this.postService.updatePost(post.id, formData).subscribe({
          next: (updatedPostResponse: Post) => {
            // Loại bỏ logic xóa media cũ nếu PostService không hỗ trợ
            const index = this.posts.findIndex(p => p.id === updatedPostResponse.id);
            if (index !== -1) {
              this.posts[index] = updatedPostResponse;
            }
            this.snackBar.open('Bài đăng đã được cập nhật thành công!', 'Đóng', {
              duration: 3000,
              panelClass: ['success-snackbar'],
            });
            console.log('Bài đăng đã được cập nhật:', updatedPostResponse);
          },
          error: (err) => {
            console.error('Lỗi khi cập nhật bài đăng:', err);
            this.snackBar.open('Không thể cập nhật bài đăng. Vui lòng thử lại.', 'Đóng', {
              duration: 3000,
              panelClass: ['error-snackbar'],
            });
          },
        });
      } else {
        console.log('Hủy chỉnh sửa bài đăng.');
      }
    });
  }

  openQrCodeDialog(post: Post): void {
    const postShareUrl = this.postService.getShareablePostUrl(post.id);

    const dialogData: QrCodeDialogData = {
      title: 'Mã QR bài đăng',
      dataToEncode: postShareUrl
    };

    this.dialog.open(QrCodeDialogComponent, {
      width: '350px',
      data: dialogData
    });
  }
}
