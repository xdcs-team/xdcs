import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { TaskDefinitionDto } from '../../../api/models/task-definition-dto';
import { DeployDefinitionComponent } from '../../modal/deploy-definition/deploy-definition.component';
import { ModalService } from '../../services/modal.service';
import { DeploymentDescriptorsDto } from '../../../api/models/deployment-descriptors-dto';
import { TaskDefinitionsService } from '../../../api/services/task-definitions.service';

@Component({
  selector: 'app-task-definition-preview',
  templateUrl: './task-definition-preview.component.html',
  styleUrls: ['./task-definition-preview.component.less'],
})
export class TaskDefinitionPreviewComponent implements OnInit, OnChanges {
  deploymentDescriptors: DeploymentDescriptorsDto;

  @Input()
  definition: TaskDefinitionDto;

  constructor(private modalService: ModalService,
              private taskDefinitionsService: TaskDefinitionsService) {

  }

  ngOnInit() {
    this.fetchDeploymentDescriptors();
  }

  ngOnChanges(changes: SimpleChanges) {
    this.fetchDeploymentDescriptors();
  }

  private fetchDeploymentDescriptors() {
    this.taskDefinitionsService.getTaskDefinitionDeployments({
      taskDefinitionId: this.definition.id,
    }).subscribe(deployments => this.deploymentDescriptors = deployments);
  }

  showDeployModal() {
    const modal = this.modalService.show(DeployDefinitionComponent, true, {
      taskDefinitionId: this.definition.id,
    });
  }
}
