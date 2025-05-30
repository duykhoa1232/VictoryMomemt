// src/app/core/home/list-post/list-post.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
// import { MatButtonModule } from '@angular/material/button'; // XÓA: Không còn nút Like/Comment/Share
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
// import { MatDividerModule } from '@angular/material/divider'; // XÓA: Không còn divider cho actions/comments
// import { MatFormFieldModule } from '@angular/material/form-field'; // XÓA: Không còn trường form cho comment
// import { MatInputModule } from '@angular/material/input'; // XÓA: Không còn input cho comment

import { PostService, Post, Comment } from '../services/post.service';
import { AuthService } from '../../../auth/services/auth.service';

@Component({
  selector: 'app-post-list',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
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
    private authService: AuthService
  ) {}

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
            // Đảm bảo các thuộc tính này có giá trị mặc định nếu backend không trả về
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

  // Các phương thức tương tác (onLike, onCommentClick, onShare) đã bị loại bỏ/comment
  // vì không còn nút tương tác trong HTML.
}
