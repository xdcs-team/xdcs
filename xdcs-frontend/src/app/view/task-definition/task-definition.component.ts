import { Component, OnInit, ViewChild } from '@angular/core';
import { NavbarItem } from '../../services/navbar.service';
import { TaskDefinitionDto } from '../../../api/models/task-definition-dto';
import { TaskDefinitionsService } from '../../../api/services/task-definitions.service';
import { first, flatMap, map } from 'rxjs/operators';
import { ActivatedRoute } from '@angular/router';
import { Alert, GlobalAlertsService } from '../../services/global-alerts.service';
import { FileTreeComponent, TreeDirectory, TreeFileType } from '../../element/file-tree/file-tree.component';
import { from, of } from 'rxjs';
import { CloseCallback, ModalService } from '../../services/modal.service';
import { Editable, EditableMode } from '../../element/code-editor/code-editor.component';
import { FileDto } from '../../../api/models/file-dto';
import { FileType } from '../../../api/models/file-type';
import { CreateFileComponent } from '../../modal/create-file/create-file.component';
import { PathUtils } from '../../utils/path-utils';
import { BlobUtils } from '../../utils/blob-utils';
import { faAngleLeft } from '@fortawesome/free-solid-svg-icons';
import { ImportFileComponent } from '../../modal/import-file/import-file.component';

@Component({
  selector: 'app-task-definition',
  templateUrl: './task-definition.component.html',
  styleUrls: ['./task-definition.component.less'],
  host: { class: 'd-flex flex-column' },
})
@NavbarItem('Task Definition')
export class TaskDefinitionComponent implements OnInit {
  faAngleLeft = faAngleLeft;

  private taskDefinitionId: string;

  @ViewChild(FileTreeComponent, { static: false })
  private fileTree: FileTreeComponent;

  taskDefinition: TaskDefinitionDto = null;

  editedFile: EditedFile;

  readonly loadHandler = path => this.loadFile(path);
  readonly moveHandler = (fromPath, toPath) => this.moveFile(fromPath, toPath);
  readonly deleteHandler = path => this.deleteFile(path);
  readonly openHandler = path => this.openFile(path);
  readonly createFileHandler = path => this.createFile(path);
  readonly createDirectoryHandler = path => this.createDirectory(path);
  readonly importFileHandler = path => this.startImportingFile(path);

  constructor(private taskDefinitionsService: TaskDefinitionsService,
              private route: ActivatedRoute,
              private globalAlerts: GlobalAlertsService,
              private modalService: ModalService) {
  }

  ngOnInit(): void {
    this.route.params.subscribe(routeParams => {
      this.loadTaskDefinition(routeParams.definitionId);
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

  private saveTaskDefinition(definition: TaskDefinitionDto) {
    this.taskDefinitionsService.updateTaskDefinition({
      taskDefinitionId: this.taskDefinitionId,
      body: definition,
    }).pipe(first()).subscribe(_ => {
      this.globalAlerts.addAlert(new Alert('success', 'Task definition saved', 'short'));
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
      flatMap(blob => from(BlobUtils.toString(blob, 'utf-8')))
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


  createFile(path: string): void {
    return this.modalService.show(CreateFileComponent, true, {
      isDirectory: false,
      parent: PathUtils.rstrip(path) + '/',
    }).content.submit.subscribe(([filename, closeCallback]) => {
      this.taskDefinitionsService.setTaskDefinitionWorkspaceFileContent({
        taskDefinitionId: this.taskDefinitionId,
        path: PathUtils.join(path, filename),
        body: new Blob(['']),
      }).toPromise()
        .then(closeCallback)
        .then(() => this.fileTree.refreshDirectory(path));
    });
  }

  createDirectory(path: string): void {
    this.modalService.show(CreateFileComponent, true, {
      isDirectory: true,
      parent: PathUtils.rstrip(path) + '/',
    }).content.submit.subscribe(([filename, closeCallback]) => {
      return this.taskDefinitionsService.setTaskDefinitionWorkspaceFile({
        taskDefinitionId: this.taskDefinitionId,
        path: PathUtils.join(path, filename),
        body: {
          type: FileType.Directory,
        } as FileDto,
      }).toPromise()
        .then(closeCallback)
        .then(() => this.fileTree.refreshDirectory(path));
    });
  }

  startImportingFile(path: string) {
    return this.modalService.show(ImportFileComponent, true, {
      parent: PathUtils.rstrip(path) + '/',
    }).content.submit.subscribe(([filename, file, closeCallback]: [string, Blob, CloseCallback]) => {
      // set application/octet-stream as type, because the generated OpenAPI code
      // takes the type from Blob and not from the endpoint definition
      file = new File([file], name, { type: 'application/octet-stream' });
      this.taskDefinitionsService.setTaskDefinitionWorkspaceFileContent({
        taskDefinitionId: this.taskDefinitionId,
        path: PathUtils.join(path, filename),
        body: file,
      }).toPromise()
        .then(closeCallback)
        .then(() => this.fileTree.refreshDirectory(path));
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
