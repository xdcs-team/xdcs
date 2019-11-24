import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { faFolderPlus, faPlusCircle } from '@fortawesome/free-solid-svg-icons';
import { ModalService } from '../../services/modal.service';

@Component({
  selector: 'app-file-tree-toolbar',
  templateUrl: './file-tree-toolbar.component.html',
  styleUrls: ['./file-tree-toolbar.component.less'],
})
export class FileTreeToolbarComponent implements OnInit {
  private faPlusCircle = faPlusCircle;
  private faFolderPlus = faFolderPlus;

  @Input()
  canCreateFile = true;

  @Output()
  createFile = new EventEmitter<void>();

  @Output()
  createDirectory = new EventEmitter<void>();

  constructor(private modalService: ModalService) {

  }

  ngOnInit() {

  }

  doCreateFile() {
    this.createFile.emit();
  }

  doCreateDirectory() {
    this.createDirectory.emit();
  }
}
