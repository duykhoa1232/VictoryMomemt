

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button'; // Đảm bảo MatButtonModule được import cho nút "Tải thêm"

// Không cần MatPaginatorModule nếu bạn đã chuyển sang chức năng "Tải thêm"
// import { MatPaginatorModule } from '@angular/material/paginator';

import { PostService } from '../services/post.service';
// Đảm bảo Page và PostResponse được import từ đúng file model
import { Page, PostResponse } from '../../../shared/models/post.model';
// import { CommentResponse } from '../../../shared/models/comment.model'; // Nếu không dùng, có thể xóa
import { AuthService } from '../../../auth/services/auth.service';

import { QrCodeDialogComponent, QrCodeDialogData } from '../../../shared/qr-code-dialog/qr-code-dialog.component';
import { ConfirmationDialogComponent, ConfirmationDialogData } from '../../../shared/confirmation-dialog.component';
import { EditPostDialogComponent, EditPostDialogData } from '../edit-post-dialog/edit-post-dialog.component';

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
    MatButtonModule, // THÊM LẠI CHO NÚT "TẢI THÊM"
  ],
  templateUrl: './list-post.component.html',
  styleUrls: ['./list-post.component.css'],
})
export class PostListComponent implements OnInit {
  posts: PostResponse[] = [];
  isLoading: boolean = false;
  currentUserId: string | null = null;

  totalPosts: number = 0; // Tổng số bài đăng từ backend (totalElements)
  pageSize: number = 10; // Số lượng bài đăng mỗi lần tải thêm
  currentPage: number = 0; // Trang hiện tại (bắt đầu từ 0)
  sort: string = 'createdAt,desc';

  constructor(
    private postService: PostService,
    private snackBar: MatSnackBar,
    private authService: AuthService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.currentUserId = this.authService.getCurrentUserId();
    // Khi khởi tạo, luôn bắt đầu từ trang 0 để tải 10 bài đầu tiên
    this.currentPage = 0;
    this.getPosts();
  }

  // Phương thức tải bài đăng (tải mới hoặc tải thêm)
  getPosts(): void {
    this.isLoading = true;
    this.postService.getAllPosts(this.currentPage, this.pageSize, this.sort).subscribe({
      next: (response: Page<PostResponse>) => {
        // Nếu đây là trang đầu tiên (hoặc một lần tải mới hoàn toàn), thay thế danh sách
        // Nếu không, nối thêm vào danh sách hiện có
        if (this.currentPage === 0) {
          this.posts = response.content.map(this.mapPostData);
        } else {
          this.posts = this.posts.concat(response.content.map(this.mapPostData));
        }

        this.totalPosts = response.totalElements; // Cập nhật tổng số bài đăng
        this.isLoading = false;

        // Debug log
        console.log('Bài đăng đã tải (đã hiển thị):', this.posts.length, 'Tổng số bài (backend):', this.totalPosts);
        console.log('Số bài trong trang hiện tại:', response.content.length, 'Trang hiện tại:', response.number);

        // Kiểm tra nếu đã tải hết bài (trang cuối có ít hơn pageSize)
        if (response.content.length < this.pageSize && this.posts.length < this.totalPosts) {
          this.totalPosts = this.posts.length; // Cập nhật totalPosts để khớp với số bài thực tế
          console.log('Đã tải hết bài, cập nhật totalPosts:', this.totalPosts);
        }
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

  // Phương thức được gọi khi người dùng nhấn nút "Tải thêm"
  loadMorePosts(): void {
    // Chỉ tải thêm nếu vẫn còn bài đăng để tải và không đang trong quá trình tải khác
    if (this.posts.length < this.totalPosts && !this.isLoading) {
      this.currentPage++; // Tăng số trang lên 1
      this.getPosts(); // Tải bài đăng cho trang tiếp theo
    }
  }

  // Phương thức mapping dữ liệu (đã tách ra để tái sử dụng)
  private mapPostData(post: PostResponse): PostResponse {
    const displayUserName = post.author?.name || (post.author?.email ? post.author.email.split('@')[0] : 'Ẩn danh');
    return {
      ...post,
      author: { ...post.author, name: displayUserName },
      likeCount: post.likeCount || 0,
      commentCount: post.commentCount || 0,
      shareCount: post.shareCount || 0,
      imageUrls: post.imageUrls || [],
      videoUrls: post.videoUrls || [],
      audioUrls: post.audioUrls || [],
    };
  }

  getUserName(post: PostResponse): string {
    return post.author?.name || (post.author?.email ? post.author.email.split('@')[0] : 'Anonymous');
  }

  // isMyPost(post: PostResponse): boolean {
  //   return this.currentUserId !== null && post.author?.id === this.currentUserId;
  // }
  //
  onLike(post: PostResponse): void {
    this.postService.toggleLike(post.id).subscribe({
      next: (updatedPost: PostResponse) => {
        const index = this.posts.findIndex(p => p.id === updatedPost.id);
        if (index !== -1) {
          this.posts[index] = updatedPost;
        }
      },
      error: (err) => {
        console.error('Lỗi khi like/unlike bài đăng:', err);
        this.snackBar.open('Không thể thực hiện thao tác like/unlike.', 'Đóng', {
          duration: 3000,
          panelClass: ['error-snackbar'],
        });
      },
    });
  }

  deletePost(post: PostResponse): void {
    const dialogData: ConfirmationDialogData = {
      title: 'Xác nhận xóa bài đăng',
      message: `Are you sure you want to delete this post? ${this.getUserName(post)} có nội dung: "${post.content}" không? Hành động này không thể hoàn tác.`,
      confirmText: 'Xóa',
      cancelText: 'Hủy',
      confirmButtonColor: 'warn',
    };

    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      width: '350px',
      data: dialogData,
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.postService.deletePost(post.id).subscribe({
          next: () => {
            this.snackBar.open('Post was successfully deleted!', 'Close', {
              duration: 3000,
              panelClass: ['success-snackbar'],
            });
            console.log('Post has been deleted:', post.id);
            // Sau khi xóa, cập nhật danh sách hiển thị
            this.posts = this.posts.filter(p => p.id !== post.id);
            this.totalPosts--; // Giảm tổng số bài đăng
          },
          error: (err) => {
            console.error('Lỗi khi xóa bài đăng:', err);
            this.snackBar.open('Unable to delete post. Please try again.', 'Close', {
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

  editPost(post: PostResponse): void {
    const dialogRef = this.dialog.open(EditPostDialogComponent, {
      width: '600px',
      data: { post: { ...post } } as EditPostDialogData,
    });

    dialogRef.afterClosed().subscribe((result: { formData: FormData; deletedMediaUrls: { images: string[]; videos: string[]; audios: string[] } } | null) => {
      if (result) {
        const { formData, deletedMediaUrls } = result;

        this.postService.updatePost(post.id, formData).subscribe({
          next: (updatedPostResponse: PostResponse) => {
            const index = this.posts.findIndex(p => p.id === updatedPostResponse.id);
            if (index !== -1) {
              this.posts[index] = updatedPostResponse;
            }
            this.snackBar.open('Post has been updated successfully!', 'Close', {
              duration: 3000,
              panelClass: ['success-snackbar'],
            });
            console.log('Post has been updated:', updatedPostResponse);
          },
          error: (err) => {
            console.error('Error updating post:', err);
            this.snackBar.open('Unable to update post. Please try again.', 'Close', {
              duration: 3000,
              panelClass: ['error-snackbar'],
            });
          },
        });
      } else {
        console.log('Cancel editing post.');
      }
    });
  }

  openQrCodeDialog(post: PostResponse): void {
    const postShareUrl = this.postService.getShareablePostUrl(post.id);

    const dialogData: QrCodeDialogData = {
      title: 'Post QR Code',
      dataToEncode: postShareUrl,
    };

    this.dialog.open(QrCodeDialogComponent, {
      width: '350px',
      data: dialogData,
    });
  }
}








