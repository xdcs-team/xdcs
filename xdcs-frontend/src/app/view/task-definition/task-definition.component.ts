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
import { CloseCallback, ModalService } from '../../services/modal.service';
import { CodeEditorComponent, Editable, EditableMode } from '../../element/code-editor/code-editor.component';

@Component({
  selector: 'app-task-definition',
  templateUrl: './task-definition.component.html',
  styleUrls: ['./task-definition.component.less'],
  host: { class: 'd-flex flex-column' },
})
@NavbarItem('Task Definition')
export class TaskDefinitionComponent implements OnInit {
  private taskDefinitionId: string;
  taskDefinition: TaskDefinitionDto = null;

  editedFile: EditedFile;

  readonly loadHandler = path => this.loadFile(path);
  readonly moveHandler = (fromPath, toPath) => this.moveFile(fromPath, toPath);
  readonly deleteHandler = path => this.deleteFile(path);
  readonly openHandler = path => this.openFile(path);

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
          .pipe(map(closeCallback => {
            closeCallback();
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
        mode: getModeFromPath(path),
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

  onCreateFile([filename, closeCallback]: [string, CloseCallback]) {
    return this.taskDefinitionsService.setTaskDefinitionWorkspaceFileContent({
      taskDefinitionId: this.taskDefinitionId,
      path: this.getSelectedDirectory() + '/' + filename,
      body: new Blob(['']),
    }).toPromise().then(() => {
      closeCallback();
    });
  }

  onCreateDirectory([filename, closeCallback]: [string, CloseCallback]) {
    // TODO implement
  }

  private getSelectedDirectory(): string {
    // TODO implement
    return '';
  }
}

interface EditedFile extends Editable {
  path: string;
}


function getModeFromPath(path: string): EditableMode {
  const filename = path.substr(path.lastIndexOf('/') + 1);
  if (filename.startsWith('Dockerfile')) {
    return EditableMode.DOCKERFILE;
  }

  if (filename.toLowerCase() === 'makefile') {
    return EditableMode.MAKEFILE;
  }

  const extension = path.substr(path.lastIndexOf('.') + 1);
  switch (extension) {
    default:
      return EditableMode.PLAIN_TEXT;

    case 'js':
      return EditableMode.JAVASCRIPT;

    case 'java':
      return EditableMode.JAVA;

    case 'sh':
      return EditableMode.SH;

    case 'json':
      return EditableMode.JSON;

    case 'xml':
      return EditableMode.XML;

    case 'yaml':
    case 'yml':
      return EditableMode.YAML;

    case 'c':
    case 'cpp':
    case 'h':
    case 'hpp':
      return EditableMode.C_CPP;

    case 'cs':
      return EditableMode.C_SHARP;

    case 'clj':
    case 'cljs':
    case 'cljc':
    case 'edn':
      return EditableMode.CLOJURE;

    case 'rs':
    case 'rlib':
      return EditableMode.RUST;

    case 'scala':
      return EditableMode.SCALA;

    case 'properties':
    case 'conf':
      return EditableMode.PROPERTIES;

    case 'toml':
      return EditableMode.TOML;

    case 'md':
      return EditableMode.MARKDOWN;

    case 'jl':
      return EditableMode.JULIA;

    case 'py':
      return EditableMode.PYTHON;

    case 'groovy':
      return EditableMode.GROOVY;

    case 'kt':
      return EditableMode.KOTLIN;

    case 'hs':
      return EditableMode.HASKELL;

    case 'ex':
    case 'exs':
      return EditableMode.ELIXIR;
  }
}
