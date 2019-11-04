import { Component, Input, OnInit } from '@angular/core';
import { TaskDefinitionDto } from '../../../api/models/task-definition-dto';
import { DeployDefinitionComponent } from '../../modal/deploy-definition/deploy-definition.component';
import { ConfirmationComponent } from '../../modal/confirmation/confirmation.component';
import { ModalService } from '../../services/modal.service';

@Component({
  selector: 'app-task-definition-preview',
  templateUrl: './task-definition-preview.component.html',
  styleUrls: ['./task-definition-preview.component.less'],
})
export class TaskDefinitionPreviewComponent implements OnInit {
  @Input()
  definition: TaskDefinitionDto;

  constructor(private modalService: ModalService) {

  }

  ngOnInit() {

  }

  private showDeployModal() {
    const modal = this.modalService.show(DeployDefinitionComponent, true, {
      taskDefinitionId: this.definition.id,
    });
  }
}
