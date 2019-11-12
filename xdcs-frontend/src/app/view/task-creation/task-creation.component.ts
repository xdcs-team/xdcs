import { Component } from '@angular/core';
import { TaskDefinitionsService } from '../../../api/services/task-definitions.service';
import { ActivatedRoute } from '@angular/router';
import { first } from 'rxjs/operators';
import { NodesDto } from '../../../api/models/nodes-dto';
import { NodesService } from '../../../api/services/nodes.service';
import { DeploymentDto, NodeDto } from '../../../api/models';
import { DeploymentsService } from '../../../api/services/deployments.service';

@Component({
  selector: 'app-task-creation',
  templateUrl: './task-creation.component.html',
  styleUrls: ['./task-creation.component.less'],
})
export class TaskCreationComponent {
  nodes: Array<NodeDto> = [];
  emptyNode: NodeDto = {};
  deploymentId: string;
  deployment: DeploymentDto;
  usedNodes: Array<NodeDto> = [];
  files: Map<string, File> = new Map([]);

  constructor(private taskDefinitionsService: TaskDefinitionsService,
              private route: ActivatedRoute,
              private nodesService: NodesService,
              private deploymentService: DeploymentsService) {

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
}
