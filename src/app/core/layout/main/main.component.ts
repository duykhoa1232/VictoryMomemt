// src/app/layout/main/main.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import {HeaderComponent} from '../../header/header.component';
import {FooterComponent} from '../../footer/footer.component'; // Để hiển thị nội dung route

// Import các component con của layout

@Component({
  selector: 'app-main',
  standalone: true, // Nếu project của bạn là standalone
  imports: [
    CommonModule,
    RouterOutlet, // Rất quan trọng để hiển thị các route con
    HeaderComponent, // Sử dụng header component
    FooterComponent  // Sử dụng footer component
  ],
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.css']
})
export class MainComponent { }
