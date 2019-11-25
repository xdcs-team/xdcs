import { Component, Input, OnInit } from '@angular/core';
import { faCheck, faCircleNotch, faRedo, faTimes, faBox } from '@fortawesome/free-solid-svg-icons';
import { faClock as farClock, faStopCircle as farStopCircle } from '@fortawesome/free-regular-svg-icons';
import { TaskDto } from '../../../api/models/task-dto';

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
  faBox = faBox;
  farClock = farClock;
  farStopCircle = farStopCircle;

  TaskState = TaskState;

  @Input()
  task: TaskDto;

  constructor() {

  }

  ngOnInit() {

  }
}

export enum TaskState {
  QUEUED = 'queued',
  IN_PROGRESS = 'in_progress',
  FINISHED = 'finished',
  CANCELED = 'canceled',
  ERRORED = 'errored',
}
