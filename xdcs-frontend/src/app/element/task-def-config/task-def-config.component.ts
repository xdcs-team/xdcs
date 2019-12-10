import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { TaskDefinitionDto } from '../../../api/models/task-definition-dto';
import { KernelParamDto } from '../../../api/models/kernel-param-dto';

@Component({
  selector: 'app-task-def-config',
  templateUrl: './task-def-config.component.html',
  styleUrls: ['./task-def-config.component.less'],
})
export class TaskDefConfigComponent implements OnInit {
  emptyElement: KernelParamDto = {};
  types: Map<string, string> = new Map([
    ['opencl', 'OpenCL'],
    ['cuda', 'CUDA'],
    ['docker', 'Docker'],
    ['script', 'Script'],
  ]);

  paramTypes: Map<string, string> = new Map([
    ['simple', 'Simple'],
    ['pointer', 'Pointer'],
  ]);

  paramDirections: Map<string, string> = new Map([
    ['in', 'in'],
    ['out', 'out'],
    ['inout', 'in/out'],
  ]);

  @Input()
  taskDefinition: TaskDefinitionDto;

  @Output()
  save = new EventEmitter<TaskDefinitionDto>();

  constructor() {

  }

  ngOnInit() {
    if (!this.taskDefinition.config.kernelparams) {
      this.taskDefinition.config.kernelparams = [];
    }
    this.taskDefinition.config.allocatePseudoTty = false;
  }

  saveConfiguration() {
    this.save.emit(this.taskDefinition);
  }
}
