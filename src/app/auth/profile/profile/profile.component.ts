import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router'; // Quan trọng để sử dụng routerLink trong template

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    RouterLink // Thêm RouterLink vào imports
  ],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  // Dữ liệu hồ sơ người dùng tĩnh để hiển thị
  profileData = {
    name: 'Nguyễn Văn A',
    username: '@nguyenvana',
    bio: 'Chào mừng bạn đến với trang hồ sơ của tôi! Tôi là một người yêu thích công nghệ và chia sẻ những khoảnh khắc đáng nhớ.',
    profileImageUrl: 'https://placehold.co/150x150/8BC34A/FFFFFF?text=NV', // Ảnh placeholder
    followers: 1234,
    following: 567,
    posts: 89,
    location: 'Hồ Chí Minh, Việt Nam',
    website: 'https://example.com',
    joinedDate: 'Tháng 1 năm 2023',
  };

  constructor() { }

  ngOnInit(): void {
    // Không có logic phức tạp ở đây theo yêu cầu
  }
}
