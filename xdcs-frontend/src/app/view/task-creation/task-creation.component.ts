import { Component } from '@angular/core';
import { TaskDefinitionsService } from '../../../api/services/task-definitions.service';
import { ActivatedRoute, Router } from '@angular/router';
import { first } from 'rxjs/operators';
import { NodesDto } from '../../../api/models/nodes-dto';
import { NodesService } from '../../../api/services/nodes.service';
import {
  DeploymentDto,
  EnvironmentVariableDto,
  NodeDto,
  ResourceDto,
  TaskCreationDto,
  TaskDefinitionDto,
} from '../../../api/models';
import { DeploymentsService } from '../../../api/services/deployments.service';
import { TasksService } from '../../../api/services/tasks.service';
import { BlobUtils } from '../../utils/blob-utils';

interface KernelArgument {
  file?: File;
  value?: string;
  size?: number;
  encodedValue: string;
}

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
  nodeIds: string[];

  kernelArguments: KernelArgument[] = [];
  globalWorkShape: number[] = [1, 1, 1];
  customLocalWorkShape = false;
  localWorkShape: number[] = [1, 1, 1];
  mergingAgent: string;

  reservedParamNames: string[] = ['XDCS_AGENT_ID', 'XDCS_AGENT_COUNT'];

  taskName = '';
  emptyResource: ResourceDto = {
    agent: '',
    key: '',
  };
  selectedResources: ResourceDto[] = [];

  environmentVariableSchema: EnvironmentVariableDto = {
    name: '',
    value: '',
  };
  addedEnvironmentVariables: EnvironmentVariableDto[] = [];

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
      this.initializeKernelArguments();

      this.taskDefinitionsService.getTaskDefinition({
        taskDefinitionId: dp.taskDefinitionId,
      }).pipe(first()).subscribe(definition => this.definition = definition);
    });

    this.nodesService.getNodes()
      .pipe(first())
      .subscribe((nodes: NodesDto) => {
        this.nodes = nodes.items;
        this.nodeIds = nodes.items.map(node => node.id);
      });
  }

  initializeKernelArguments() {
    if (this.deployment.config.kernelparams) {
      Array.from(this.deployment.config.kernelparams).forEach((param) => {
        if (param.type === 'pointer' && param.direction !== 'out') {
          this.kernelArguments.push({ file: null, encodedValue: null });
        } else if (param.type === 'pointer' && param.direction === 'out') {
          this.kernelArguments.push({ size: 0, encodedValue: null });
        } else {
          this.kernelArguments.push({ value: '', encodedValue: null });
        }
      });
    }
  }

  resourceKeysByNodeId(nodeId: string): string[] {
    const node = this.nodes.find(n => n.id === nodeId);
    if (node) {
      return node.resources.map(res => res.key);
    } else {
      return [];
    }
  }

  submitTask() {
    this.convertArguments()
      .then(() => this.startTask());
  }

  convertArguments() {
    return Promise.all(
      this.kernelArguments.map(argument => this.convertArgument(argument))
    );
  }

  convertArgument(argument: KernelArgument) {
    if (argument.file !== undefined) {
      return BlobUtils.toBinaryString(argument.file)
        .then(convertedFile => {
          argument.encodedValue = btoa(convertedFile);
        });
    } else if (argument.size !== undefined) {
      argument.encodedValue = btoa(unescape(encodeURIComponent(argument.size.toString())));
    } else if (argument.value !== undefined) {
      argument.encodedValue = btoa(unescape(encodeURIComponent(argument.value)));
    }
    return Promise.resolve();
  }

  startTask() {
    this.tasksService.startTask({
      body: {
        name: this.taskName,
        deploymentId: this.deploymentId,
        resources: this.selectedResources,
        environmentVariables: this.addedEnvironmentVariables,
        kernelArguments: this.prepareKernelArguments(),
        globalWorkShape: this.prepareGlobalWorkShape(),
        localWorkShape: this.prepareLocalWorkShape(),
        mergingAgent: this.mergingAgent,
      } as TaskCreationDto,
    }).subscribe(() => {
      this.router.navigateByUrl('/');
    });
  }

  prepareKernelArguments(): string[] {
    if (this.isOpenclOrCuda()) {
      return this.kernelArguments.map(arg => arg.encodedValue);
    }
    return null;
  }

  prepareGlobalWorkShape(): number[] {
    if (this.isOpenclOrCuda()) {
      return this.globalWorkShape;
    }
    return null;
  }

  prepareLocalWorkShape(): number[] {
    if (this.isOpenclOrCuda() && this.customLocalWorkShape) {
      return this.localWorkShape;
    }
    return null;
  }

  isOpenclOrCuda(): boolean {
    return ['opencl', 'cuda'].includes(this.deployment.config.type);
  }
}
