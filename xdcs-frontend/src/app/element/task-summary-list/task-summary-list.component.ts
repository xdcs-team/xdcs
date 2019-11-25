import { Component, OnInit } from '@angular/core';
import { TasksService } from '../../../api/services/tasks.service';
import { TasksDto } from '../../../api/models';

@Component({
  selector: 'app-task-summary-list',
  templateUrl: './task-summary-list.component.html',
  styleUrls: ['./task-summary-list.component.less'],
})
export class TaskSummaryListComponent implements OnInit {
  tasks: TasksDto = null;

  constructor(private tasksService: TasksService) {

  }

  ngOnInit() {
    this.loadTasks();
  }

  private loadTasks() {
    this.tasksService.getTasks({})
      .subscribe(tasks => this.tasks = tasks);
  }
}
