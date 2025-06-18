// src/app/shared/pipes/time-ago.pipe.ts (hoặc thư mục tương ứng)

import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'timeAgo',
  standalone: true // Nếu bạn đang sử dụng standalone components/pipes
})
export class TimeAgoPipe implements PipeTransform {
  transform(value: string | Date | number): string {
    if (!value) {
      return '';
    }

    const date = new Date(value);
    const now = new Date();
    const seconds = Math.floor((now.getTime() - date.getTime()) / 1000);

    let interval = seconds / 31536000; // Số giây trong 1 năm

    if (interval > 1) {
      return Math.floor(interval) + (Math.floor(interval) === 1 ? ' năm trước' : ' năm trước');
    }
    interval = seconds / 2592000; // Số giây trong 1 tháng (30 ngày)
    if (interval > 1) {
      return Math.floor(interval) + (Math.floor(interval) === 1 ? ' tháng trước' : ' tháng trước');
    }
    interval = seconds / 604800; // Số giây trong 1 tuần
    if (interval > 1) {
      return Math.floor(interval) + (Math.floor(interval) === 1 ? ' tuần trước' : ' tuần trước');
    }
    interval = seconds / 86400; // Số giây trong 1 ngày
    if (interval > 1) {
      const days = Math.floor(interval);
      if (days === 1) {
        return 'Hôm qua';
      } else {
        return days + ' ngày trước';
      }
    }
    interval = seconds / 3600; // Số giây trong 1 giờ
    if (interval > 1) {
      return Math.floor(interval) + ' giờ trước';
    }
    interval = seconds / 60; // Số giây trong 1 phút
    if (interval > 1) {
      return Math.floor(interval) + ' phút trước';
    }
    return Math.floor(seconds) + ' giây trước';
  }
}
