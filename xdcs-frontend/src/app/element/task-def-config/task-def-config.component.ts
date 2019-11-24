import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { faChevronDown, faChevronUp, faPlus, faTrash } from '@fortawesome/free-solid-svg-icons';
import { TaskDefinitionDto } from '../../../api/models/task-definition-dto';
import { KernelParamDto } from '../../../api/models/kernel-param-dto';

@Component({
  selector: 'app-task-def-config',
  templateUrl: './task-def-config.component.html',
  styleUrls: ['./task-def-config.component.less'],
})
export class TaskDefConfigComponent implements OnInit {
  faChevronUp = faChevronUp;
  faChevronDown = faChevronDown;
  faTrash = faTrash;
  faPlus = faPlus;

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
  submit = new EventEmitter<TaskDefinitionDto>();

  constructor() {

  }

  ngOnInit() {

  }

  addKernelParam() {
    if (!this.taskDefinition.config.kernelparams) {
      this.taskDefinition.config.kernelparams = [];
    }

    const param: KernelParamDto = {};
    this.taskDefinition.config.kernelparams.push(param);
  }

  removeKernelParam(param: KernelParamDto) {
    const params = this.taskDefinition.config.kernelparams;
    this.taskDefinition.config.kernelparams = params.filter(item => item !== param);
  }

  moveKernelParamUp(param: KernelParamDto) {
    const index = this.taskDefinition.config.kernelparams.indexOf(param);
    if (index > 0 && index < this.taskDefinition.config.kernelparams.length) {
      this.swapKernelParams(index - 1, index);
    }
  }

  moveKernelParamDown(param: KernelParamDto) {
    const index = this.taskDefinition.config.kernelparams.indexOf(param);
    if (index >= 0 && index < this.taskDefinition.config.kernelparams.length - 1) {
      this.swapKernelParams(index + 1, index);
    }
  }

  private swapKernelParams(a, b) {
    const params = this.taskDefinition.config.kernelparams;
    const tmp = params[a];
    params[a] = params[b];
    params[b] = tmp;
  }

  isValid() {
    if (this.taskDefinition.config.type === null) {
      return false;
    }

    return true;
  }

  saveConfiguration() {
    this.submit.emit(this.taskDefinition);
  }
}
