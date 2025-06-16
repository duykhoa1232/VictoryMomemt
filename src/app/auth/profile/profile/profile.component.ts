import { Component, OnInit } from '@angular/core';
import { MatIcon } from '@angular/material/icon';
import { MatTab, MatTabGroup } from '@angular/material/tabs';
import { MatCard, MatCardContent, MatCardHeader, MatCardTitle } from '@angular/material/card';
import { MatButton, MatIconButton } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import {ProfileResponse} from '../../../shared/models/profile.model';
import {UserService} from '../../services/user.service';

@Component({
  selector: 'app-profile',
  standalone: true, // Assuming this is a standalone component
  imports: [
    MatIcon,
    MatTab,
    MatTabGroup,
    MatCard,
    MatCardHeader,
    MatCardContent,
    MatIconButton,
    MatButton,
    MatCardTitle,
    RouterLink
  ],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {

  userProfile: ProfileResponse | null = null; // Biến để lưu trữ dữ liệu profile

  constructor(private userService: UserService) { } // Inject UserService

  ngOnInit(): void {
    this.loadUserProfile();
  }

  loadUserProfile(): void {
    // Gọi API để lấy dữ liệu profile của người dùng hiện tại
    this.userService.getCurrentUserProfile().subscribe({
      next: (data) => {
        this.userProfile = data; // Gán dữ liệu nhận được vào biến userProfile
        console.log('User Profile fetched successfully:', this.userProfile); // In ra console để kiểm tra
      },
      error: (err) => {
        console.error('Error fetching user profile:', err);
        // Xử lý lỗi, ví dụ: hiển thị thông báo lỗi cho người dùng
        // Có thể kiểm tra err.status (ví dụ: 401 Unauthorized)
      }
    });
  }

  // Hàm helper để định dạng ngày từ chuỗi ISO 8601
  formatDate(dateString: string | undefined): string {
    if (!dateString) {
      return 'N/A';
    }
    // Lấy phần ngày thôi nếu chuỗi có cả thời gian (ví dụ: "2024-06-16T10:30:00")
    const datePart = dateString.split('T')[0];
    const [year, month, day] = datePart.split('-');
    return `${day}/${month}/${year}`; // Định dạng DD/MM/YYYY
  }

  // Bạn có thể thêm các phương thức xử lý sự kiện khác ở đây
  // Ví dụ: handleEditProfile()
}
