import { Component, OnInit, ViewChild } from '@angular/core';
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
import { CodeEditorComponent, Editable } from '../../element/code-editor/code-editor.component';

@Component({
  selector: 'app-task-definition',
  templateUrl: './task-definition.component.html',
  styleUrls: ['./task-definition.component.less'],
  host: { class: 'd-flex flex-column' },
})
@NavbarItem('Task Definition')
export class TaskDefinitionComponent implements OnInit {
  private taskDefinitionId: string;
  private taskDefinition: TaskDefinitionDto = null;

  @ViewChild(CodeEditorComponent, { static: false })
  private editor: CodeEditorComponent;

  private editedFile: EditedFile;

  private readonly loadHandler = path => this.loadFile(path);
  private readonly moveHandler = (fromPath, toPath) => this.moveFile(fromPath, toPath);
  private readonly deleteHandler = path => this.deleteFile(path);
  private readonly openHandler = path => this.openFile(path);

  constructor(private taskDefinitionsService: TaskDefinitionsService,
              private route: ActivatedRoute,
              private globalAlerts: GlobalAlertsService,
              private modalService: ModalService) {
  }

  ngOnInit(): void {
    this.route.params.subscribe(routeParams => {
      this.loadTaskDefinition(routeParams.id);
    });
  }

  loadTaskDefinition(taskDefinitionId): void {
    this.taskDefinitionId = taskDefinitionId;
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
      if (this.editedFile && this.editedFile.modified) {
        return from(this.modalService.confirmation({
          text: 'Another file is modified, any unsaved changes will be lost',
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
        text: content,
        save: () => this.save(),
        modified: false,
      };
    });
  }

  private save(): Promise<void> {
    return this.taskDefinitionsService.setTaskDefinitionWorkspaceFileContent({
      taskDefinitionId: this.taskDefinitionId,
      path: this.editedFile.path,
      body: new Blob([this.editedFile.text]),
    }).toPromise().then(() => {
      this.editedFile.modified = false;
    });
  }
}

interface EditedFile extends Editable {
  path: string;
}
