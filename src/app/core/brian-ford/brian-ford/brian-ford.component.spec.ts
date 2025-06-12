import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BrianFordComponent } from './brian-ford.component';

describe('BrianFordComponent', () => {
  let component: BrianFordComponent;
  let fixture: ComponentFixture<BrianFordComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BrianFordComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BrianFordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
