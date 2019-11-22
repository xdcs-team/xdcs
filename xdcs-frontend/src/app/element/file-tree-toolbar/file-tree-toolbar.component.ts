import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { faFolderPlus, faPlusCircle } from '@fortawesome/free-solid-svg-icons';
import { CloseCallback, ModalService } from '../../services/modal.service';
import { CreateFileComponent } from '../../modal/create-file/create-file.component';

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
  createFile = new EventEmitter<[string, CloseCallback]>();

  @Output()
  createDirectory = new EventEmitter<[string, CloseCallback]>();

  constructor(private modalService: ModalService) {

  }

  ngOnInit() {

  }

  doCreateFile() {
    this.modalService.show(CreateFileComponent, true, {
      isDirectory: false,
    }).content.submit.subscribe(([filename, closeCallback]) => {
      this.createFile.emit([filename, closeCallback]);
    });
  }

  doCreateDirectory() {
    this.modalService.show(CreateFileComponent, true, {
      isDirectory: true,
    }).content.submit.subscribe(([filename, closeCallback]) => {
      this.createDirectory.emit([filename, closeCallback]);
    });
  }
}
