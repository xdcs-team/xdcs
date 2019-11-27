import { Component } from '@angular/core';
import { TaskDefinitionsService } from '../../../api/services/task-definitions.service';
import { ActivatedRoute, Router } from '@angular/router';
import { first } from 'rxjs/operators';
import { NodesDto } from '../../../api/models/nodes-dto';
import { NodesService } from '../../../api/services/nodes.service';
import { DeploymentDto, NodeDto, ResourceDto, TaskCreationDto, TaskDefinitionDto } from '../../../api/models';
import { DeploymentsService } from '../../../api/services/deployments.service';
import { TasksService } from '../../../api/services/tasks.service';

@Component({
  selector: 'app-task-creation',
  templateUrl: './task-creation.component.html',
  styleUrls: ['./task-creation.component.less'],
})
export class TaskCreationComponent {
  deploymentId: string;
  deployment: DeploymentDto;
  definition: TaskDefinitionDto;
  nodes: Array<NodeDto>;
  files: Map<string, File> = new Map([]);

  taskName = '';
  resources: Array<ResourceDto> = [];
  emptyResource: ResourceDto = {
    agent: '',
    key: '',
    quantity: 1,
    type: 'cpu',
  };

  constructor(private taskDefinitionsService: TaskDefinitionsService,
              private route: ActivatedRoute,
              private router: Router,
              private nodesService: NodesService,
              private deploymentService: DeploymentsService,
              private tasksService: TasksService) {

  }

  ngOnInit(): void {
    this.route.params.subscribe(routeParams => {
      this.loadTask(routeParams.deploymentId);
    });
  }

  loadTask(deploymentId): void {
    this.deploymentId = deploymentId;

    this.deploymentService.getDeployment({
      deploymentId: this.deploymentId,
    }).pipe(first()).subscribe(dp => {
      this.deployment = dp;
      this.loadFilesMap();

      this.taskDefinitionsService.getTaskDefinition({
        taskDefinitionId: dp.taskDefinitionId,
      }).pipe(first()).subscribe(definition => this.definition = definition);
    });

    this.nodesService.getNodes()
      .pipe(first())
      .subscribe((nodes: NodesDto) => {
        this.nodes = nodes.items;
      });
  }

  loadFilesMap(): void {
    if (this.deployment.config.kernelparams) {
      this.deployment.config.kernelparams.forEach(param => {
        if (param.type === 'pointer' && param.direction !== 'out') {
          this.files.set(param.name, null);
        }
      });
    }
  }

  submitTask() {
    this.tasksService.startTask({
      body: {
        name: this.taskName,
        deploymentId: this.deploymentId,
        resources: this.resources,
      } as TaskCreationDto,
    }).subscribe(() => {
      this.router.navigateByUrl('/');
    });
  }
}
