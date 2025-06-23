//
//
//
// import {Component, OnInit, Input, OnChanges, SimpleChanges, ViewChild, AfterViewInit,OnDestroy} from '@angular/core';
// import { CommonModule, DatePipe } from '@angular/common';
// import { MatCardModule } from '@angular/material/card';
// import { MatIconModule } from '@angular/material/icon';
// import { MatMenuModule } from '@angular/material/menu';
// import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
// import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
// import { MatDialog, MatDialogModule } from '@angular/material/dialog';
// import { MatButtonModule } from '@angular/material/button';
// import { Page, PostResponse } from '../../../shared/models/post.model';
// import { AuthService } from '../../../auth/services/auth.service';
//
// import { ConfirmationDialogComponent, ConfirmationDialogData } from '../../../shared/confirmation-dialog.component';
// import { EditPostDialogComponent, EditPostDialogData } from '../edit-post-dialog/edit-post-dialog.component';
// import { PostService } from '../services/post.service';
// import { Observable } from 'rxjs';
// import { I18nService } from '../services/i18n.service';
// import { TranslatePipe } from '@ngx-translate/core';
// import {CommentSectionComponent} from './comment-section/comment-section.component';
// import {CommentResponse} from '../../../shared/models/comment.model';
// import {
//   trigger,
//   transition,
//   style,
//   animate
// } from '@angular/animations';
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
//     MatButtonModule,
//     DatePipe,
//     TranslatePipe,
//     CommentSectionComponent,
//   ],
//   templateUrl: './list-post.component.html',
//   styleUrls: ['./list-post.component.css'],
//   animations: [ // 👈 Đúng vị trí ở đây!
//     trigger('fadeIn', [
//       transition(':enter', [
//         style({ opacity: 0, transform: 'translateY(10px)' }),
//         animate('300ms ease-out', style({ opacity: 1, transform: 'translateY(0)' })),
//       ]),
//     ]),
//   ],
// })
// export class PostListComponent implements OnInit, OnChanges, OnDestroy, AfterViewInit {
//   @Input() userEmail: string | undefined;
//   @ViewChild('loadMoreTrigger', { static: false }) loadMoreTrigger: any;
//   private intersectionObserver?: IntersectionObserver;
//
//   posts: PostResponse[] = [];
//   isLoading: boolean = false;
//   currentUserId: string | null = null;
//   currentUserEmail: string | null = null;
//
//   totalPosts: number = 0;
//   pageSize: number = 10;
//   currentPage: number = 0;
//   sort: string = 'createdAt,desc';
//   commentVisibilityMap = new Map<string, boolean>();
//
//   constructor(
//     private postService: PostService,
//     private snackBar: MatSnackBar,
//     private authService: AuthService,
//     private dialog: MatDialog,
//     private i18n: I18nService,
//   ) {}
//
//   ngAfterViewInit(): void {
//     if (this.loadMoreTrigger) {
//       this.intersectionObserver = new IntersectionObserver(entries => {
//         if (entries[0].isIntersecting) {
//           this.loadMorePosts();
//         }
//       });
//
//       this.intersectionObserver.observe(this.loadMoreTrigger.nativeElement);
//     }
//   }
//   ngOnDestroy(): void {
//     this.intersectionObserver?.disconnect();
//   }
//
//
//   ngOnInit(): void {
//     this.currentUserId = this.authService.getCurrentUserId();
//     this.currentUserEmail = this.authService.getCurrentUserEmail();
//   }
//
//   ngOnChanges(changes: SimpleChanges): void {
//     if (changes['userEmail']) {
//       this.currentPage = 0;
//       this.posts = [];
//       this.getPosts();
//     }
//   }
//
//   getPosts(): void {
//     this.isLoading = true;
//     let postsObservable: Observable<Page<PostResponse>>;
//
//     if (this.userEmail) {
//       postsObservable = this.postService.getPostsByUserEmail(this.userEmail, this.currentPage, this.pageSize, this.sort);
//     } else {
//       postsObservable = this.postService.getAllPosts(this.currentPage, this.pageSize, this.sort);
//     }
//
//     postsObservable.subscribe({
//       next: (response: Page<PostResponse>) => {
//         if (this.currentPage === 0) {
//           this.posts = response.content.map(this.mapPostData);
//         } else {
//           this.posts = this.posts.concat(response.content.map(this.mapPostData));
//         }
//
//         this.totalPosts = response.totalElements;
//         this.isLoading = false;
//
//         if (response.content.length < this.pageSize && this.posts.length < this.totalPosts) {
//           this.totalPosts = this.posts.length;
//         }
//       },
//       error: (err: any) => {
//         console.error('Lỗi khi tải bài đăng:', err);
//         this.isLoading = false;
//         this.snackBar.open(
//           this.i18n.instant('POST_LIST_SNACKBAR.LOAD_ERROR'),
//           'Đóng',
//           { duration: 3000, panelClass: ['error-snackbar'] }
//         );
//       },
//     });
//   }
//
//   loadMorePosts(): void {
//     if (this.posts.length < this.totalPosts && !this.isLoading) {
//       this.currentPage++;
//       this.getPosts();
//     } else if (this.posts.length >= this.totalPosts && !this.isLoading) {
//       this.snackBar.open(
//         this.i18n.instant('POST_LIST_SNACKBAR.NO_MORE_POSTS') || 'Đã tải hết bài viết.',
//         'Đóng',
//         { duration: 3000, panelClass: ['info-snackbar'] }
//       );
//     }
//   }
//
//
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
//
//       // ✅ GIỮ LẠI COMMENT từ backend nếu có
//       comments: post.comments || [],
//     };
//   }
//
//   getUserName(post: PostResponse): string {
//     return post.author?.name || (post.author?.email ? post.author.email.split('@')[0] : 'Anonymous');
//   }
//
//   isMyPost(post: PostResponse): boolean {
//     return this.currentUserEmail !== null && post.author?.email === this.currentUserEmail;
//   }
//
//   onLike(post: PostResponse): void {
//     this.postService.toggleLike(post.id).subscribe({
//       next: (updatedPost: PostResponse) => {
//         const index = this.posts.findIndex(p => p.id === updatedPost.id);
//         if (index !== -1) {
//           this.posts[index] = updatedPost;
//         }
//       },
//       error: (err: any) => {
//         console.error('Lỗi khi like/unlike bài đăng:', err);
//         this.snackBar.open(
//           this.i18n.instant('POST_LIST_SNACKBAR.LIKE_ERROR'),
//           'Đóng',
//           { duration: 3000, panelClass: ['error-snackbar'] }
//         );
//       },
//     });
//   }
//
//   deletePost(post: PostResponse): void {
//     const dialogData: ConfirmationDialogData = {
//       title: 'Xác nhận xóa bài đăng',
//       message: `Bạn có chắc muốn xóa bài đăng của ${this.getUserName(post)} với nội dung: "${post.content}"?`,
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
//             this.snackBar.open(
//               this.i18n.instant('POST_LIST_SNACKBAR.DELETE_SUCCESS'),
//               'Close',
//               { duration: 3000, panelClass: ['success-snackbar'] }
//             );
//             this.posts = this.posts.filter(p => p.id !== post.id);
//             this.totalPosts--;
//           },
//           error: (err: any) => {
//             console.error('Lỗi khi xóa bài đăng:', err);
//             this.snackBar.open(
//               this.i18n.instant('POST_LIST_SNACKBAR.DELETE_ERROR'),
//               'Close',
//               { duration: 3000, panelClass: ['error-snackbar'] }
//             );
//           },
//         });
//       }
//     });
//   }
//   toggleComments(post: PostResponse): void {
//     const current = this.commentVisibilityMap.get(post.id) || false;
//     this.commentVisibilityMap.set(post.id, !current);
//   }
//
//   areCommentsShown(postId: string): boolean {
//     return this.commentVisibilityMap.get(postId) || false;
//   }
//
//   getTotalCommentsCount(comments: CommentResponse[] = []): number {
//     let total = comments.length;
//     for (const comment of comments) {
//       if (comment.replies?.length) {
//         total += this.getTotalCommentsCount(comment.replies);
//       }
//     }
//     return total;
//   }
//   editPost(post: PostResponse): void {
//     const dialogRef = this.dialog.open(EditPostDialogComponent, {
//       width: '600px',
//       data: { post: { ...post } } as EditPostDialogData,
//     });
//
//     dialogRef.afterClosed().subscribe((result: { formData: FormData } | null) => {
//       if (result) {
//         this.postService.updatePost(post.id, result.formData).subscribe({
//           next: (updatedPost: PostResponse) => {
//             const index = this.posts.findIndex(p => p.id === updatedPost.id);
//             if (index !== -1) {
//               this.posts[index] = updatedPost;
//             }
//             this.snackBar.open(
//               this.i18n.instant('POST_LIST_SNACKBAR.UPDATE_SUCCESS'),
//               'Close',
//               { duration: 3000, panelClass: ['success-snackbar'] }
//             );
//           },
//           error: (err: any) => {
//             console.error('Lỗi khi cập nhật bài đăng:', err);
//             this.snackBar.open(
//               this.i18n.instant('POST_LIST_SNACKBAR.UPDATE_ERROR'),
//               'Close',
//               { duration: 3000, panelClass: ['error-snackbar'] }
//             );
//           },
//         });
//       }
//     });
//   }
// }


import {
  Component,
  OnInit,
  Input,
  OnChanges,
  SimpleChanges,
  ViewChild,
  AfterViewInit,
  OnDestroy, QueryList, ElementRef, ViewChildren,
} from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { Page, PostResponse } from '../../../shared/models/post.model';
import { AuthService } from '../../../auth/services/auth.service';
import {
  ConfirmationDialogComponent,
  ConfirmationDialogData,
} from '../../../shared/confirmation-dialog.component';
import {
  EditPostDialogComponent,
  EditPostDialogData,
} from '../edit-post-dialog/edit-post-dialog.component';
import { PostService } from '../services/post.service';
import {last, Observable} from 'rxjs';
import { I18nService } from '../services/i18n.service';
import { TranslatePipe } from '@ngx-translate/core';
import { CommentSectionComponent } from './comment-section/comment-section.component';
import { CommentResponse } from '../../../shared/models/comment.model';
import {
  trigger,
  transition,
  style,
  animate,
} from '@angular/animations';

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
    DatePipe,
    TranslatePipe,
    CommentSectionComponent,
  ],
  templateUrl: './list-post.component.html',
  styleUrls: ['./list-post.component.css'],
  animations: [
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(10px)' }),
        animate('300ms ease-out', style({ opacity: 1, transform: 'translateY(0)' })),
      ]),
    ]),
  ],
})
export class PostListComponent implements OnInit, OnChanges, OnDestroy, AfterViewInit {
  @Input() userEmail: string | undefined;
  @ViewChildren('loadMoreTrigger') loadMoreTriggers!: QueryList<ElementRef>;

  posts: PostResponse[] = [];
  isLoading = false;
  currentUserId: string | null = null;
  currentUserEmail: string | null = null;

  totalPosts = 0;
  pageSize = 10;
  currentPage = 0;
  sort = 'createdAt,desc';

  private intersectionObserver?: IntersectionObserver;
  commentVisibilityMap = new Map<string, boolean>();

  constructor(
    private postService: PostService,
    private snackBar: MatSnackBar,
    private authService: AuthService,
    private dialog: MatDialog,
    private i18n: I18nService,
  ) {}

  ngOnInit(): void {
    this.currentUserId = this.authService.getCurrentUserId();
    this.currentUserEmail = this.authService.getCurrentUserEmail();
    this.refreshAllPosts();
  }
  ngAfterViewInit(): void {
    this.setupIntersectionObserver();

    // Theo dõi thay đổi của trigger (DOM)
    this.loadMoreTriggers.changes.subscribe(() => {
      this.observeLastTrigger(); // Chỉ cần gọi 1 lần là đủ
    });

    // Dùng ResizeObserver để kiểm tra nếu trang chưa đủ scroll
    const resizeObserver = new ResizeObserver(() => {
      if (!this.isLoading && !this.isScrollable() && this.posts.length < this.totalPosts) {
        this.loadMorePosts();
      }
    });

    const postContainer = document.querySelector('.post-list-container');
    if (postContainer) resizeObserver.observe(postContainer);

    // Gọi observe sau lần đầu load bài viết
    setTimeout(() => this.observeLastTrigger(), 800);
  }

  isScrollable(): boolean {
    const el = document.querySelector('.post-list-container')!;
    return el.scrollHeight > el.clientHeight;
  }


  ngOnDestroy(): void {
    this.intersectionObserver?.disconnect();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['userEmail']) {
      this.refreshAllPosts();
    }
  }

  refreshAllPosts(): void {
    this.posts = [];
    this.currentPage = 0;
    this.totalPosts = 0;
    this.getPosts();
  }

  setupIntersectionObserver(): void {
    if (this.intersectionObserver) {
      this.intersectionObserver.disconnect();
    }

    this.intersectionObserver = new IntersectionObserver(entries => {
      if (entries[0].isIntersecting) {
        console.log('👀 Trigger seen, loading more...');
        this.loadMorePosts();
      }
    });
  }
  isMyPost(post: PostResponse): boolean {
    return this.currentUserEmail !== null && post.author?.email === this.currentUserEmail;
  }
  loadMorePosts(): void {
    if (this.isLoading || this.posts.length >= this.totalPosts) return;
    this.currentPage++;
    this.getPosts();
  }

  getPosts(): void {
    this.isLoading = true;

    const postsObservable = this.userEmail
      ? this.postService.getPostsByUserEmail(this.userEmail, this.currentPage, this.pageSize, this.sort)
      : this.postService.getAllPosts(this.currentPage, this.pageSize, this.sort);

    postsObservable.subscribe({
      next: (response: Page<PostResponse>) => {
        const newPosts = response.content.map(this.mapPostData);
        this.posts = this.posts.concat(newPosts);
        this.totalPosts = response.totalElements;
        this.isLoading = false;

        this.observeLastTrigger(); // ✅ gọi lại sau khi thêm bài viết
      },
      error: (err) => {
        console.error('Lỗi tải bài viết:', err);
        this.snackBar.open('Lỗi tải bài viết', 'Đóng', { duration: 3000 });
        this.isLoading = false;
      },
    });
  }
  observeLastTrigger(): void {
    const lastTrigger = this.loadMoreTriggers.last;
    if (lastTrigger && this.intersectionObserver) {
      this.intersectionObserver.unobserve(lastTrigger.nativeElement); // unobserve cũ
      this.intersectionObserver.observe(lastTrigger.nativeElement);   // observe mới
      console.log('🔁 Observing trigger:', lastTrigger.nativeElement);
    }
  }


  private mapPostData(post: PostResponse): PostResponse {
    const displayUserName = post.author?.name || (post.author?.email?.split('@')[0] || 'Ẩn danh');
    return {
      ...post,
      author: { ...post.author, name: displayUserName },
      likeCount: post.likeCount || 0,
      commentCount: post.commentCount || 0,
      shareCount: post.shareCount || 0,
      imageUrls: post.imageUrls || [],
      videoUrls: post.videoUrls || [],
      audioUrls: post.audioUrls || [],
      comments: post.comments || [],
    };
  }

  getUserName(post: PostResponse): string {
    return post.author?.name || (post.author?.email?.split('@')[0] || 'Ẩn danh');
  }

  getTotalCommentsCount(comments: CommentResponse[] = []): number {
    let total = comments.length;
    for (const comment of comments) {
      if (comment.replies?.length) {
        total += this.getTotalCommentsCount(comment.replies);
      }
    }
    return total;
  }

  toggleComments(post: PostResponse): void {
    const current = this.commentVisibilityMap.get(post.id) || false;
    this.commentVisibilityMap.set(post.id, !current);
  }

  areCommentsShown(postId: string): boolean {
    return this.commentVisibilityMap.get(postId) || false;
  }

  onLike(post: PostResponse): void {
    this.postService.toggleLike(post.id).subscribe({
      next: (updatedPost) => {
        const index = this.posts.findIndex(p => p.id === updatedPost.id);
        if (index !== -1) this.posts[index] = updatedPost;
      },
      error: (err) => {
        console.error('Lỗi khi like bài viết:', err);
        this.snackBar.open('Lỗi khi like bài viết', 'Đóng', { duration: 3000 });
      }
    });
  }
  trackByPostId(index: number, post: PostResponse): string {
    return post.id;
  }

  editPost(post: PostResponse): void {
    const dialogRef = this.dialog.open(EditPostDialogComponent, {
      width: '600px',
      data: { post: { ...post } } as EditPostDialogData,
    });

    dialogRef.afterClosed().subscribe((result: { formData: FormData } | null) => {
      if (result) {
        this.postService.updatePost(post.id, result.formData).subscribe({
          next: (updatedPost) => {
            const index = this.posts.findIndex(p => p.id === updatedPost.id);
            if (index !== -1) this.posts[index] = updatedPost;
            this.snackBar.open('Cập nhật thành công', 'Đóng', { duration: 3000 });
          },
          error: (err) => {
            console.error('Lỗi cập nhật:', err);
            this.snackBar.open('Cập nhật thất bại', 'Đóng', { duration: 3000 });
          }
        });
      }
    });
  }

  deletePost(post: PostResponse): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      width: '350px',
      data: {
        title: 'Xác nhận xóa bài đăng',
        message: `Bạn có chắc muốn xóa bài đăng này?`,
        confirmText: 'Xóa',
        cancelText: 'Hủy',
        confirmButtonColor: 'warn',
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.postService.deletePost(post.id).subscribe({
          next: () => {
            this.posts = this.posts.filter(p => p.id !== post.id);
            this.totalPosts--;
            this.snackBar.open('Đã xóa bài viết', 'Đóng', { duration: 3000 });
          },
          error: (err) => {
            console.error('Lỗi xóa bài viết:', err);
            this.snackBar.open('Xóa thất bại', 'Đóng', { duration: 3000 });
          }
        });
      }
    });
  }

  protected readonly last = last;
}
