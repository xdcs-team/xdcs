import { Component, Input, OnInit } from '@angular/core';
import { faCheck, faCircleNotch, faTimes, faRedo } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-task-summary-item',
  templateUrl: './task-summary-item.component.html',
  styleUrls: ['./task-summary-item.component.less'],
})
export class TaskSummaryItemComponent implements OnInit {
  isCollapsed = true;
  faCircleNotch = faCircleNotch;
  faCheck = faCheck;
  faTimes = faTimes;
  faRedo = faRedo;

  TaskExecutionStatus = TaskExecutionStatus;

  @Input()
  status: TaskExecutionStatus = TaskExecutionStatus.InProgress;

  @Input()
  taskTitle = '???';

  constructor() {
  }

  ngOnInit() {
  }
}

export enum TaskExecutionStatus {
  InProgress,
  Finished,
  Errored,
}
