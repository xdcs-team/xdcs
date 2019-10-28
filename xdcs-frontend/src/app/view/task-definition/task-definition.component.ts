import { Component, OnInit } from '@angular/core';
import { NavbarItem } from '../../services/navbar.service';
import { TaskDefinitionDto } from '../../../api/models/task-definition-dto';
import { TaskDefinitionsService } from '../../../api/services/task-definitions.service';
import { first, flatMap, map } from 'rxjs/operators';
import { ActivatedRoute } from '@angular/router';
import { TaskDefinitionConfigDto } from '../../../api/models/task-definition-config-dto';
import { Alert, GlobalAlertsService } from '../../services/global-alerts.service';
import { TreeDirectory, TreeFileType } from '../../element/file-tree/file-tree.component';
import { from, Observable, of } from 'rxjs';
import { ModalService } from '../../services/modal.service';

@Component({
  selector: 'app-task-definition',
  templateUrl: './task-definition.component.html',
  styleUrls: ['./task-definition.component.less'],
  host: { class: 'd-flex flex-column' },
})
@NavbarItem('Task Definition')
export class TaskDefinitionComponent implements OnInit {
  private readonly taskDefinitionId: string;
  private taskDefinition: TaskDefinitionDto = null;

  private editedFile: EditedFile = null;

  private readonly loadHandler = path => this.loadFile(path);
  private readonly moveHandler = (fromPath, toPath) => this.moveFile(fromPath, toPath);
  private readonly deleteHandler = path => this.deleteFile(path);
  private readonly openHandler = path => this.openFile(path);

  constructor(private taskDefinitionsService: TaskDefinitionsService,
              private route: ActivatedRoute,
              private globalAlerts: GlobalAlertsService,
              private modalService: ModalService) {
    this.taskDefinitionId = this.route.snapshot.params.id;
  }

  ngOnInit(): void {
    this.taskDefinitionsService.getTaskDefinition({
      taskDefinitionId: this.taskDefinitionId,
    }).pipe(first()).subscribe(def => {
      this.taskDefinition = def;
    });
  }

  private saveTaskDefinitionConfig(config: TaskDefinitionConfigDto) {
    this.taskDefinitionsService.setTaskDefinitionConfiguration({
      taskDefinitionId: this.taskDefinitionId,
      body: config,
    }).pipe(first()).subscribe(_ => {
      this.globalAlerts.addAlert(new Alert('success', 'Task definition saved'));
    });
  }

  private loadFile(path: string): Promise<TreeDirectory> {
    function mapType(type: 'regular' | 'directory' | 'link'): TreeFileType {
      switch (type) {
        case 'regular':
        default:
          return TreeFileType.FILE_OTHER;
        case 'directory':
          return TreeFileType.DIRECTORY;
        case 'link':
          return TreeFileType.FILE_SYMLINK;
      }
    }

    return this.taskDefinitionsService.getTaskDefinitionWorkspaceFile({
      taskDefinitionId: this.taskDefinitionId, path,
    }).pipe(map(fileDto => ({
      entries: fileDto.children.map(child => ({
        name: child.name,
        type: mapType(child.type),
      })),
    }))).toPromise();
  }

  private moveFile(pathFrom: string, pathTo: string): Promise<void> {
    return Promise.reject();
  }

  private deleteFile(path: string): Promise<void> {
    return this.taskDefinitionsService.deleteTaskDefinitionWorkspaceFile({
      taskDefinitionId: this.taskDefinitionId, path,
    }).toPromise();
  }

  private openFile(path: string): void {
    this.taskDefinitionsService.getTaskDefinitionWorkspaceFileContent({
      taskDefinitionId: this.taskDefinitionId, path,
    }).pipe(
      first(),
      flatMap(blob => new Observable<string>(observer => {
        // convert blob to string
        const reader = new FileReader();
        reader.onload = () => {
          observer.next(reader.result as string);
        };
        reader.readAsText(blob, 'utf-8');
      }))
    ).pipe(flatMap(content => {
      if (this.editedFile) {
        return from(this.modalService.confirmation({
          text: 'Another file is opened, any unsaved changes will be lost',
          type: 'warning',
          confirmText: 'Open anyways',
        }))
          .pipe(map(callback => {
            callback();
            return content;
          }));
      } else {
        return of(content);
      }
    })).subscribe(content => {
      this.editedFile = {
        path,
        type: 'text',
        text: content,
      };
    });
  }
}

interface EditedFile {
  path: string;
  type: 'text';
  text?: string;
}
