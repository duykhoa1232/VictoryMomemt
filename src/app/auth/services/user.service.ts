import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {ProfileRequest, ProfileResponse} from '../../shared/models/profile.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = environment.apiUrl + '/api/users'; // Đảm bảo URL này đúng với backend

  constructor(private http: HttpClient) { }

  // Phương thức này là để lấy profile của người dùng hiện tại (authenticated user)
  // Backend endpoint: GET /api/users/profile (không cần email trong path)
  // ĐÃ SỬA: Không nhận tham số
  getCurrentUserProfile(): Observable<ProfileResponse> {
    return this.http.get<ProfileResponse>(`${this.apiUrl}/profile`);
  }

  // Phương thức này là để lấy profile của một người dùng bất kỳ theo email
  // Backend endpoint: GET /api/users/{userEmail}/profile
  getUserProfileByEmail(userEmail: string): Observable<ProfileResponse> {
    return this.http.get<ProfileResponse>(`${this.apiUrl}/${userEmail}/profile`);
  }

  // Phương thức để cập nhật thông tin text của profile
  // Backend endpoint: PUT /api/users/{userEmail}/profile
  updateUserProfile(userEmail: string, request: ProfileRequest): Observable<ProfileResponse> {
    return this.http.put<ProfileResponse>(`${this.apiUrl}/${userEmail}/profile`, request);
  }

  // ĐÃ SỬA: Thêm phương thức uploadUserAvatar
  // Backend endpoint: POST /api/users/{userEmail}/avatar
  uploadUserAvatar(userEmail: string, avatarFile: File): Observable<ProfileResponse> {
    const formData = new FormData();
    formData.append('file', avatarFile, avatarFile.name); // 'file' phải khớp với @RequestParam("file") trong backend

    return this.http.post<ProfileResponse>(`${this.apiUrl}/${userEmail}/avatar`, formData);
  }
}
