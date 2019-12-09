import { Component, OnInit } from '@angular/core';
import { TasksService } from '../../../api/services/tasks.service';
import { TaskDto } from '../../../api/models';
import { NavbarItem } from '../../services/navbar.service';
import { NewTaskComponent } from 'src/app/modal/new-task/new-task.component';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { first } from 'rxjs/operators';
import { forkJoin } from 'rxjs';

const taskIdParam = 'taskId';

@Component({
  selector: 'app-tasks',
  templateUrl: './tasks.component.html',
  styleUrls: ['./tasks.component.less'],
})
@NavbarItem('Tasks')
export class TasksComponent implements OnInit {

  tasks: Array<TaskDto> = null;
  NewTaskComponent = NewTaskComponent;
  selected: TaskDto;

  constructor(private tasksService: TasksService,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit(): void {
    this.fetchTaskResults();
    this.route.params.subscribe(params => {
      this.refreshSelection(params);
    });
  }

  onSelectionChange(selected: TaskDto) {
    this.router.navigate(['tasks', selected.id], {
      replaceUrl: true,
    });
  }

  private fetchTaskResults(): void {
    const parameters = this.route.params.pipe(first());
    const tasksDto = this.tasksService.getTasks({}).pipe(first());

    forkJoin([parameters, tasksDto])
      .subscribe(([params, tasks]) => {
        this.tasks = tasks.items;
        this.refreshSelection(params);
      });
  }

  private refreshSelection(params: Params) {
    if (!this.tasks) {
      return;
    }

    const found = this.tasks
      .find(def => def.id === params[taskIdParam]);
    if (found) {
      this.selected = found;
    }
  }
}
