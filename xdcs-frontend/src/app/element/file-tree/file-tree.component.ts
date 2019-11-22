import { Component, Input, OnInit } from '@angular/core';
import { ITreeOptions, KEYS, TREE_ACTIONS, TreeModel, TreeNode } from 'angular-tree-component';
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
  faWrench,
} from '@fortawesome/free-solid-svg-icons';
import { faClipboard as farClipboard, faEdit as farEdit, faTrashAlt as farTrashAlt, } from '@fortawesome/free-regular-svg-icons';
import { Alert, GlobalAlertsService } from '../../services/global-alerts.service';
import { ClipboardService } from 'ngx-clipboard';
import { ModalService } from '../../services/modal.service';

@Component({
  selector: 'app-file-tree',
  templateUrl: './file-tree.component.html',
  styleUrls: ['./file-tree.component.less'],
})
export class FileTreeComponent implements OnInit {
  private farTrashAlt = farTrashAlt;
  private farEdit = farEdit;
  private farClipboard = farClipboard;
  private faExternalLinkAlt = faExternalLinkAlt;
  private faWrench = faWrench;

  @Input()
  loadHandler: (path: string) => Promise<TreeDirectory>;

  @Input()
  moveHandler: (pathFrom: string, pathTo: string) => Promise<void>;

  @Input()
  deleteHandler: (path: string) => Promise<void>;

  @Input()
  renameHandler: (path: string, newName: string) => Promise<void>;

  @Input()
  openHandler: (path: string) => void;

  private nodes: Array<any> = [];
  private options: ITreeOptions = {
    actionMapping: {
      mouse: {
        dblClick: (tree, node, $event) => {
          if (node.hasChildren) {
            TREE_ACTIONS.TOGGLE_EXPANDED(tree, node, $event);
          } else {
            this.startOpening(node);
          }
        },
        drop: (tree: TreeModel, node: TreeNode, $event: any, { from, to }: { from: TreeNode, to: TreeNode }) => {
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
    },
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
        };
      }));
  }

  private loadRoot() {
    this.loadChildren(null).then(nodes => this.nodes = nodes);
  }

  private getModelFromNode(node: TreeNode): TreeFileEntry {
    return node.data.model as TreeFileEntry;
  }

  private nodeToPath(node: TreeNode): string {
    if (!node || !node.data.name) {
      return '/';
    }

    const name = node.data.name;
    if (node.parent) {
      const parentPath = this.nodeToPath(node.parent);
      const parentPathWithSlash = parentPath.endsWith('/') ? parentPath : parentPath + '/';
      return parentPathWithSlash + name;
    } else {
      return '/' + name;
    }
  }

  private getIcon(file: TreeFileEntry, expanded: boolean = false) {
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

  private copyToClipboard(content: string) {
    this.clipboardService.copyFromContent(content);
  }

  private startRenaming(node: TreeNode) {
    this.globalAlertsService.addAlert(
      new Alert('danger', 'Renaming files is not supported yet', 5));
  }

  private startDeleting(node: TreeNode) {
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
      }).catch(() => {
        this.globalAlertsService.addAlert(
          new Alert('danger', 'Failed to delete the file', 'long'));
      }).finally(() => closeCallback());
    });
  }

  private startOpening(node: TreeNode) {
    const path = this.nodeToPath(node);
    this.openHandler(path);
  }

  private openAttributes(node: TreeNode) {

  }
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
