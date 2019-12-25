import { Component, OnInit } from '@angular/core';
import { TasksService } from '../../../api/services/tasks.service';
import { TaskDto } from '../../../api/models';
import { NavbarItem } from '../../services/navbar.service';
import { NewTaskComponent } from 'src/app/modal/new-task/new-task.component';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { first } from 'rxjs/operators';
import { forkJoin, Observable } from 'rxjs';
import { FetchCallback } from '../../element/fullscreen-list/fullscreen-list.component';

const taskIdParam = 'taskId';

@Component({
  selector: 'app-tasks',
  templateUrl: './tasks.component.html',
  styleUrls: ['./tasks.component.less'],
})
@NavbarItem('Tasks')
export class TasksComponent implements OnInit {
  private static PAGE_SIZE = 25;

  NewTaskComponent = NewTaskComponent;
  tasks: Array<TaskDto> = null;
  selected: TaskDto;
  linkedTask: TaskDto;

  nextPage = 0;
  fetching = false;
  fetchedAll = false;

  constructor(private tasksService: TasksService,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit(): void {
    this.fetchNextPage();
    this.route.params.subscribe(params => {
      this.refreshSelection(params);
    });
  }

  onSelectionChange(selected: TaskDto) {
    this.router.navigate(['tasks', selected.id], {
      replaceUrl: true,
    });
  }

  fetchNextPage(callback: FetchCallback = null) {
    if (this.fetching || this.fetchedAll) {
      return;
    }

    this.fetching = true;
    const from = this.nextPage * TasksComponent.PAGE_SIZE;
    const maxResults = TasksComponent.PAGE_SIZE;

    const parameters = this.route.params.pipe(first());
    const tasksDto = this.tasksService.getTasks({
      from,
      maxResults,
    }).pipe(first());

    forkJoin([parameters, tasksDto])
      .subscribe(([params, tasks]) => {
        this.fetching = false;

        if (tasks.items.length === 0) {
          this.fetchedAll = true;
          this.tasks = this.tasks || [];
        } else {
          if (!this.tasks) {
            this.tasks = tasks.items;
          } else {
            this.tasks.push(...tasks.items);
          }

          this.refreshSelection(params);
          this.nextPage++;
        }

        if (callback) {
          callback.fetched();
        }
      });
  }

  private refreshSelection(params: Params) {
    if (!this.tasks) {
      return;
    }

    const found = this.tasks
      .find(def => def.id === params[taskIdParam]);
    if (found) {
      this.findLinkedTaskForTask(found).subscribe(linkedTask => {
        this.linkedTask = linkedTask;
        this.selected = found;
      });
    }
  }

  private findLinkedTaskForTask(task: TaskDto): Observable<TaskDto> {
    if (task.originTaskId) {
      return this.tasksService.getTask({ taskId: task.originTaskId });
    } else {
      return this.tasksService.getMergingTaskForTask({ taskId: task.id });
    }
  }
}
