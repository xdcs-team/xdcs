import { Component, OnInit } from '@angular/core';
import { TasksService } from '../../../api/services/tasks.service';
import { TaskDto } from '../../../api/models';
import { NavbarItem } from '../../services/navbar.service';
import { NewTaskComponent } from 'src/app/modal/new-task/new-task.component';

@Component({
  selector: 'app-tasks',
  templateUrl: './tasks.component.html',
  styleUrls: ['./tasks.component.less'],
})
@NavbarItem('Tasks')
export class TasksComponent implements OnInit {

  tasks: Array<TaskDto> = [];
  NewTaskComponent = NewTaskComponent;


  constructor(private tasksService: TasksService) {
  }

  ngOnInit(): void {
    this.fetchTaskResults();
  }

  private fetchTaskResults(): void {
    this.tasksService.getTasks({})
      .subscribe(tasks => this.tasks = tasks.items);
  }
}
