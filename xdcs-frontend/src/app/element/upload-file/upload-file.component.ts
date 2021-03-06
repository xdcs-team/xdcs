import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-upload-file',
  templateUrl: './upload-file.component.html',
  styleUrls: ['./upload-file.component.less'],
})
export class UploadFileComponent {
  filenames: string[];

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

  @Output()
  fileChange = new EventEmitter<File>();

  @Input()
  file(value: File) {
    if (value) {
      this.files = [value];
    } else {
      this.files = null;
    }
  }

  constructor() {

  }

  filesChanged(files: File[]) {
    if (!this.multiple) {
      files = [files[files.length - 1]];
      this.fileChange.emit(files[0]);
    }
    this.filesChange.emit(files);

    this.filenames = files.map(file => file.name);
  }
}
