import { Component, ContentChild, Input, OnInit, TemplateRef } from '@angular/core';

@Component({
  selector: 'app-fullscreen-list',
  templateUrl: './fullscreen-list.component.html',
  styleUrls: ['./fullscreen-list.component.less'],
})
export class FullscreenListComponent implements OnInit {
  @ContentChild('header', { static: false })
  headerTemplateRef: TemplateRef<undefined>;

  @ContentChild('element', { static: false })
  elementTemplateRef: TemplateRef<undefined>;

  @ContentChild('details', { static: false })
  detailsTemplateRef: TemplateRef<undefined>;

  @Input()
  data: Array<any>;

  private selected: any = undefined;

  constructor() {

  }

  ngOnInit() {

  }

  createTemplateContext(element?: any) {
    return {
      element,
      selected: this.selected,
    };
  }
}
