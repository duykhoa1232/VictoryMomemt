// // src/app/core/home/home.component.ts
// import { Component, OnInit, ViewChild } from '@angular/core';
// import { CommonModule } from '@angular/common'; // Cần CommonModule nếu đây là standalone hoặc NgModule
// import { CreatePostComponent } from './create-post/create-post.component';
// import { PostListComponent } from './list-post/list-post.component';
// import {MatCardModule} from '@angular/material/card';
//
// @Component({
//   selector: 'app-home',
//   standalone: true, // Hoặc declarations/imports trong NgModule
//   imports: [
//     CommonModule, // Đảm bảo có CommonModule
//     CreatePostComponent,
//     PostListComponent,
//     MatCardModule,
//   ],
//   templateUrl: './home.component.html',
//   styleUrls: ['./home.component.css']
// })
// export class HomeComponent implements OnInit {
//   // Sử dụng ViewChild để truy cập instance của PostListComponent
//   // Lưu ý: Nếu PostListComponent được đặt trong *ngIf hoặc tương tự, bạn cần thêm { static: false }
//   @ViewChild(PostListComponent) postListComponent!: PostListComponent;
//
//   constructor() { }
//
//   ngOnInit(): void {
//     // Có thể thực hiện các khởi tạo khác nếu cần
//   }
//
//   onPostCreated(): void {
//     console.log('Sự kiện postCreated đã được kích hoạt từ CreatePostComponent.');
//     // Sau khi một bài đăng mới được tạo, gọi lại phương thức getPosts của PostListComponent
//     if (this.postListComponent) {
//       this.postListComponent.getPosts();
//     } else {
//       console.warn('PostListComponent chưa sẵn sàng hoặc không tìm thấy.');
//     }
//   }
// }


import { Component, OnInit, ViewChild } from '@angular/core'; // THÊM ViewChild
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

import { PostListComponent } from './list-post/list-post.component';
import { CreatePostComponent } from './create-post/create-post.component';
import { PostService } from './services/post.service'; // THÊM: Import PostService

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    PostListComponent,
    CreatePostComponent,
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  @ViewChild(PostListComponent) postListComponent!: PostListComponent; // THÊM: Tham chiếu đến PostListComponent

  constructor(private postService: PostService) { } // THÊM: Inject PostService

  ngOnInit(): void {
  }

  onPostCreated(): void {
    console.log('Bài đăng mới đã được tạo!');
    // Gọi phương thức tải bài đăng của PostListComponent để làm mới danh sách
    if (this.postListComponent) {
      this.postListComponent.getPosts();
    }
  }
}
