import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { CloseCallback } from '../../services/modal.service';

@Component({
  selector: 'app-rename-file',
  templateUrl: './rename-file.component.html',
  styleUrls: ['./rename-file.component.less'],
})
export class RenameFileComponent implements OnInit {
  @ViewChild('modal', { static: false })
  private modal;

  @Input()
  parent = null;

  @Input()
  filename = '';

  @Output()
  submit = new EventEmitter<[string, CloseCallback]>();

  constructor() {

  }

  ngOnInit() {

  }

  doSubmit() {
    this.submit.emit([this.filename, () => {
      this.modal.closeModal();
    }]);
  }

  isValid() {
    return !!this.filename;
  }
}
