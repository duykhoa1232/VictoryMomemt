

// src/app/core/header/header.component.ts
import { Component, OnInit, OnDestroy, ViewChild, ElementRef, HostBinding, HostListener } from '@angular/core';
import { CommonModule, NgIf } from '@angular/common';
import { RouterModule, RouterLink, Router } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Subject, Subscription, debounceTime, distinctUntilChanged, filter, switchMap, tap } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatListModule } from '@angular/material/list';

import { AuthService } from '../../auth/services/auth.service';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../shared/confirm-dialog.component';
import { UserResponse } from '../../shared/models/profile.model';
import { UserService } from '../../auth/services/user.service';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {I18nService} from '../home/services/i18n.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    NgIf,
    RouterModule,
    RouterLink,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDialogModule,
    MatDividerModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule,
    MatListModule,
    TranslatePipe,
  ],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit, OnDestroy {


  isSearchExpanded: boolean = false;
  searchControl = new FormControl('');
  searchResults: UserResponse[] = [];
  isLoadingSearch = false;
  showSearchResults = false;

  @ViewChild('searchInput') searchInput!: ElementRef<HTMLInputElement>;
  @ViewChild('searchContainer') searchContainer!: ElementRef;

  @HostBinding('class.search-active') get isSearchActive() {
    return this.isSearchExpanded;
  }

  private subscriptions: Subscription = new Subscription();
  private destroy$ = new Subject<void>();

  constructor(
    public authService: AuthService,
    private dialog: MatDialog,
    private userService: UserService,
    private router: Router,
    public i18n: I18nService // ✅ Dùng service bạn đã tạo


  ) {


  }
  switchLang(lang: string): void {
    this.i18n.switchLang(lang);
  }

  ngOnInit(): void {
    this.subscriptions.add(this.searchControl.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      filter((query): query is string => typeof query === 'string' && query.trim().length > 0),
      tap(() => {
        this.isLoadingSearch = true;
        this.showSearchResults = true;
        this.searchResults = [];
      }),
      switchMap(query => {
        return this.userService.searchUsers(query.trim()).pipe(
          takeUntil(this.destroy$)
        );
      })
    ).subscribe({
      next: (users: UserResponse[]) => {
        this.searchResults = users;
        this.isLoadingSearch = false;
      },
      error: (err) => {
        console.error('Error fetching search results:', err);
        this.searchResults = [];
        this.isLoadingSearch = false;
      }
    }));
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
    this.destroy$.next();
    this.destroy$.complete();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (this.searchContainer && !this.searchContainer.nativeElement.contains(event.target as Node)) {
      this.showSearchResults = false;
      if (!this.searchControl.value) {
        this.isSearchExpanded = false;
      }
    }
  }

  expandSearch(): void {
    if (!this.isSearchExpanded) {
      this.isSearchExpanded = true;
      setTimeout(() => {
        this.searchInput.nativeElement.focus();
        if (this.searchControl.value && this.searchControl.value.trim().length > 0) {
          this.showSearchResults = true;
        }
      }, 300);
    }
  }

  collapseSearch(): void {
    setTimeout(() => {
      if (!this.searchControl.value && document.activeElement !== this.searchInput.nativeElement) {
        this.isSearchExpanded = false;
        this.showSearchResults = false;
        this.searchResults = [];
      }
    }, 100);
  }

  clearSearch(event: Event): void {
    event.stopPropagation();
    this.searchControl.reset();
    this.searchResults = [];
    this.isLoadingSearch = false;
    this.showSearchResults = false;
    this.isSearchExpanded = false;
    this.searchInput.nativeElement.blur();
  }

  performSearch(): void {
    const query = this.searchControl.value;
    if (query && typeof query === 'string' && query.trim() !== '') {
      this.isLoadingSearch = true;
      this.showSearchResults = true;
      this.searchResults = [];
      this.userService.searchUsers(query.trim())
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (users: UserResponse[]) => {
            this.searchResults = users;
            this.isLoadingSearch = false;
          },
          error: (err) => {
            console.error('Error fetching search results on Enter:', err);
            this.searchResults = [];
            this.isLoadingSearch = false;
          }
        });
    } else {
      this.searchResults = [];
      this.showSearchResults = false;
    }
  }

  goToUserProfile(userEmail: string): void {
    this.showSearchResults = false;
    this.isSearchExpanded = false;
    this.searchControl.reset();
    this.searchResults = [];
    this.searchInput.nativeElement.blur();
    this.router.navigate(['/users', userEmail, 'profile']);
  }

  logout(): void {
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
        this.authService.logout();
      } else {
        console.log('Đăng xuất đã bị hủy.');
      }
    });
  }
}
