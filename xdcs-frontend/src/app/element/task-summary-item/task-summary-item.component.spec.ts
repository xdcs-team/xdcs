import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TaskSummaryItemComponent } from './task-summary-item.component';

describe('TaskSummaryItemComponent', () => {
  let component: TaskSummaryItemComponent;
  let fixture: ComponentFixture<TaskSummaryItemComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TaskSummaryItemComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskSummaryItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
