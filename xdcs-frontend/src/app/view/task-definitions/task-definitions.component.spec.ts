import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TaskDefinitionsComponent } from './task-definitions.component';

describe('TaskDefinitionsComponent', () => {
  let component: TaskDefinitionsComponent;
  let fixture: ComponentFixture<TaskDefinitionsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TaskDefinitionsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskDefinitionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
