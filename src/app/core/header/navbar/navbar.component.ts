import {Component, OnInit} from '@angular/core';
import {RouterLink, RouterModule} from '@angular/router';


import {CommonModule, NgIf} from '@angular/common';
import {HardcodeAuthenticationService} from '../../../auth/services/hardcode-authentication.service';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatMenuModule} from '@angular/material/menu';


@Component({
  selector: 'app-navbar',
  imports: [
    RouterLink,
    NgIf,
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule // Thêm MatMenuModule
  ],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent  implements OnInit{
  isUserLoggedIn = false;
  constructor(public hardcodeAuthenticationService: HardcodeAuthenticationService) {

  }
  ngOnInit(){
    this.isUserLoggedIn = this.hardcodeAuthenticationService.isUserLoggedIn();

  }
}
