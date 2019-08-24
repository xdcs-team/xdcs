import { Component, OnInit } from '@angular/core';
import { faChevronDown, faChevronUp, faPlus, faTrash } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-task-def-config',
  templateUrl: './task-def-config.component.html',
  styleUrls: ['./task-def-config.component.less']
})
export class TaskDefConfigComponent implements OnInit {
  private TaskDefinitionType = TaskDefinitionType;

  private faChevronUp = faChevronUp;
  private faChevronDown = faChevronDown;
  private faTrash = faTrash;
  private faPlus = faPlus;

  private types: Map<number, string> = new Map([
    [TaskDefinitionType.OpenCL, 'OpenCL'],
    [TaskDefinitionType.CUDA, 'CUDA'],
    [TaskDefinitionType.Docker, 'Docker'],
    [TaskDefinitionType.Script, 'Script'],
  ]);

  private paramTypes: Map<number, string> = new Map([
    [KernelParamType.Simple, 'Simple'],
    [KernelParamType.Pointer, 'Pointer'],
  ]);

  private paramDirections: Map<number, string> = new Map([
    [KernelParamDirection.In, 'in'],
    [KernelParamDirection.Out, 'out'],
    [KernelParamDirection.InOut, 'in/out'],
  ]);

  // Common configuration
  private name: string = null;
  private type: number = null;

  // OpenCL / CUDA config
  private kernel = {
    file: '',
    name: '',
    params: [],
  };

  // Docker config
  private docker = {
    dockerfile: '',
  };

  // Script config
  private script = {
    path: '',
  };

  private lastId = 0;

  constructor() {

  }

  ngOnInit() {

  }

  addKernelParam() {
    const param = new KernelParam();
    param.id = this.lastId++;
    this.kernel.params.push(param);
  }

  removeKernelParam(param: KernelParam) {
    this.kernel.params = this.kernel.params.filter(item => item !== param);
  }

  moveKernelParamUp(param: KernelParam) {
    const index = this.kernel.params.indexOf(param);
    if (index > 0 && index < this.kernel.params.length) {
      this.swapKernelParams(index - 1, index);
    }
  }

  moveKernelParamDown(param: KernelParam) {
    const index = this.kernel.params.indexOf(param);
    if (index >= 0 && index < this.kernel.params.length - 1) {
      this.swapKernelParams(index + 1, index);
    }
  }

  private swapKernelParams(a, b) {
    const tmp = this.kernel.params[a];
    this.kernel.params[a] = this.kernel.params[b];
    this.kernel.params[b] = tmp;
  }

  isValid() {
    if (this.type === null) {
      return false;
    }

    return true;
  }

  saveConfiguration() {

  }
}

enum TaskDefinitionType {
  OpenCL,
  CUDA,
  Docker,
  Script,
}

enum KernelParamDirection {
  In,
  Out,
  InOut,
}

enum KernelParamType {
  Simple,
  Pointer,
}

class KernelParam {
  id: number = null;
  name: string = null;
  type: number = null;
  direction: number = null;
}
