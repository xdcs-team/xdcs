import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';

@Component({
  selector: 'app-code-editor',
  templateUrl: './code-editor.component.html',
  styleUrls: ['./code-editor.component.less']
})
export class CodeEditorComponent implements OnInit, AfterViewInit {
  text: string;
  options = {
    fontFamily: 'Roboto Mono',
    fontSize: '14pt',
    scrollPastEnd: 0.8,
    useSoftTabs: true,
    autoScrollEditorIntoView: true,
  };

  @ViewChild('editor', { static: false })
  editor;

  @ViewChild('wrapper', { static: false })
  wrapper;

  constructor() {

  }

  ngOnInit() {

  }

  ngAfterViewInit() {
    this.refreshHeight();
  }

  onChange($event: any) {

  }

  refreshHeight() {
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
}
