import { Component, Input } from '@angular/core';
import { TaskDefinitionDto } from '../../../api/models/task-definition-dto';

@Component({
  selector: 'app-task-definition-details',
  templateUrl: './task-definition-details.component.html',
  styleUrls: ['./task-definition-details.component.less'],
})
export class TaskDefinitionDetailsComponent {

  @Input()
  definition: TaskDefinitionDto;

  constructor() {

  }
}
