// import {
//   Component,
//   Input,
//   OnInit,
//   ChangeDetectionStrategy
// } from '@angular/core';
// import { CommonModule, DatePipe } from '@angular/common';
// import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
// import { MatCardModule } from '@angular/material/card';
// import { MatIconModule } from '@angular/material/icon';
// import { MatFormFieldModule } from '@angular/material/form-field';
// import { MatInputModule } from '@angular/material/input';
// import { MatButtonModule } from '@angular/material/button';
// import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
// import { TranslatePipe } from '@ngx-translate/core';
//
// import { PostResponse } from '../../../../shared/models/post.model';
// import { CommentService } from '../../services/comment.service';
// import { AuthService } from '../../../../auth/services/auth.service';
// import { CommentRequest, CommentResponse } from '../../../../shared/models/comment.model';
// import { I18nService } from '../../services/i18n.service';
//
// @Component({
//   selector: 'app-comment-section',
//   standalone: true,
//   imports: [
//     CommonModule,
//     ReactiveFormsModule,
//     MatCardModule,
//     MatIconModule,
//     MatFormFieldModule,
//     MatInputModule,
//     MatButtonModule,
//     MatSnackBarModule,
//     DatePipe,
//     TranslatePipe
//   ],
//   templateUrl: './comment-section.component.html',
//   styleUrls: ['./comment-section.component.css'],
//   changeDetection: ChangeDetectionStrategy.OnPush
// })
// export class CommentSectionComponent implements OnInit {
//   @Input() post!: PostResponse;
//   @Input() isShown: boolean = false;
//
//   commentControl = new FormControl('', Validators.required);
//   replyControlsMap = new Map<string, FormControl>();
//   editingCommentMap = new Map<string, boolean>();
//   editCommentControlsMap = new Map<string, FormControl>();
//
//   currentUserEmail: string | null = null;
//
//   constructor(
//     private commentService: CommentService,
//     private authService: AuthService,
//     private snackBar: MatSnackBar,
//     private i18n: I18nService
//   ) {}
//
//   ngOnInit(): void {
//     this.currentUserEmail = this.authService.getCurrentUserEmail();
//   }
//
//   getCommentUserName(comment: CommentResponse): string {
//     return comment.userName || (comment.userEmail ? comment.userEmail.split('@')[0] : 'Anonymous');
//   }
//
//   isMyComment(comment: CommentResponse): boolean {
//     return this.currentUserEmail !== null && comment.userEmail === this.currentUserEmail;
//   }
//
//   isReplyFormShown(commentId: string): boolean {
//     return this.replyControlsMap.has(commentId);
//   }
//
//   getReplyFormControl(commentId: string): FormControl {
//     return this.replyControlsMap.get(commentId)!;
//   }
//
//   toggleReplyForm(comment: CommentResponse): void {
//     if (this.isReplyFormShown(comment.id)) {
//       this.replyControlsMap.delete(comment.id);
//     } else {
//       const tagged = `@${this.getCommentUserName(comment)} `;
//       this.replyControlsMap.set(comment.id, new FormControl(tagged, Validators.required));
//     }
//   }
//
//   createComment(): void {
//     if (this.commentControl.invalid) return;
//
//     const req: CommentRequest = {
//       content: this.commentControl.value!,
//       parentCommentId: undefined
//     };
//
//     this.commentService.createComment(this.post.id, req).subscribe({
//       next: (comment: CommentResponse) => {
//         this.post.comments = [comment, ...(this.post.comments  ?? [])];
//         this.commentControl.reset();
//       }
//     });
//   }
//
//   createReply(parent: CommentResponse): void {
//     const replyControl = this.replyControlsMap.get(parent.id);
//     if (!replyControl || replyControl.invalid) return;
//
//     const req: CommentRequest = {
//       content: replyControl.value!,
//       parentCommentId: parent.id
//     };
//
//     this.commentService.createComment(this.post.id, req).subscribe({
//       next: (reply: CommentResponse) => {
//         this.addReplyToComment(this.post.comments ?? [], reply);
//         this.replyControlsMap.delete(parent.id);
//       }
//     });
//   }
//
//   private addReplyToComment(comments: CommentResponse[], reply: CommentResponse): boolean {
//     for (let comment of comments) {
//       if (comment.id === reply.parentCommentId) {
//         comment.replies = [reply, ...(comment.replies || [])];
//         return true;
//       }
//       if (comment.replies && this.addReplyToComment(comment.replies, reply)) {
//         return true;
//       }
//     }
//     return false;
//   }
//
//   startEditingComment(comment: CommentResponse): void {
//     this.editingCommentMap.set(comment.id, true);
//     this.editCommentControlsMap.set(comment.id, new FormControl(comment.content, Validators.required));
//   }
//
//   cancelEditingComment(commentId: string): void {
//     this.editingCommentMap.set(commentId, false);
//     this.editCommentControlsMap.delete(commentId);
//   }
//
//   isEditingComment(commentId: string): boolean {
//     return this.editingCommentMap.get(commentId) || false;
//   }
//
//   getEditCommentFormControl(commentId: string): FormControl {
//     return this.editCommentControlsMap.get(commentId)!;
//   }
//
//   saveEditedComment(comment: CommentResponse): void {
//     const control = this.getEditCommentFormControl(comment.id);
//     if (!control || control.invalid) return;
//
//     const req: CommentRequest = { content: control.value! };
//
//     this.commentService.updateComment(comment.id, req).subscribe({
//       next: (updated) => {
//         this.updateCommentInList(this.post.comments ?? [], updated.id!, updated);
//         this.cancelEditingComment(comment.id);
//       }
//     });
//   }
//
//   deleteComment(comment: CommentResponse): void {
//     if (!confirm('Bạn có chắc muốn xóa bình luận này?')) return;
//
//     this.commentService.deleteComment(comment.id!).subscribe(() => {
//       this.removeCommentFromList(this.post.comments ?? [] , comment.id!);
//     });
//   }
//
//   private updateCommentInList(comments: CommentResponse[], id: string, data: Partial<CommentResponse>): boolean {
//     for (let i = 0; i < comments.length; i++) {
//       if (comments[i].id === id) {
//         comments[i] = { ...comments[i], ...data };
//         return true;
//       }
//       if (comments[i].replies && this.updateCommentInList(comments[i].replies ?? [], id, data)) {
//         return true;
//       }
//     }
//     return false;
//   }
//
//   private removeCommentFromList(comments: CommentResponse[], id: string): boolean {
//     for (let i = 0; i < comments.length; i++) {
//       if (comments[i].id === id) {
//         comments.splice(i, 1);
//         return true;
//       }
//       if (comments[i].replies && this.removeCommentFromList(comments[i].replies ?? [], id)) {
//         return true;
//       }
//     }
//     return false;
//   }
// }


import {
  Component,
  Input,
  OnInit,
  ChangeDetectionStrategy
} from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslatePipe } from '@ngx-translate/core';

import { PostResponse } from '../../../../shared/models/post.model';
import { CommentService } from '../../services/comment.service';
import { AuthService } from '../../../../auth/services/auth.service';
import { CommentRequest, CommentResponse } from '../../../../shared/models/comment.model';
import { I18nService } from '../../services/i18n.service';
import {ConfirmationDialogComponent, ConfirmationDialogData} from '../../../../shared/confirmation-dialog.component';
import {MatDialog} from '@angular/material/dialog';

@Component({
  selector: 'app-comment-section',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule,
    DatePipe,
    TranslatePipe
  ],
  templateUrl: './comment-section.component.html',
  styleUrls: ['./comment-section.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CommentSectionComponent implements OnInit {
  @Input() post!: PostResponse;
  @Input() isShown: boolean = false;

  commentControl = new FormControl('', Validators.required);
  replyControlsMap = new Map<string, FormControl>();
  editingCommentMap = new Map<string, boolean>();
  editCommentControlsMap = new Map<string, FormControl>();

  currentUserEmail: string | null = null;

  constructor(
    private commentService: CommentService,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private i18n: I18nService,
    private dialog: MatDialog

  ) {}

  ngOnInit(): void {
    this.currentUserEmail = this.authService.getCurrentUserEmail();
  }

  getCommentUserName(comment: CommentResponse): string {
    return comment.userName || (comment.userEmail ? comment.userEmail.split('@')[0] : 'Anonymous');
  }

  isMyComment(comment: CommentResponse): boolean {
    return this.currentUserEmail !== null && comment.userEmail === this.currentUserEmail;
  }

  isReplyFormShown(commentId: string): boolean {
    return this.replyControlsMap.has(commentId);
  }

  getReplyFormControl(commentId: string): FormControl {
    return this.replyControlsMap.get(commentId)!;
  }

  toggleReplyForm(comment: CommentResponse): void {
    if (this.isReplyFormShown(comment.id)) {
      this.replyControlsMap.delete(comment.id);
    } else {
      const tagged = `@${this.getCommentUserName(comment)} `;
      this.replyControlsMap.set(comment.id, new FormControl(tagged, Validators.required));
    }
  }

  createComment(): void {
    if (this.commentControl.invalid) return;

    const content = this.commentControl.value!;

    const dialogData: ConfirmationDialogData = {
      title: this.i18n.instant('COMMENT_SECTION.CONFIRM_CREATE_TITLE'),
      message: this.i18n.instant('COMMENT_SECTION.CONFIRM_CREATE_MESSAGE', {
        content: content
      }),
      confirmText: this.i18n.instant('COMMENT_SECTION.CONFIRM_CREATE_CONFIRM'),
      cancelText: this.i18n.instant('COMMENT_SECTION.CONFIRM_CREATE_CANCEL')
    };

    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      width: '350px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const request: CommentRequest = {
          content,
          parentCommentId: undefined
        };

        this.commentService.createComment(this.post.id, request).subscribe({
          next: (newComment: CommentResponse) => {
            if (!this.post.comments) {
              this.post.comments = [];
            }
            this.post.comments.unshift(newComment);
            this.commentControl.reset();

            this.post.commentCount = (this.post.commentCount || 0) + 1;

            this.snackBar.open(
              this.i18n.instant('COMMENT_SECTION.SNACKBAR_CREATE_SUCCESS'),
              this.i18n.instant('COMMON.CLOSE'),
              {
                duration: 2000,
                panelClass: ['success-snackbar'],
              }
            );
          },
          error: () => {
            this.snackBar.open(
              this.i18n.instant('COMMENT_SECTION.SNACKBAR_CREATE_ERROR'),
              this.i18n.instant('COMMON.CLOSE'),
              {
                duration: 2000,
                panelClass: ['error-snackbar'],
              }
            );
          }
        });
      }
    });
  }

  createReply(post: PostResponse, parentComment: CommentResponse): void {
    const replyControl = this.getReplyFormControl(parentComment.id);
    if (!replyControl.valid) return;

    const content = replyControl.value!;
    const dialogData: ConfirmationDialogData = {
      title: this.i18n.instant('COMMENT_SECTION.CONFIRM_REPLY_TITLE'),
      message: this.i18n.instant('COMMENT_SECTION.CONFIRM_REPLY_MESSAGE', {
        user: this.getCommentUserName(parentComment),
        content: content
      }),
      confirmText: this.i18n.instant('COMMENT_SECTION.CONFIRM_REPLY_CONFIRM'),
      cancelText: this.i18n.instant('COMMENT_SECTION.CONFIRM_REPLY_CANCEL')
    };

    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      width: '350px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const request: CommentRequest = {
          content,
          parentCommentId: parentComment.id
        };

        this.commentService.createComment(post.id, request).subscribe({
          next: (newReply: CommentResponse) => {
            this.addReplyToComment(post.comments!, newReply);
            replyControl.reset();
            this.toggleReplyForm(parentComment);
            post.commentCount = (post.commentCount || 0) + 1;

            this.snackBar.open(
              this.i18n.instant('COMMENT_SECTION.SNACKBAR_REPLY_SUCCESS'),
              this.i18n.instant('COMMON.CLOSE'),
              { duration: 2000, panelClass: ['success-snackbar'] }
            );
          },
          error: () => {
            this.snackBar.open(
              this.i18n.instant('COMMENT_SECTION.SNACKBAR_REPLY_ERROR'),
              this.i18n.instant('COMMON.CLOSE'),
              { duration: 2000, panelClass: ['error-snackbar'] }
            );
          }
        });
      }
    });
  }

  private addReplyToComment(comments: CommentResponse[], reply: CommentResponse): boolean {
    for (let comment of comments) {
      if (comment.id === reply.parentCommentId) {
        comment.replies = [reply, ...(comment.replies || [])];
        return true;
      }
      if (comment.replies && this.addReplyToComment(comment.replies, reply)) {
        return true;
      }
    }
    return false;
  }

  startEditingComment(comment: CommentResponse): void {
    this.editingCommentMap.set(comment.id, true);
    this.editCommentControlsMap.set(comment.id, new FormControl(comment.content, Validators.required));
  }

  cancelEditingComment(commentId: string): void {
    this.editingCommentMap.set(commentId, false);
    this.editCommentControlsMap.delete(commentId);
  }

  isEditingComment(commentId: string): boolean {
    return this.editingCommentMap.get(commentId) || false;
  }

  getEditCommentFormControl(commentId: string): FormControl {
    return this.editCommentControlsMap.get(commentId)!;
  }

  saveEditedComment(comment: CommentResponse): void {
    const control = this.getEditCommentFormControl(comment.id);
    if (!control || control.invalid) return;

    const updatedContent = control.value!;

    const dialogData: ConfirmationDialogData = {
      title: this.i18n.instant('COMMENT_SECTION.CONFIRM_EDIT_TITLE'),
      message: this.i18n.instant('COMMENT_SECTION.CONFIRM_EDIT_MESSAGE', {
        content: updatedContent
      }),
      confirmText: this.i18n.instant('COMMENT_SECTION.CONFIRM_EDIT_CONFIRM'),
      cancelText: this.i18n.instant('COMMENT_SECTION.CONFIRM_EDIT_CANCEL')
    };

    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      width: '350px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const req: CommentRequest = { content: updatedContent };

        this.commentService.updateComment(comment.id, req).subscribe({
          next: (updated) => {
            this.updateCommentInList(this.post.comments ?? [], updated.id!, updated);
            this.cancelEditingComment(comment.id);

            this.snackBar.open(
              this.i18n.instant('COMMENT_SECTION.SNACKBAR_EDIT_SUCCESS'),
              this.i18n.instant('COMMON.CLOSE'),
              { duration: 2000, panelClass: ['success-snackbar'] }
            );
          },
          error: () => {
            this.snackBar.open(
              this.i18n.instant('COMMENT_SECTION.SNACKBAR_EDIT_ERROR'),
              this.i18n.instant('COMMON.CLOSE'),
              { duration: 2000, panelClass: ['error-snackbar'] }
            );
          }
        });
      }
    });
  }

  deleteComment(post: PostResponse, comment: CommentResponse): void {
    const dialogData: ConfirmationDialogData = {
      title: this.i18n.instant('COMMENT_SECTION.CONFIRM_DELETE_TITLE'),
      message: this.i18n.instant('COMMENT_SECTION.CONFIRM_DELETE_MESSAGE', {
        user: this.getCommentUserName(comment),
        content: comment.content,
      }),
      confirmText: this.i18n.instant('COMMENT_SECTION.CONFIRM_DELETE_CONFIRM'),
      cancelText: this.i18n.instant('COMMENT_SECTION.CONFIRM_DELETE_CANCEL'),
      confirmButtonColor: 'warn',
    };

    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      width: '350px',
      data: dialogData,
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.commentService.deleteComment(comment.id!).subscribe({
          next: () => {
            const wasTopLevel = this.removeCommentFromList(post.comments!, comment.id!);

            if (wasTopLevel && post.commentCount && post.commentCount > 0) {
              post.commentCount--;
            }

            this.snackBar.open(
              this.i18n.instant('COMMENT_SECTION.SNACKBAR_DELETE_SUCCESS'),
              this.i18n.instant('COMMON.CLOSE'),
              {
                duration: 2000,
                panelClass: ['success-snackbar'],
              }
            );
          },
          error: () => {
            this.snackBar.open(
              this.i18n.instant('COMMENT_SECTION.SNACKBAR_DELETE_ERROR'),
              this.i18n.instant('COMMON.CLOSE'),
              {
                duration: 2000,
                panelClass: ['error-snackbar'],
              }
            );
          }
        });
      }
    });
  }

  private updateCommentInList(comments: CommentResponse[], id: string, data: Partial<CommentResponse>): boolean {
    for (let i = 0; i < comments.length; i++) {
      if (comments[i].id === id) {
        comments[i] = { ...comments[i], ...data };
        return true;
      }
      if (comments[i].replies && this.updateCommentInList(comments[i].replies ?? [], id, data)) {
        return true;
      }
    }
    return false;
  }

  private removeCommentFromList(comments: CommentResponse[], id: string): boolean {
    for (let i = 0; i < comments.length; i++) {
      if (comments[i].id === id) {
        comments.splice(i, 1);
        return true;
      }
      if (this.removeCommentFromList(comments[i].replies ?? [], id)) {
        return true;
      }
    }
    return false;
  }


}
