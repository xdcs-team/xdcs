import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { CloseCallback } from '../../services/modal.service';

@Component({
  selector: 'app-import-file',
  templateUrl: './import-file.component.html',
  styleUrls: ['./import-file.component.less'],
})
export class ImportFileComponent implements OnInit {
  @ViewChild('modal', { static: false })
  private modal;

  @Input()
  parent = null;

  @Output()
  submit = new EventEmitter<[string, Blob, CloseCallback]>();

  filename: string;
  file: File;

  constructor() {

  }

  ngOnInit() {

  }

  doSubmit() {
    this.submit.emit([this.filename, this.file, () => {
      this.modal.closeModal();
    }]);
  }

  isValid() {
    return !!this.file && !!this.filename;
  }

  onChange() {
    if (!this.filename) {
      this.filename = this.file.name;
    }
  }
}
