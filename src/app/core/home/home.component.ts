// import { Component, OnInit, ViewChild } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { MatCardModule } from '@angular/material/card';
// import { MatButtonModule } from '@angular/material/button';
// import { MatIconModule } from '@angular/material/icon';
// import { MatSnackBar } from '@angular/material/snack-bar';
// import { RouterModule } from '@angular/router'; // Thêm import này
//
// import { PostListComponent } from './list-post/list-post.component';
// import { CreatePostComponent } from './create-post/create-post.component';
// import { PostService } from './services/post.service';
// import { AuthService } from '../../auth/services/auth.service';
//
// @Component({
//   selector: 'app-home',
//   standalone: true,
//   imports: [
//     CommonModule,
//     MatCardModule,
//     MatButtonModule,
//     MatIconModule,
//     RouterModule, // Thêm RouterModule vào imports
//     PostListComponent,
//     CreatePostComponent,
//   ],
//   templateUrl: './home.component.html',
//   styleUrls: ['./home.component.css']
// })
// export class HomeComponent implements OnInit {
//   @ViewChild(PostListComponent) postListComponent!: PostListComponent;
//   isLoggedIn: boolean = false;
//
//   constructor(
//     private postService: PostService,
//     private authService: AuthService,
//     private snackBar: MatSnackBar
//   ) { }
//
//   ngOnInit(): void {
//     this.isLoggedIn = this.authService.isUserLoggedIn();
//     if (this.isLoggedIn) {
//       this.postListComponent.getPosts();
//     } else {
//       this.snackBar.open('Please login to create or view full post.', "Log in", {
//         duration: 5000,
//       }).onAction().subscribe(() => {
//         window.location.href = '/login'; // Sẽ sửa ở bước tiếp theo
//       });
//     }
//   }
//
//   onPostCreated(): void {
//     console.log('New post has been created!');
//     if (this.isLoggedIn && this.postListComponent) {
//       this.postListComponent.getPosts();
//     } else {
//       this.snackBar.open('You need to login to refresh the post list.', "Log in", {
//         duration: 5000,
//       }).onAction().subscribe(() => {
//         window.location.href = '/login'; // Sẽ sửa ở bước tiếp theo
//       });
//     }
//   }
//
//
// }
//
//
//
//
//
//
//
//
//
//
//
//
//


import { Component, OnInit, ViewChild } from '@angular/core';
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
    public dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isUserLoggedIn();
    if (this.isLoggedIn) {
      this.postListComponent.getPosts();
    } else {
      this.snackBar.open('Please login to create or view full post.', "Log in", {
        duration: 5000,
      }).onAction().subscribe(() => {
        window.location.href = '/login';
      });
    }
  }

  onPostCreated(): void {
    console.log('New post has been created!');
    if (this.isLoggedIn && this.postListComponent) {
      this.postListComponent.getPosts();
    } else {
      this.snackBar.open('You need to login to refresh the post list.', "Log in", {
        duration: 5000,
      }).onAction().subscribe(() => {
        window.location.href = '/login';
      });
    }
  }

  openCreatePostDialog(): void {
    if (!this.isLoggedIn) {
      this.snackBar.open('Please login to create a post.', "Log in", {
        duration: 5000,
      }).onAction().subscribe(() => {
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
