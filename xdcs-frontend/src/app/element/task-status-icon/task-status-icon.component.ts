import { Component, Input } from '@angular/core';
import { faCheck, faCircleNotch, faTimes } from '@fortawesome/free-solid-svg-icons';
import { faClock as farClock, faStopCircle as farStopCircle } from '@fortawesome/free-regular-svg-icons';
import { TaskDto } from '../../../api/models/task-dto';
import { TaskState } from '../task-summary-item/task-summary-item.component';

@Component({
  selector: 'app-task-status-icon',
  templateUrl: './task-status-icon.component.html',
  styleUrls: ['./task-status-icon.component.less'],
})
export class TaskStatusIconComponent {
  faCircleNotch = faCircleNotch;
  faCheck = faCheck;
  faTimes = faTimes;
  farClock = farClock;
  farStopCircle = farStopCircle;

  @Input()
  task: TaskDto;
  @Input()
  size = 'md';
  TaskState = TaskState;

  constructor() {
  }
}
