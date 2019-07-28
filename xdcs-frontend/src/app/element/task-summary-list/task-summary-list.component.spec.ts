import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TaskSummaryListComponent } from './task-summary-list.component';

describe('TaskSummaryListComponent', () => {
  let component: TaskSummaryListComponent;
  let fixture: ComponentFixture<TaskSummaryListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TaskSummaryListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskSummaryListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
