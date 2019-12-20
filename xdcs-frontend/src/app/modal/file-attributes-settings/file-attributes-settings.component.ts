import { Component, Input, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { first } from 'rxjs/operators';
import { TaskDefinitionsService } from '../../../api/services/task-definitions.service';
import { FileDto } from '../../../api/models';

@Component({
  selector: 'app-file-attributes-settings',
  templateUrl: './file-attributes-settings.component.html',
  styleUrls: ['./file-attributes-settings.component.less'],
})
export class FileAttributesSettingsComponent {
  @ViewChild('modal', { static: false })
  private modal;
  fileInfo: FileDto = null;

  @Input()
  taskDefinitionId: string = null;

  @Input()
  path: string = null;

  validPattern = '[r-][w-][x-][r-][w-][x-][r-][w-][x-]';


  constructor(private taskDefinitionService: TaskDefinitionsService) {
  }

  ngOnInit() {
    this.fetchAccessRights();
  }

  private fetchAccessRights() {
    this.taskDefinitionService.getTaskDefinitionWorkspaceFile({
      taskDefinitionId: this.taskDefinitionId,
      path: this.path,
    }).pipe(first()).subscribe(fileInfo => this.fileInfo = fileInfo);
  }

  doCancel() {
    this.modal.closeModal();
  }

  isValid(): boolean {
    return new RegExp(this.validPattern).test(this.fileInfo.permissions);
  }

  doSubmit() {
    this.taskDefinitionService.setTaskDefinitionWorkspaceFile({
      taskDefinitionId: this.taskDefinitionId,
      path: this.path,
      body: this.fileInfo,
    }).subscribe(() => {
      this.modal.closeModal();
    });
  }
}
