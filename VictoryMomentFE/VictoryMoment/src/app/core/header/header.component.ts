import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import {MatDialog, MatDialogModule} from '@angular/material/dialog'; // Import MatDialog

import { AuthService } from '../../auth/services/auth.service';
import {ConfirmDialogComponent, ConfirmDialogData} from '../../shared/confirm-dialog.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDialogModule // <<< THÊM DÒNG NÀY
  ],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent {
  constructor(
    public authService: AuthService,
    private dialog: MatDialog // Inject MatDialog
  ) { }

  logout(): void { // Đổi tên phương thức để rõ ràng hơn
    const dialogData: ConfirmDialogData = {
      title: 'Xác nhận Đăng xuất',
      message: 'Bạn có chắc chắn muốn đăng xuất khỏi ứng dụng không?',
      confirmButtonText: 'Đăng xuất',
      cancelButtonText: 'Hủy'
    };

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.authService.logout(); // Gọi hàm logout đã được định nghĩa trong AuthService
      } else {
        console.log('Đăng xuất đã bị hủy.');
      }
    });
  }
}
