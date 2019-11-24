import { AfterViewInit, Component, Input, OnInit, ViewChild } from '@angular/core';
import { AceEditorComponent } from 'ng2-ace-editor';

import 'brace/theme/tomorrow';
import 'brace/mode/c_cpp';
import 'brace/mode/clojure';
import 'brace/mode/csharp';
import 'brace/mode/dockerfile';
import 'brace/mode/elixir';
import 'brace/mode/groovy';
import 'brace/mode/haskell';
import 'brace/mode/javascript';
import 'brace/mode/java';
import 'brace/mode/json';
import 'brace/mode/julia';
import 'brace/mode/kotlin';
import 'brace/mode/makefile';
import 'brace/mode/markdown';
import 'brace/mode/plain_text';
import 'brace/mode/properties';
import 'brace/mode/python';
import 'brace/mode/rust';
import 'brace/mode/scala';
import 'brace/mode/sh';
import 'brace/mode/toml';
import 'brace/mode/xml';
import 'brace/mode/yaml';


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

  readonly options = {
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

export enum EditableMode {
  C_CPP = 'c_cpp',
  CLOJURE = 'clojure',
  C_SHARP = 'csharp',
  DOCKERFILE = 'dockerfile',
  ELIXIR = 'elixir',
  GROOVY = 'groovy',
  HASKELL = 'haskell',
  JAVASCRIPT = 'javascript',
  JAVA = 'java',
  JSON = 'json',
  JULIA = 'julia',
  KOTLIN = 'kotlin',
  MAKEFILE = 'makefile',
  MARKDOWN = 'markdown',
  PLAIN_TEXT = 'plain_text',
  PROPERTIES = 'properties',
  PYTHON = 'python',
  RUST = 'rust',
  SCALA = 'scala',
  SH = 'sh',
  TOML = 'toml',
  XML = 'xml',
  YAML = 'yaml',
}

export interface Editable {
  text: string;
  modified: boolean;
  mode: EditableMode;

  save(): Promise<void>;
}
