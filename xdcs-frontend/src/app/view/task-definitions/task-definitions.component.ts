import { Component, OnInit } from '@angular/core';
import { NavbarItem } from '../../services/navbar.service';
import { TaskDefinitionDto } from '../../../api/models/task-definition-dto';
import { TaskDefinitionsService } from '../../../api/services/task-definitions.service';
import { NewTaskDefinitionComponent } from 'src/app/modal/new-task-definition/new-task-definition.component';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { first } from 'rxjs/operators';

const definitionIdParam = 'definitionId';

@Component({
  selector: 'app-task-definitions',
  templateUrl: './task-definitions.component.html',
  styleUrls: ['./task-definitions.component.less'],
  host: { class: 'd-flex flex-column' },
})
@NavbarItem('Task Definitions')
export class TaskDefinitionsComponent implements OnInit {
  NewTaskDefinitionComponent = NewTaskDefinitionComponent;

  definitions: Array<TaskDefinitionDto> = null;
  selected: TaskDefinitionDto;

  constructor(private taskDefinitionsService: TaskDefinitionsService,
              private route: ActivatedRoute,
              private router: Router) {

  }

  ngOnInit(): void {
    this.fetchTaskDefinitions();
    this.route.params.subscribe(params => {
      this.refreshSelection(params);
    });
  }

  onSelectionChange(selected: TaskDefinitionDto) {
    this.router.navigate(['task-definitions', selected.id], {
      replaceUrl: true,
    });
  }

  private fetchTaskDefinitions(): void {
    const parameters = this.route.params.pipe(first());
    const taskDefinitions = this.taskDefinitionsService.getTaskDefinitions({}).pipe(first());

    forkJoin([parameters, taskDefinitions])
      .subscribe(([params, definitions]) => {
        this.definitions = definitions.items;
        this.refreshSelection(params);
      });
  }

  private refreshSelection(params: Params) {
    if (!this.definitions) {
      return;
    }

    const found = this.definitions
      .find(def => def.id === params[definitionIdParam]);
    if (found) {
      this.selected = found;
    }
  }

  onModalHidden(): void {
    this.fetchTaskDefinitions();
  }
}
