import { Component } from '@angular/core';
import { CommonModule, NgIf } from '@angular/common'; // Thêm NgIf
import { RouterModule, RouterLink } from '@angular/router'; // Thêm RouterLink

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

import { AuthService } from '../../auth/services/auth.service'; // Đảm bảo đường dẫn đúng
import { ConfirmDialogComponent, ConfirmDialogData } from '../../shared/confirm-dialog.component';
import {MatDivider} from '@angular/material/divider'; // Đảm bảo đường dẫn đúng

@Component({
  selector: 'app-header', // Selector của HeaderComponent
  standalone: true,
  imports: [
    CommonModule,
    NgIf, // Để sử dụng *ngIf
    RouterModule, // Để sử dụng routerLink
    RouterLink, // Để sử dụng routerLink
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDialogModule,
    MatDivider
  ],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent {
  constructor(
    public authService: AuthService, // Inject AuthService
    private dialog: MatDialog
  ) { }

  logout(): void {
    const dialogData: ConfirmDialogData = {
      title: 'Confirm Logout',
      message: 'Are you sure you want to log out of the application?',
      confirmButtonText: 'Log out',
      cancelButtonText: 'Hủy'
    };

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.authService.logout();
      } else {
        console.log('Logout has been canceled.');
      }
    });
  }
}




