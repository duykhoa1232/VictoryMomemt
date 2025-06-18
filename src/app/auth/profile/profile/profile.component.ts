


// src/app/auth/profile/profile/profile.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink, ActivatedRoute, Router } from '@angular/router'; // THÊM ActivatedRoute, Router
import { ProfileResponse } from '../../../shared/models/profile.model'; // Đảm bảo đường dẫn đúng
import { UserService } from '../../services/user.service'; // Đảm bảo đường dẫn đúng
import { NgIf, CommonModule, DatePipe } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CreatePostComponent } from '../../../core/home/create-post/create-post.component'; // Đảm bảo đường dẫn đúng
import { PostListComponent } from '../../../core/home/list-post/list-post.component'; // Đảm bảo đường dẫn đúng
import { AuthService } from '../../services/auth.service'; // Đảm bảo đường dẫn đúng
import { Subject, takeUntil, switchMap, tap, Observable } from 'rxjs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner'; // Đổi từ MatProgressSpinner sang MatProgressSpinnerModule

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    MatIconModule,
    MatTabsModule,
    MatCardModule,
    MatButtonModule,
    RouterLink,
    PostListComponent,
    NgIf,
    CommonModule,
    MatProgressSpinnerModule, // Sử dụng MatProgressSpinnerModule
    // DatePipe // Cần DatePipe nếu muốn dùng pipe trong template
  ],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit, OnDestroy {

  userProfile: ProfileResponse | null = null;
  isMyProfile: boolean = false;
  isLoading: boolean = true;
  private destroy$ = new Subject<void>();

  constructor(
    private userService: UserService,
    private authService: AuthService,
    public dialog: MatDialog,
    private snackBar: MatSnackBar,
    private route: ActivatedRoute,
    public router: Router // <--- SỬA TỪ private SANG public
  ) { }

  ngOnInit(): void {
    this.route.paramMap.pipe(
      tap(() => {
        this.isLoading = true;
        this.userProfile = null;
        this.isMyProfile = false;
      }),
      switchMap(params => {
        const userEmailFromRoute = params.get('userEmail');

        if (userEmailFromRoute) {
          return this.userService.getUserProfileByEmail(userEmailFromRoute);
        } else {
          return this.userService.getCurrentUserProfile();
        }
      }),
      takeUntil(this.destroy$)
    ).subscribe({
      next: (data) => {
        this.userProfile = data;
        this.isMyProfile = (this.userProfile?.email === this.authService.getCurrentUserEmail());
        this.isLoading = false;
        console.log('User Profile fetched successfully:', this.userProfile);
      },
      error: (err) => {
        console.error('Error fetching user profile:', err);
        this.snackBar.open('Lỗi khi tải hồ sơ người dùng.', 'Đóng', { duration: 3000 });
        this.isLoading = false;
        if (err.status === 404) {
          this.router.navigate(['/home']);
        } else if (err.status === 401 || err.status === 403) {
          this.router.navigate(['/login']);
        }
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) {
      return 'N/A';
    }
    const date = new Date(dateString);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
  }

  openCreatePostDialog(): void {
    if (!this.authService.isUserLoggedIn()) {
      this.snackBar.open('Vui lòng đăng nhập để tạo bài đăng.', "Đăng nhập", {
        duration: 5000,
      }).onAction().subscribe(() => {
        this.router.navigate(['/login']);
      });
      return;
    }

    const dialogRef = this.dialog.open(CreatePostComponent, {
      width: '600px',
      data: {},
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.onPostCreated();
      }
    });
  }

  onPostCreated(): void {
    console.log('Bài đăng mới đã được tạo từ trang profile!');
    this.snackBar.open('Bài đăng đã được tạo thành công!', 'Đóng', { duration: 2000 });
    this.loadProfileBasedOnRoute();
  }

  private loadProfileBasedOnRoute(): void {
    const userEmailFromRoute = this.route.snapshot.paramMap.get('userEmail');
    let profileObservable: Observable<ProfileResponse>;

    if (userEmailFromRoute) {
      profileObservable = this.userService.getUserProfileByEmail(userEmailFromRoute);
    } else {
      profileObservable = this.userService.getCurrentUserProfile();
    }

    profileObservable.pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (data) => {
        this.userProfile = data;
        this.isMyProfile = (this.userProfile?.email === this.authService.getCurrentUserEmail());
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error fetching user profile after post creation:', err);
        this.snackBar.open('Lỗi khi tải lại hồ sơ người dùng.', 'Đóng', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }
}
