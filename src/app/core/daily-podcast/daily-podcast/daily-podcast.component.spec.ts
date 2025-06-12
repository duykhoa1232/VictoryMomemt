import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DailyPodcastComponent } from './daily-podcast.component';

describe('DailyPodcastComponent', () => {
  let component: DailyPodcastComponent;
  let fixture: ComponentFixture<DailyPodcastComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DailyPodcastComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DailyPodcastComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
