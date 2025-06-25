//
//
//
// import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core'; // ĐÃ SỬA: Thêm AfterViewInit
// import { CommonModule } from '@angular/common';
// import { MatCardModule } from '@angular/material/card';
// import { MatButtonModule } from '@angular/material/button';
// import { MatIconModule } from '@angular/material/icon';
// import { MatSnackBar } from '@angular/material/snack-bar';
// import { RouterModule } from '@angular/router';
// import { MatDialog } from '@angular/material/dialog';
//
// import { PostListComponent } from './list-post/list-post.component';
// import { CreatePostComponent } from './create-post/create-post.component';
// import { PostService } from './services/post.service'; // Đảm bảo đường dẫn này đúng
// import { AuthService } from '../../auth/services/auth.service';
// import {TranslatePipe, TranslateService} from '@ngx-translate/core';
// import {I18nService} from './services/i18n.service'; // Đảm bảo đường dẫn này đúng
//
// @Component({
//   selector: 'app-home',
//   standalone: true,
//   imports: [
//     CommonModule,
//     MatCardModule,
//     MatButtonModule,
//     MatIconModule,
//     RouterModule,
//     PostListComponent,
//     TranslatePipe,
//     // Đảm bảo PostListComponent được import và thêm vào imports
//   ],
//   templateUrl: './home.component.html',
//   styleUrls: ['./home.component.css']
// })
// export class HomeComponent implements OnInit, AfterViewInit { // ĐÃ SỬA: Implement AfterViewInit
//   @ViewChild(PostListComponent) postListComponent!: PostListComponent;
//   isLoggedIn: boolean = false;
//
//   constructor(
//     private postService: PostService, // Mặc dù PostService không được dùng trực tiếp ở đây, nếu có dùng sau này thì giữ lại
//     private authService: AuthService,
//     private snackBar: MatSnackBar,
//     public dialog: MatDialog,
//   private in18n:I18nService,
//   ) {
//
//   }
//
//   ngOnInit(): void {
//     this.isLoggedIn = this.authService.isUserLoggedIn();
//     // Logic tải bài viết ban đầu sẽ được chuyển xuống ngAfterViewInit
//   }
//
//   ngAfterViewInit(): void { // ĐÃ SỬA: Thêm ngAfterViewInit
//     // Đảm bảo postListComponent đã được khởi tạo
//     if (this.isLoggedIn && this.postListComponent) {
//       console.log("HomeComponent: Calling getPosts from PostListComponent in ngAfterViewInit.");
//       this.postListComponent.getPosts();
//     } else if (!this.isLoggedIn) {
//       this.snackBar.open('Please login to create or view full post.', "Log in", {
//         duration: 5000,
//       }).onAction().subscribe(() => {
//         window.location.href = '/login';
//       });
//     }
//   }
//
//   onPostCreated(): void {
//     console.log('New post has been created!');
//     if (this.isLoggedIn && this.postListComponent) {
//       // Khi một bài viết mới được tạo, yêu cầu PostListComponent tải lại bài viết
//       this.postListComponent.currentPage = 0; // Đặt lại trang về 0
//       this.postListComponent.posts = []; // Xóa bài cũ để tải lại từ đầu
//       this.postListComponent.getPosts();
//     } else {
//       this.snackBar.open('You need to login to refresh the post list.', "Log in", {
//         duration: 5000,
//       }).onAction().subscribe(() => {
//         window.location.href = '/login';
//       });
//     }
//   }
//
//   openCreatePostDialog(): void {
//     if (!this.isLoggedIn) {
//       this.snackBar.open('Please login to create a post.', "Log in", {
//         duration: 5000,
//       }).onAction().subscribe(() => {
//         window.location.href = '/login';
//       });
//       return;
//     }
//
//     const dialogRef = this.dialog.open(CreatePostComponent, {
//       width: '600px',
//       data: { postCreated: () => this.onPostCreated() },
//     });
//
//     dialogRef.afterClosed().subscribe(result => {
//       if (result) {
//         this.onPostCreated(); // Nếu dialog đóng với kết quả (nghĩa là post đã được tạo)
//       }
//     });
//   }
// }




import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RouterModule } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';

import { PostListComponent } from './list-post/list-post.component';
import { CreatePostComponent } from './create-post/create-post.component';
import { PostService } from './services/post.service';
import { AuthService } from '../../auth/services/auth.service';
import { TranslatePipe } from '@ngx-translate/core';
import { I18nService } from './services/i18n.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    RouterModule,
    PostListComponent,
    TranslatePipe,
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  @ViewChild(PostListComponent) postListComponent!: PostListComponent;
  isLoggedIn: boolean = false;

  constructor(
    private postService: PostService,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    public dialog: MatDialog,
    private i18n: I18nService,
  ) {}

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isUserLoggedIn();
  }

  // ngAfterViewInit(): void {
  //   if (this.isLoggedIn && this.postListComponent) {
  //     console.log("HomeComponent: Calling getPosts from PostListComponent in ngAfterViewInit.");
  //     this.postListComponent.getPosts();
  //   } else if (!this.isLoggedIn) {
  //     this.snackBar.open(
  //       this.i18n.instant('HOME_SNACKBAR_LOGIN_REQUIRED_VIEW_POSTS'),
  //       'Đóng',
  //       { duration: 3000, panelClass: ['error-snackbar'] }
  //     ).onAction().subscribe(() => {
  //       window.location.href = '/login';
  //     });
  //   }
  // }

  onPostCreated(): void {
    console.log('New post has been created!');
    if (this.isLoggedIn && this.postListComponent) {
      this.postListComponent.currentPage = 0;
      this.postListComponent.posts = [];
      this.postListComponent.getPosts();
    } else {
      this.snackBar.open(
        this.i18n.instant('HOME_SNACKBAR_LOGIN_REQUIRED_CREATE_POST'),
        'Đóng',
        { duration: 3000, panelClass: ['error-snackbar'] }
      ).onAction().subscribe(() => {
        window.location.href = '/login';
      });
    }
  }

  openCreatePostDialog(): void {
    if (!this.isLoggedIn) {
      this.snackBar.open(
        this.i18n.instant('HOME_SNACKBAR_LOGIN_REQUIRED_CREATE_POST'),
        'Đóng',
        { duration: 3000, panelClass: ['error-snackbar'] }
      ).onAction().subscribe(() => {
        window.location.href = '/login';
      });
      return;
    }

    const dialogRef = this.dialog.open(CreatePostComponent, {
      width: '600px',
      data: { postCreated: () => this.onPostCreated() },
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.onPostCreated();
      }
    });
  }
}
