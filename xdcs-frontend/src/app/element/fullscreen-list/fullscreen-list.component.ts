import { Component, ContentChild, Input, TemplateRef } from '@angular/core';

@Component({
  selector: 'app-fullscreen-list',
  templateUrl: './fullscreen-list.component.html',
  styleUrls: ['./fullscreen-list.component.less'],
  host: { class: 'flex-fill d-flex' },
})
export class FullscreenListComponent {
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

  createTemplateContext(element?: any) {
    return {
      element,
      selected: this.selected,
    };
  }
}
