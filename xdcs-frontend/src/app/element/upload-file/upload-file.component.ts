import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-upload-file',
  templateUrl: './upload-file.component.html',
  styleUrls: ['./upload-file.component.less'],
})
export class UploadFileComponent {
  @Input()
  label: string = null;

  @Input()
  multiple = false;

  @Input()
  accept = '*';

  @Input()
  maxSize: number = null;

  @Input()
  files: File[];

  @Output()
  filesChange = new EventEmitter<File[]>();

  constructor() {

  }

  filesChanged($event: File[]) {
    this.filesChange.emit($event);
  }
}