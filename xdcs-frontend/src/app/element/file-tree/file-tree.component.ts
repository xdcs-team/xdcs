import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { IActionMapping, ITreeOptions, KEYS, TREE_ACTIONS, TreeComponent, TreeNode } from 'angular-tree-component';
import {
  faExternalLinkAlt,
  faFile,
  faFileArchive,
  faFileCode,
  faFileImage,
  faFilePdf,
  faFolder,
  faFolderOpen,
  faLink,
  faPlus, faUpload,
  faWrench,
} from '@fortawesome/free-solid-svg-icons';
import { faClipboard as farClipboard, faEdit as farEdit, faTrashAlt as farTrashAlt, } from '@fortawesome/free-regular-svg-icons';
import { Alert, GlobalAlertsService } from '../../services/global-alerts.service';
import { ClipboardService } from 'ngx-clipboard';
import { ModalService } from '../../services/modal.service';
import { PathUtils } from '../../utils/path-utils';
import { NewTaskDefinitionComponent } from '../../modal/new-task-definition/new-task-definition.component';
import { FileAttributesSettingsComponent } from '../../modal/file-attributes-settings/file-attributes-settings.component';

@Component({
  selector: 'app-file-tree',
  templateUrl: './file-tree.component.html',
  styleUrls: ['./file-tree.component.less'],
})
export class FileTreeComponent implements OnInit {
  TreeFileType = TreeFileType;

  farTrashAlt = farTrashAlt;
  farEdit = farEdit;
  farClipboard = farClipboard;
  faExternalLinkAlt = faExternalLinkAlt;
  faWrench = faWrench;
  faPlus = faPlus;
  faFolder = faFolder;
  faFile = faFile;
  faUpload = faUpload;

  @ViewChild(TreeComponent, { static: false })
  private treeComponent: TreeComponent;

  @Input()
  editAttributesHandler: (path: string) => Promise<void>;

  @Input()
  loadHandler: (path: string) => Promise<TreeDirectory>;

  @Input()
  moveHandler: (pathFrom: string, pathTo: string) => Promise<void>;

  @Input()
  deleteHandler: (path: string) => Promise<void>;

  @Input()
  renameHandler: (path: string, newName: string) => Promise<void>;

  @Input()
  createFileHandler: (path: string) => void;

  @Input()
  createDirectoryHandler: (path: string) => void;

  @Input()
  importFileHandler: (path: string) => void;

  @Input()
  openHandler: (path: string) => void;
  nodes: Array<any> = [];
  options: ITreeOptions = {
    actionMapping: {
      mouse: {
        dblClick: (tree, node, $event) => {
          if (node.hasChildren) {
            TREE_ACTIONS.TOGGLE_EXPANDED(tree, node, $event);
          } else {
            this.startOpening(node);
          }
        },
        drop: (tree, node, $event, { from, to }: { from: TreeNode, to: TreeNode }) => {
          // use from to get the dragged node.
          // use to.parent and to.index to get the drop location
          // TREE_ACTIONS.MOVE_NODE(tree, node, $event, { from, to });
          this.globalAlertsService.addAlert(
            new Alert('danger', 'Moving files is not supported yet', 5));
        },
      },
      keys: {
        [KEYS.ENTER]: (tree, node, $event) => {
          node.expandAll();
        },
      },
    } as IActionMapping,
    allowDrag: (node) => {
      return true;
    },
    allowDrop: (node) => {
      return true;
    },
    allowDragoverStyling: true,
    levelPadding: 25,
    animateExpand: true,
    getChildren: (node: TreeNode) => this.loadChildren(node),
  };

  constructor(private globalAlertsService: GlobalAlertsService,
              private clipboardService: ClipboardService,
              private modalService: ModalService) {

  }

  ngOnInit() {
    this.loadRoot();
  }

  private loadChildren(root: TreeNode): Promise<any[]> {
    const path = this.nodeToPath(root);
    return this.loadHandler(path).then(dir =>
      dir.entries.map(entry => {
        return {
          name: entry.name,
          model: {
            name: entry.name,
            type: entry.type,
          },
          hasChildren: entry.type === TreeFileType.DIRECTORY,
        } as NodeData;
      }));
  }

  private loadRoot() {
    this.loadChildren(null).then(nodes => this.nodes = nodes);
  }

  getModelFromNode(node: TreeNode): TreeFileEntry {
    return node.data.model as TreeFileEntry;
  }

  nodeToPath(node: TreeNode): string {
    if (!node || !node.data.name) {
      return '/';
    }

    const name = node.data.name;
    if (node.parent) {
      const parentPath = this.nodeToPath(node.parent);
      return PathUtils.join(parentPath, name);
    } else {
      return '/' + name;
    }
  }

  pathToNode(path: string): TreeNode {
    const parent = PathUtils.parent(path);
    const filename = PathUtils.filename(path);
    if (parent && parent !== '/') {
      const parentNode = this.pathToNode(parent);
      if (!parentNode || !parentNode.children) {
        return null;
      }

      for (const child of parentNode.children) {
        if (child.data.name === filename) {
          return child;
        }
      }

      return null;
    }

    const roots = this.treeComponent.treeModel.roots;
    for (const root of roots) {
      if (root.data.name === filename) {
        return root;
      }
    }

    return null;
  }

  getIcon(file: TreeFileEntry, expanded: boolean = false) {
    switch (file.type) {
      case TreeFileType.DIRECTORY:
        return expanded ? faFolderOpen : faFolder;
      case TreeFileType.FILE_CODE:
        return faFileCode;
      case TreeFileType.FILE_MEDIA:
        return faFileImage;
      case TreeFileType.FILE_PDF:
        return faFilePdf;
      case TreeFileType.FILE_ARCHIVE:
        return faFileArchive;
      case TreeFileType.FILE_SYMLINK:
        return faLink;
      case TreeFileType.FILE_OTHER:
      case TreeFileType.FILE_BINARY:
      default:
        return faFile;
    }
  }

  copyToClipboard(content: string) {
    this.clipboardService.copyFromContent(content);
  }

  startRenaming(node: TreeNode) {
    this.globalAlertsService.addAlert(
      new Alert('danger', 'Renaming files is not supported yet', 5));
  }

  startDeleting(node: TreeNode) {
    const path = this.nodeToPath(node);
    this.modalService.confirmation({
      title: 'Delete file',
      text: 'Are you sure you want to delete ' + path + '?',
      confirmText: 'Delete',
      type: 'danger',
    }).then(closeCallback => {
      this.deleteHandler(path).then(() => {
        this.globalAlertsService.addAlert(
          new Alert('success', 'File deleted', 'short'));
        this.refreshDirectory(PathUtils.parent(path));
      }).catch(() => {
        this.globalAlertsService.addAlert(
          new Alert('danger', 'Failed to delete the file', 'long'));
      }).finally(closeCallback);
    });
  }

  startOpening(node: TreeNode) {
    const path = this.nodeToPath(node);
    this.openHandler(path);
  }

  startCreatingFile(node: TreeNode) {
    const path = this.nodeToPath(node);
    this.createFileHandler(path);
  }

  startCreatingDirectory(node: TreeNode) {
    const path = this.nodeToPath(node);
    this.createDirectoryHandler(path);
  }

  startImportingFile(node: TreeNode) {
    const path = this.nodeToPath(node);
    this.importFileHandler(path);
  }

  openAttributes(node: TreeNode) {
    const path = this.nodeToPath(node);
    this.editAttributesHandler(path);
  }

  refreshDirectory(path: string): void {
    const node = this.pathToNode(path);
    if (node) {
      this.loadChildren(node).then(children => {
        node.data.children = children;
        this.treeComponent.treeModel.update();
      });
    } else {
      this.loadRoot();
    }
  }
}

interface NodeData {
  name: string;
  model: {
    name: string;
    type: TreeFileType;
  };
  hasChildren: boolean;
}

export enum TreeFileType {
  DIRECTORY,
  FILE_BINARY,
  FILE_CODE,
  FILE_MEDIA,
  FILE_PDF,
  FILE_ARCHIVE,
  FILE_SYMLINK,
  FILE_OTHER,
}

export interface TreeFileEntry {
  name: string;
  type: TreeFileType;
}

export interface TreeDirectory {
  entries: Array<TreeFileEntry>;
}

export interface SelectedFile {
  path: string;
  isDirectory: boolean;
}
