import { Component, ContentChild, EventEmitter, Input, Output, TemplateRef } from '@angular/core';

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

  @Input()
  selected: any = undefined;

  @Output()
  selectedChange = new EventEmitter<any>();

  constructor() {

  }

  createTemplateContext(element?: any) {
    return {
      element,
      selected: this.selected,
    };
  }

  select(selected: any) {
    this.selected = selected;
    this.selectedChange.emit(selected);
  }
}
