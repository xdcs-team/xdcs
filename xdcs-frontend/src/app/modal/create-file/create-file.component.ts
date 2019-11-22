import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { CloseCallback } from '../../services/modal.service';

@Component({
  selector: 'app-create-file',
  templateUrl: './create-file.component.html',
  styleUrls: ['./create-file.component.less'],
})
export class CreateFileComponent implements OnInit {
  @ViewChild('modal', { static: false })
  private modal;

  @Input()
  isDirectory = false;

  @Output()
  submit = new EventEmitter<[string, CloseCallback]>();

  filename: string;

  constructor() {

  }

  ngOnInit() {

  }

  doSubmit() {
    this.submit.emit([this.filename, () => {
      this.modal.closeModal();
    }]);
  }
}
