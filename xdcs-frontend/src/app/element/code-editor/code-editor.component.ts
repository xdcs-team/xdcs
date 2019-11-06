import { AfterViewInit, Component, Input, OnInit, ViewChild } from '@angular/core';
import { AceEditorComponent } from 'ng2-ace-editor';

@Component({
  selector: 'app-code-editor',
  templateUrl: './code-editor.component.html',
  styleUrls: ['./code-editor.component.less'],
})
export class CodeEditorComponent implements OnInit, AfterViewInit {
  @ViewChild('editor', { static: false })
  private readonly editor: AceEditorComponent;

  @ViewChild('wrapper', { static: false })
  private readonly wrapper;

  private readonly options = {
    fontFamily: 'Roboto Mono',
    fontSize: '14pt',
    scrollPastEnd: 0.8,
    useSoftTabs: true,
    autoScrollEditorIntoView: true,
  };

  @Input()
  editable: Editable;

  private oldEditable: Editable;

  constructor() {

  }

  ngOnInit() {

  }

  ngAfterViewInit() {
    this.refreshHeight();
  }

  private refreshHeight() {
    // probably a bug in Firefox / Angular on Firefox:
    //    when container height is set as a percentage, ngAfterViewInit fires
    //    before the container has been rendered; we have to check whether
    //    the container which the height is computed upon is properly rendered
    if (this.editor.getEditor().renderer.container.clientHeight === 0) {
      setTimeout(() => this.refreshHeight(), 50);
      return;
    }

    this.editor.getEditor().resize();
  }

  isOpened() {
    return !!this.editable;
  }

  undo() {
    this.editor.getEditor().undo();
  }

  redo() {
    this.editor.getEditor().redo();
  }

  save() {
    this.editable.save();
  }

  aceTextChanged() {
    if (this.editable !== this.oldEditable) {
      // editable changed, this change was not caused by the user
      this.oldEditable = this.editable;
    } else {
      this.editable.modified = true;
    }
  }
}

export interface Editable {
  text: string;
  modified: boolean;

  save(): Promise<void>;
}
