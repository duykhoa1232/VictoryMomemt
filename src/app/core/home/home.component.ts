// src/app/core/home/home.component.ts
import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common'; // Cần CommonModule nếu đây là standalone hoặc NgModule
import { CreatePostComponent } from './create-post/create-post.component';
import { PostListComponent } from './list-post/list-post.component';
import {MatCardModule} from '@angular/material/card';

@Component({
  selector: 'app-home',
  standalone: true, // Hoặc declarations/imports trong NgModule
  imports: [
    CommonModule, // Đảm bảo có CommonModule
    CreatePostComponent,
    PostListComponent,
    MatCardModule,
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  // Sử dụng ViewChild để truy cập instance của PostListComponent
  // Lưu ý: Nếu PostListComponent được đặt trong *ngIf hoặc tương tự, bạn cần thêm { static: false }
  @ViewChild(PostListComponent) postListComponent!: PostListComponent;

  constructor() { }

  ngOnInit(): void {
    // Có thể thực hiện các khởi tạo khác nếu cần
  }

  onPostCreated(): void {
    console.log('Sự kiện postCreated đã được kích hoạt từ CreatePostComponent.');
    // Sau khi một bài đăng mới được tạo, gọi lại phương thức getPosts của PostListComponent
    if (this.postListComponent) {
      this.postListComponent.getPosts();
    } else {
      console.warn('PostListComponent chưa sẵn sàng hoặc không tìm thấy.');
    }
  }
}
