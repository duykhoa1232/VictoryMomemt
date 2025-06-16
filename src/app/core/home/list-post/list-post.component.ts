//
//
// import { Component, OnInit } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { MatCardModule } from '@angular/material/card';
// import { MatIconModule } from '@angular/material/icon';
// import { MatMenuModule } from '@angular/material/menu';
// import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
// import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
// import { MatDialog, MatDialogModule } from '@angular/material/dialog';
// import { MatButtonModule } from '@angular/material/button'; // Đảm bảo MatButtonModule được import cho nút "Tải thêm"
//
// // Không cần MatPaginatorModule nếu bạn đã chuyển sang chức năng "Tải thêm"
// // import { MatPaginatorModule } from '@angular/material/paginator';
//
// import { PostService } from '../services/post.service';
// // Đảm bảo Page và PostResponse được import từ đúng file model
// import { Page, PostResponse } from '../../../shared/models/post.model';
// // import { CommentResponse } from '../../../shared/models/comment.model'; // Nếu không dùng, có thể xóa
// import { AuthService } from '../../../auth/services/auth.service';
//
// import { QrCodeDialogComponent, QrCodeDialogData } from '../../../shared/qr-code-dialog/qr-code-dialog.component';
// import { ConfirmationDialogComponent, ConfirmationDialogData } from '../../../shared/confirmation-dialog.component';
// import { EditPostDialogComponent, EditPostDialogData } from '../edit-post-dialog/edit-post-dialog.component';
//
// @Component({
//   selector: 'app-post-list',
//   standalone: true,
//   imports: [
//     CommonModule,
//     MatCardModule,
//     MatIconModule,
//     MatSnackBarModule,
//     MatProgressSpinnerModule,
//     MatMenuModule,
//     MatDialogModule,
//     MatButtonModule, // THÊM LẠI CHO NÚT "TẢI THÊM"
//   ],
//   templateUrl: './list-post.component.html',
//   styleUrls: ['./list-post.component.css'],
// })
// export class PostListComponent implements OnInit {
//   posts: PostResponse[] = [];
//   isLoading: boolean = false;
//   currentUserId: string | null = null;
//
//   totalPosts: number = 0; // Tổng số bài đăng từ backend (totalElements)
//   pageSize: number = 10; // Số lượng bài đăng mỗi lần tải thêm
//   currentPage: number = 0; // Trang hiện tại (bắt đầu từ 0)
//   sort: string = 'createdAt,desc';
//
//   constructor(
//     private postService: PostService,
//     private snackBar: MatSnackBar,
//     private authService: AuthService,
//     private dialog: MatDialog
//   ) {}
//
//   ngOnInit(): void {
//     this.currentUserId = this.authService.getCurrentUserId();
//     // Khi khởi tạo, luôn bắt đầu từ trang 0 để tải 10 bài đầu tiên
//     this.currentPage = 0;
//     this.getPosts();
//   }
//
//   // Phương thức tải bài đăng (tải mới hoặc tải thêm)
//   getPosts(): void {
//     this.isLoading = true;
//     this.postService.getAllPosts(this.currentPage, this.pageSize, this.sort).subscribe({
//       next: (response: Page<PostResponse>) => {
//         // Nếu đây là trang đầu tiên (hoặc một lần tải mới hoàn toàn), thay thế danh sách
//         // Nếu không, nối thêm vào danh sách hiện có
//         if (this.currentPage === 0) {
//           this.posts = response.content.map(this.mapPostData);
//         } else {
//           this.posts = this.posts.concat(response.content.map(this.mapPostData));
//         }
//
//         this.totalPosts = response.totalElements; // Cập nhật tổng số bài đăng
//         this.isLoading = false;
//
//         // Debug log
//         console.log('Bài đăng đã tải (đã hiển thị):', this.posts.length, 'Tổng số bài (backend):', this.totalPosts);
//         console.log('Số bài trong trang hiện tại:', response.content.length, 'Trang hiện tại:', response.number);
//
//         // Kiểm tra nếu đã tải hết bài (trang cuối có ít hơn pageSize)
//         if (response.content.length < this.pageSize && this.posts.length < this.totalPosts) {
//           this.totalPosts = this.posts.length; // Cập nhật totalPosts để khớp với số bài thực tế
//           console.log('Đã tải hết bài, cập nhật totalPosts:', this.totalPosts);
//         }
//       },
//       error: (err) => {
//         console.error('Lỗi khi tải bài đăng:', err);
//         this.isLoading = false;
//         this.snackBar.open('Không thể tải bài đăng. Vui lòng thử lại sau.', 'Đóng', {
//           duration: 3000,
//           panelClass: ['error-snackbar'],
//         });
//       },
//     });
//   }
//
//   // Phương thức được gọi khi người dùng nhấn nút "Tải thêm"
//   loadMorePosts(): void {
//     // Chỉ tải thêm nếu vẫn còn bài đăng để tải và không đang trong quá trình tải khác
//     if (this.posts.length < this.totalPosts && !this.isLoading) {
//       this.currentPage++; // Tăng số trang lên 1
//       this.getPosts(); // Tải bài đăng cho trang tiếp theo
//     }
//   }
//
//   // Phương thức mapping dữ liệu (đã tách ra để tái sử dụng)
//   private mapPostData(post: PostResponse): PostResponse {
//     const displayUserName = post.author?.name || (post.author?.email ? post.author.email.split('@')[0] : 'Ẩn danh');
//     return {
//       ...post,
//       author: { ...post.author, name: displayUserName },
//       likeCount: post.likeCount || 0,
//       commentCount: post.commentCount || 0,
//       shareCount: post.shareCount || 0,
//       imageUrls: post.imageUrls || [],
//       videoUrls: post.videoUrls || [],
//       audioUrls: post.audioUrls || [],
//     };
//   }
//
//   getUserName(post: PostResponse): string {
//     return post.author?.name || (post.author?.email ? post.author.email.split('@')[0] : 'Anonymous');
//   }
//
//   // isMyPost(post: PostResponse): boolean {
//   //   return this.currentUserId !== null && post.author?.id === this.currentUserId;
//   // }
//   //
//   onLike(post: PostResponse): void {
//     this.postService.toggleLike(post.id).subscribe({
//       next: (updatedPost: PostResponse) => {
//         const index = this.posts.findIndex(p => p.id === updatedPost.id);
//         if (index !== -1) {
//           this.posts[index] = updatedPost;
//         }
//       },
//       error: (err) => {
//         console.error('Lỗi khi like/unlike bài đăng:', err);
//         this.snackBar.open('Không thể thực hiện thao tác like/unlike.', 'Đóng', {
//           duration: 3000,
//           panelClass: ['error-snackbar'],
//         });
//       },
//     });
//   }
//
//   deletePost(post: PostResponse): void {
//     const dialogData: ConfirmationDialogData = {
//       title: 'Xác nhận xóa bài đăng',
//       message: `Are you sure you want to delete this post? ${this.getUserName(post)} có nội dung: "${post.content}" không? Hành động này không thể hoàn tác.`,
//       confirmText: 'Xóa',
//       cancelText: 'Hủy',
//       confirmButtonColor: 'warn',
//     };
//
//     const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
//       width: '350px',
//       data: dialogData,
//     });
//
//     dialogRef.afterClosed().subscribe(result => {
//       if (result) {
//         this.postService.deletePost(post.id).subscribe({
//           next: () => {
//             this.snackBar.open('Post was successfully deleted!', 'Close', {
//               duration: 3000,
//               panelClass: ['success-snackbar'],
//             });
//             console.log('Post has been deleted:', post.id);
//             // Sau khi xóa, cập nhật danh sách hiển thị
//             this.posts = this.posts.filter(p => p.id !== post.id);
//             this.totalPosts--; // Giảm tổng số bài đăng
//           },
//           error: (err) => {
//             console.error('Lỗi khi xóa bài đăng:', err);
//             this.snackBar.open('Unable to delete post. Please try again.', 'Close', {
//               duration: 3000,
//               panelClass: ['error-snackbar'],
//             });
//           },
//         });
//       } else {
//         console.log('Hủy xóa bài đăng.');
//       }
//     });
//   }
//
//   editPost(post: PostResponse): void {
//     const dialogRef = this.dialog.open(EditPostDialogComponent, {
//       width: '600px',
//       data: { post: { ...post } } as EditPostDialogData,
//     });
//
//     dialogRef.afterClosed().subscribe((result: { formData: FormData; deletedMediaUrls: { images: string[]; videos: string[]; audios: string[] } } | null) => {
//       if (result) {
//         const { formData, deletedMediaUrls } = result;
//
//         this.postService.updatePost(post.id, formData).subscribe({
//           next: (updatedPostResponse: PostResponse) => {
//             const index = this.posts.findIndex(p => p.id === updatedPostResponse.id);
//             if (index !== -1) {
//               this.posts[index] = updatedPostResponse;
//             }
//             this.snackBar.open('Post has been updated successfully!', 'Close', {
//               duration: 3000,
//               panelClass: ['success-snackbar'],
//             });
//             console.log('Post has been updated:', updatedPostResponse);
//           },
//           error: (err) => {
//             console.error('Error updating post:', err);
//             this.snackBar.open('Unable to update post. Please try again.', 'Close', {
//               duration: 3000,
//               panelClass: ['error-snackbar'],
//             });
//           },
//         });
//       } else {
//         console.log('Cancel editing post.');
//       }
//     });
//   }
//
//   openQrCodeDialog(post: PostResponse): void {
//     const postShareUrl = this.postService.getShareablePostUrl(post.id);
//
//     const dialogData: QrCodeDialogData = {
//       title: 'Post QR Code',
//       dataToEncode: postShareUrl,
//     };
//
//     this.dialog.open(QrCodeDialogComponent, {
//       width: '350px',
//       data: dialogData,
//     });
//   }
// }
//
//
//
//
//
//
//
//


// src/app/pages/post/list-post/list-post.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';

import { PostService } from '../services/post.service';
import { Page, PostResponse, PostRequest } from '../../../shared/models/post.model';
import { CommentRequest, CommentResponse } from '../../../shared/models/comment.model';
import { AuthService } from '../../../auth/services/auth.service';

// Bỏ comment các imports này nếu bạn dùng chúng trong template hoặc logic
import { QrCodeDialogComponent, QrCodeDialogData } from '../../../shared/qr-code-dialog/qr-code-dialog.component';
import { ConfirmationDialogComponent, ConfirmationDialogData } from '../../../shared/confirmation-dialog.component';
import { EditPostDialogComponent, EditPostDialogData } from '../edit-post-dialog/edit-post-dialog.component';
import {CommentService} from '../services/comment.service';

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
    MatInputModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    DatePipe,
    // QrCodeDialogComponent, // Đảm bảo đã import và được sử dụng
    // ConfirmationDialogComponent, // Đảm bảo đã import và được sử dụng
    // EditPostDialogComponent // Đảm bảo đã import và được sử dụng
  ],
  templateUrl: './list-post.component.html',
  styleUrls: ['./list-post.component.css'],
})
export class PostListComponent implements OnInit {
  posts: PostResponse[] = [];
  isLoading: boolean = false;
  currentUserId: string | null = null;
  currentUserEmail: string | null = null;

  totalPosts: number = 0;
  pageSize: number = 10;
  currentPage: number = 0;
  sort: string = 'createdAt,desc';

  showCommentsMap: Map<string, boolean> = new Map<string, boolean>();
  commentControlsMap: Map<string, FormControl> = new Map<string, FormControl>();
  replyControlsMap: Map<string, FormControl> = new Map<string, FormControl>();
  editingCommentMap: Map<string, boolean> = new Map<string, boolean>();
  editCommentControlsMap: Map<string, FormControl> = new Map<string, FormControl>();

  constructor(
    private postService: PostService,
    private commentService: CommentService,
    private snackBar: MatSnackBar,
    private authService: AuthService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.currentUserId = this.authService.getCurrentUserId();
    this.currentUserEmail = this.authService.getCurrentUserEmail();
    this.currentPage = 0;
    this.getPosts();
  }

  private initializeCommentControls(postId: string): void {
    if (!this.commentControlsMap.has(postId)) {
      this.commentControlsMap.set(postId, new FormControl('', Validators.required));
    }
  }

  // --- Post-related methods ---
  getPosts(): void {
    this.isLoading = true;
    this.postService.getAllPosts(this.currentPage, this.pageSize, this.sort).subscribe({
      next: (response: Page<PostResponse>) => {
        if (this.currentPage === 0) {
          this.posts = response.content.map(this.mapPostData);
        } else {
          this.posts = this.posts.concat(response.content.map(this.mapPostData));
        }

        // --- THAY ĐỔI QUAN TRỌNG ĐỂ TẢI BÌNH LUẬN NGAY TỪ ĐẦU ---
        // Sử dụng Promise.all để chờ tất cả bình luận được tải xong trước khi tắt isLoading
        const commentLoadPromises = this.posts.map(post => {
          this.initializeCommentControls(post.id);
          // Kiểm tra nếu comments chưa có hoặc rỗng thì mới tải
          if (!post.comments || post.comments.length === 0) {
            return this.commentService.getCommentsByPostId(post.id).toPromise().then(comments => {
              post.comments = comments || []; // Gán bình luận đã tải
              console.log('Bình luận đã tải ngay cho post', post.id, post.comments);
            }).catch(err => {
              console.error('Lỗi khi tải bình luận ngay lập tức cho post', post.id, err);
              // Xử lý lỗi nhưng không chặn Promise.all
            });
          }
          return Promise.resolve(); // Trả về Promise đã resolved nếu không cần tải
        });

        Promise.all(commentLoadPromises).finally(() => {
          this.totalPosts = response.totalElements;
          this.isLoading = false;

          console.log('Bài đăng đã tải (đã hiển thị):', this.posts.length, 'Tổng số bài (backend):', this.totalPosts);
          console.log('Số bài trong trang hiện tại:', response.content.length, 'Trang hiện tại:', response.number);

          if (response.content.length < this.pageSize && this.posts.length < this.totalPosts) {
            this.totalPosts = this.posts.length;
            console.log('Đã tải hết bài, cập nhật totalPosts:', this.totalPosts);
          }
        });
        // --- KẾT THÚC THAY ĐỔI QUAN TRỌNG ---
      },
      error: (err: any) => {
        console.error('Lỗi khi tải bài đăng:', err);
        this.isLoading = false;
        this.snackBar.open('Không thể tải bài đăng. Vui lòng thử lại sau.', 'Đóng', {
          duration: 3000,
          panelClass: ['error-snackbar'],
        });
      },
    });
  }

  loadMorePosts(): void {
    if (this.posts.length < this.totalPosts && !this.isLoading) {
      this.currentPage++;
      this.getPosts();
    }
  }

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
      comments: post.comments || [] // Đảm bảo comments được khởi tạo rỗng nếu không có
    };
  }

  getUserName(post: PostResponse): string {
    return post.author?.name || (post.author?.email ? post.author.email.split('@')[0] : 'Anonymous');
  }

  onLike(post: PostResponse): void {
    this.postService.toggleLike(post.id).subscribe({
      next: (updatedPost: PostResponse) => {
        const index = this.posts.findIndex(p => p.id === updatedPost.id);
        if (index !== -1) {
          this.posts[index] = updatedPost;
        }
      },
      error: (err: any) => {
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
            this.posts = this.posts.filter(p => p.id !== post.id);
            this.totalPosts--;
          },
          error: (err: any) => {
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
        const { formData } = result;

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
          error: (err: any) => {
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

  // --- Comment-related methods ---

  toggleComments(post: PostResponse): void {
    console.log('Clicked comment icon for post:', post.id);
    const currentState = this.showCommentsMap.get(post.id) || false;
    this.showCommentsMap.set(post.id, !currentState);
    console.log('New comment section state for post', post.id, ':', this.showCommentsMap.get(post.id));

    // Logic này không cần thay đổi nhiều vì comments đã được tải sẵn
    // Tuy nhiên, nếu bạn muốn đảm bảo comments được cập nhật mới nhất mỗi khi mở,
    // bạn có thể gọi loadCommentsForPost ở đây. Nhưng điều đó sẽ lại gây tốn tài nguyên.
    // Giữ nguyên như cũ nếu bạn chỉ muốn mở/đóng phần đã tải.
  }

  areCommentsShown(postId: string): boolean {
    return this.showCommentsMap.get(postId) || false;
  }

  // loadCommentsForPost giờ được gọi ngay khi post được tải, không cần ở đây nữa
  loadCommentsForPost(post: PostResponse): void {
    this.commentService.getCommentsByPostId(post.id).subscribe({
      next: (comments: CommentResponse[]) => {
        post.comments = comments; // Gán dữ liệu bình luận vào bài đăng
        console.log('Bình luận đã tải cho post', post.id, comments);
      },
      error: (err: any) => {
        console.error('Lỗi khi tải bình luận:', err);
        this.snackBar.open('Không thể tải bình luận. Vui lòng thử lại.', 'Đóng', {
          duration: 3000,
          panelClass: ['error-snackbar'],
        });
      },
    });
  }

  getCommentFormControl(postId: string): FormControl {
    if (!this.commentControlsMap.has(postId)) {
      this.initializeCommentControls(postId);
    }
    return this.commentControlsMap.get(postId)!;
  }

  createComment(post: PostResponse): void {
    const commentControl = this.getCommentFormControl(post.id);
    if (commentControl.valid) {
      const request: CommentRequest = {
        content: commentControl.value!,
        parentCommentId: undefined
      };

      this.commentService.createComment(post.id, request).subscribe({
        next: (newComment: CommentResponse) => {
          if (!post.comments) {
            post.comments = [];
          }
          post.comments.unshift(newComment);
          commentControl.reset();
          this.snackBar.open('Bình luận đã được tạo!', 'Đóng', {
            duration: 2000,
            panelClass: ['success-snackbar'],
          });
          // Không cập nhật post.commentCount ở đây nữa vì getTotalCommentsCount sẽ tự tính
          // post.commentCount = (post.commentCount || 0) + 1; // <--- CÓ THỂ BỎ DÒNG NÀY
        },
        error: (err: any) => {
          console.error('Lỗi khi tạo bình luận:', err);
          this.snackBar.open('Không thể tạo bình luận. Vui lòng thử lại.', 'Đóng', {
            duration: 3000,
            panelClass: ['error-snackbar'],
          });
        },
      });
    }
  }

  toggleReplyForm(comment: CommentResponse): void {
    this.replyControlsMap.set(comment.id, this.isReplyFormShown(comment.id) ? undefined! : new FormControl('', Validators.required));
  }

  isReplyFormShown(commentId: string): boolean {
    return this.replyControlsMap.has(commentId) && this.replyControlsMap.get(commentId) !== undefined;
  }

  getReplyFormControl(commentId: string): FormControl {
    return this.replyControlsMap.get(commentId)!;
  }

  createReply(post: PostResponse, parentComment: CommentResponse): void {
    const replyControl = this.getReplyFormControl(parentComment.id);
    if (replyControl.valid) {
      const request: CommentRequest = {
        content: replyControl.value!,
        parentCommentId: parentComment.id
      };

      this.commentService.createComment(post.id, request).subscribe({
        next: (newReply: CommentResponse) => {
          this.addReplyToComment(post.comments!, newReply);
          replyControl.reset();
          this.toggleReplyForm(parentComment);
          this.snackBar.open('Phản hồi đã được tạo!', 'Đóng', {
            duration: 2000,
            panelClass: ['success-snackbar'],
          });
          // Không cập nhật parentComment.replyCount ở đây nữa vì getTotalCommentsCount sẽ tự tính
          // parentComment.replyCount = (parentComment.replyCount || 0) + 1; // <--- CÓ THỂ BỎ DÒNG NÀY
        },
        error: (err: any) => {
          console.error('Lỗi khi tạo phản hồi:', err);
          this.snackBar.open('Không thể tạo phản hồi. Vui lòng thử lại.', 'Đóng', {
            duration: 3000,
            panelClass: ['error-snackbar'],
          });
        },
      });
    }
  }

  private addReplyToComment(comments: CommentResponse[], newReply: CommentResponse): boolean {
    for (const comment of comments) {
      if (comment.id === newReply.parentCommentId) {
        if (!comment.replies) {
          comment.replies = [];
        }
        comment.replies.push(newReply);
        comment.replies.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
        return true;
      }
      if (comment.replies && this.addReplyToComment(comment.replies, newReply)) {
        return true;
      }
    }
    return false;
  }

  startEditingComment(comment: CommentResponse): void {
    this.editingCommentMap.set(comment.id, true);
    this.editCommentControlsMap.set(comment.id, new FormControl(comment.content, Validators.required));
  }

  cancelEditingComment(commentId: string): void {
    this.editingCommentMap.set(commentId, false);
    this.editCommentControlsMap.delete(commentId);
  }

  isEditingComment(commentId: string): boolean {
    return this.editingCommentMap.get(commentId) || false;
  }

  getEditCommentFormControl(commentId: string): FormControl {
    return this.editCommentControlsMap.get(commentId)!;
  }

  saveEditedComment(post: PostResponse, comment: CommentResponse): void {
    const editControl = this.getEditCommentFormControl(comment.id);
    if (editControl.valid) {
      const request: CommentRequest = {
        content: editControl.value!
      };

      this.commentService.updateComment(comment.id, request).subscribe({
        next: (updatedComment: CommentResponse) => {
          this.updateCommentInList(post.comments!, updatedComment.id!, updatedComment);
          this.cancelEditingComment(comment.id);
          this.snackBar.open('Bình luận đã được cập nhật!', 'Đóng', {
            duration: 2000,
            panelClass: ['success-snackbar'],
          });
        },
        error: (err: any) => {
          console.error('Lỗi khi cập nhật bình luận:', err);
          this.snackBar.open('Không thể cập nhật bình luận. Vui lòng thử lại.', 'Đóng', {
            duration: 3000,
            panelClass: ['error-snackbar'],
          });
        },
      });
    }
  }

  private updateCommentInList(comments: CommentResponse[], commentIdToUpdate: string, updatedData: Partial<CommentResponse>): boolean {
    for (let i = 0; i < comments.length; i++) {
      if (comments[i].id === commentIdToUpdate) {
        comments[i] = { ...comments[i], ...updatedData };
        return true;
      }
      if (comments[i].replies && this.updateCommentInList(comments[i].replies!, commentIdToUpdate, updatedData)) {
        return true;
      }
    }
    return false;
  }

  deleteComment(post: PostResponse, commentId: string): void {
    if (confirm('Bạn có chắc muốn xóa bình luận này?')) {
      this.commentService.deleteComment(commentId).subscribe({
        next: () => {
          // Chỉ cần gọi removeCommentFromList, getTotalCommentsCount sẽ tự cập nhật
          this.removeCommentFromList(post.comments!, commentId);
          this.snackBar.open('Bình luận đã được xóa!', 'Đóng', {
            duration: 2000,
            panelClass: ['success-snackbar'],
          });
        },
        error: (err: any) => {
          console.error('Lỗi khi xóa bình luận:', err);
          this.snackBar.open('Không thể xóa bình luận. Vui lòng thử lại.', 'Đóng', {
            duration: 3000,
            panelClass: ['error-snackbar'],
          });
        },
      });
    }
  }

  private removeCommentFromList(comments: CommentResponse[], commentIdToRemove: string): boolean {
    for (let i = 0; i < comments.length; i++) {
      if (comments[i].id === commentIdToRemove) {
        const wasTopLevel = (comments[i].parentCommentId === null || comments[i].parentCommentId === undefined);
        comments.splice(i, 1);
        return wasTopLevel;
      }
      if (comments[i].replies && comments[i].replies!.length > 0 && this.removeCommentFromList(comments[i].replies!, commentIdToRemove)) {
        return false;
      }
    }
    return false;
  }

  isMyComment(comment: CommentResponse): boolean {
    return this.currentUserEmail !== null && comment.userEmail === this.currentUserEmail;
  }

  getCommentUserName(comment: CommentResponse): string {
    return comment.userName || (comment.userEmail ? comment.userEmail.split('@')[0] : 'Anonymous');
  }

  getTotalCommentsCount(comments: CommentResponse[] | undefined): number {
    if (!comments || comments.length === 0) {
      return 0;
    }

    let count = 0;
    for (const comment of comments) {
      count++;
      if (comment.replies && comment.replies.length > 0) {
        count += this.getTotalCommentsCount(comment.replies);
      }
    }
    return count;
  }
}
