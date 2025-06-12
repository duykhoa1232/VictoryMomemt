import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExpertInterviewsComponent } from './expert-interviews.component';

describe('ExpertInterviewsComponent', () => {
  let component: ExpertInterviewsComponent;
  let fixture: ComponentFixture<ExpertInterviewsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExpertInterviewsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ExpertInterviewsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
