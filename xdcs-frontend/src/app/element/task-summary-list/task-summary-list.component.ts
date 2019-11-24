import { Component, OnInit } from '@angular/core';
import { TaskExecutionStatus } from '../task-summary-item/task-summary-item.component';

@Component({
  selector: 'app-task-summary-list',
  templateUrl: './task-summary-list.component.html',
  styleUrls: ['./task-summary-list.component.less'],
})
export class TaskSummaryListComponent implements OnInit {
  TaskExecutionStatus = TaskExecutionStatus;

  constructor() {

  }

  ngOnInit() {

  }
}
