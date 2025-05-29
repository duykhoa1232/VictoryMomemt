// src/app/core/home/home.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button'; // Để dùng mat-button
import { MatSliderModule } from '@angular/material/slider'; // Cho thanh audio
import { MatCardModule } from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon'; // Cho các card bài viết/sidebar

@Component({
  selector: 'app-home',
  standalone: true, // Nếu project của bạn là standalone
  imports: [
    CommonModule,
    MatButtonModule,
    MatSliderModule,
    MatCardModule,
    MatIconModule // THÊM MatIconModule VÀO ĐÂY

  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

  // Có thể thêm logic cho slider, ví dụ:
  formatLabel(value: number): string {
    return `${value}`; // Hoặc định dạng thời gian nếu là slider audio
  }
}
