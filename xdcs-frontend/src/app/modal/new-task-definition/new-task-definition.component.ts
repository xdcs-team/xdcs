import { Component, EventEmitter, Output, ViewChild } from '@angular/core';
import { TaskDefinitionsService } from '../../../api/services/task-definitions.service';
import { TaskDefinitionDto } from '../../../api/models/task-definition-dto';

@Component({
  selector: 'app-new-task-definition',
  templateUrl: './new-task-definition.component.html',
  styleUrls: ['./new-task-definition.component.less']
})
export class NewTaskDefinitionComponent {
  private taskDefinition: TaskDefinitionDto = {};

  @Output()
  created = new EventEmitter();

  @ViewChild('modal', { static: false })
  modal;

  constructor(private taskDefinitionsService: TaskDefinitionsService) {

  }

  isValid() {
    if (!this.taskDefinition.name) {
      return false;
    }

    return true;
  }

  submit() {
    this.taskDefinitionsService.createTaskDefinition({
      body: this.taskDefinition
    }).subscribe(() => {
      this.modal.closeModal();
      this.created.emit();
    });
  }
}
