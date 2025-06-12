import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChangeYourLifeComponent } from './change-your-life.component';

describe('ChangeYourLifeComponent', () => {
  let component: ChangeYourLifeComponent;
  let fixture: ComponentFixture<ChangeYourLifeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChangeYourLifeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ChangeYourLifeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
