import { Component, Input, OnInit } from '@angular/core';
import { faCheck, faCircleNotch, faTimes, faRedo } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-task-summary-item',
  templateUrl: './task-summary-item.component.html',
  styleUrls: ['./task-summary-item.component.less']
})
export class TaskSummaryItemComponent implements OnInit {
  private isCollapsed = true;
  private faCircleNotch = faCircleNotch;
  private faCheck = faCheck;
  private faTimes = faTimes;
  private faRedo = faRedo;

  private TaskExecutionStatus = TaskExecutionStatus;

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
