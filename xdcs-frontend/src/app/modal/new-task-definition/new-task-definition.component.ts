import { Component, EventEmitter, Output, ViewChild } from '@angular/core';
import { TaskDefinitionsService } from '../../../api/services/task-definitions.service';
import { TaskDefinitionDto } from '../../../api/models/task-definition-dto';

@Component({
  selector: 'app-new-task-definition',
  templateUrl: './new-task-definition.component.html',
  styleUrls: ['./new-task-definition.component.less'],
})
export class NewTaskDefinitionComponent {
  private taskDefinition: TaskDefinitionDto = {};

  @ViewChild('modal', { static: false })
  private modal;

  @Output()
  created = new EventEmitter();

  constructor(private taskDefinitionsService: TaskDefinitionsService) {

  }

  isValid() {
    return !!this.taskDefinition.name;
  }

  submit() {
    this.taskDefinitionsService.createTaskDefinition({
      body: this.taskDefinition,
    }).subscribe(() => {
      this.modal.closeModal();
      this.created.emit();
    });
  }
}
