import { Component, Input, OnInit } from '@angular/core';
import { faSave as farSave } from '@fortawesome/free-regular-svg-icons';
import { CodeEditorComponent } from '../code-editor/code-editor.component';

@Component({
  selector: 'app-editor-toolbar',
  templateUrl: './editor-toolbar.component.html',
  styleUrls: ['./editor-toolbar.component.less'],
})
export class EditorToolbarComponent implements OnInit {
  farSave = farSave;

  @Input()
  editor: CodeEditorComponent;

  constructor() {

  }

  ngOnInit() {

  }

  doSave() {
    this.editor.save();
  }
}
