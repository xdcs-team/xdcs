import { Component, OnInit } from '@angular/core';
import { NavbarItem } from '../../services/navbar.service';
import { TaskDefinitionDto } from '../../../api/models/task-definition-dto';
import { TaskDefinitionsService } from '../../../api/services/task-definitions.service';
import { NewTaskDefinitionComponent } from 'src/app/modal/new-task-definition/new-task-definition.component';

@Component({
  selector: 'app-task-definitions',
  templateUrl: './task-definitions.component.html',
  styleUrls: ['./task-definitions.component.less'],
})
@NavbarItem('Task Definitions')
export class TaskDefinitionsComponent implements OnInit {
  NewTaskDefinitionComponent = NewTaskDefinitionComponent;

  definitionList: Array<TaskDefinitionDto> = [];

  constructor(private taskDefinitionsService: TaskDefinitionsService) {

  }

  ngOnInit(): void {
    this.taskDefinitionsService.getTaskDefinitions({})
      .subscribe(definitions => this.definitionList = definitions.items);
  }
}
