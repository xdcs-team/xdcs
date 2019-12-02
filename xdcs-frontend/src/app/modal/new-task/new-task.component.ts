import { Component, ViewChild } from '@angular/core';
import { TaskDefinitionsService } from '../../../api/services/task-definitions.service';
import { TaskDefinitionDto } from '../../../api/models/task-definition-dto';
import { Router } from '@angular/router';
import { DeploymentDescriptorDto } from '../../../api/models/deployment-descriptor-dto';
import { DeploymentsService } from '../../../api/services/deployments.service';

@Component({
  selector: 'app-new-task-definition',
  templateUrl: './new-task.component.html',
  styleUrls: ['./new-task.component.less'],
})
export class NewTaskComponent {
  @ViewChild('modal', { static: false })
  modal;
  definitions: Array<TaskDefinitionDto> = null;
  definitionId: string = null;
  deployments: Array<DeploymentDescriptorDto> = null;
  deploymentId: string = null;

  constructor(private taskDefinitionsService: TaskDefinitionsService,
              private deploymentsService: DeploymentsService,
              private router: Router) {

  }

  ngOnInit(): void {
    this.taskDefinitionsService.getTaskDefinitions({})
      .subscribe(definitions => this.definitions = definitions.items);
  }

  isValid() {
    return this.deploymentId !== null;
  }

  submit() {
    this.modal.closeModal();
    this.router.navigateByUrl('/new-task/' + this.deploymentId);
  }

  onDefinitionChange() {
    this.taskDefinitionsService.getTaskDefinitionDeployments({
      taskDefinitionId: this.definitionId,
    }).subscribe(deployments => this.deployments = deployments.items);
  }
}
