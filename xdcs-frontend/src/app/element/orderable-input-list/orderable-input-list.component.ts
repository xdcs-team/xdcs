import { Component, ContentChild, Input, TemplateRef } from '@angular/core';
import { faChevronDown, faChevronUp, faPlus, faTrash } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-list-manager',
  templateUrl: './orderable-input-list.component.html',
  styleUrls: ['./orderable-input-list.component.less'],
})

export class OrderableInputListComponent {

  @ContentChild('content', { static: false })
  contentTemplateRef: TemplateRef<undefined>;

  @Input()
  data: Array<any>;

  @Input()
  emptyElement: any;

  faChevronUp = faChevronUp;
  faChevronDown = faChevronDown;
  faTrash = faTrash;
  faPlus = faPlus;

  addElement() {
    const param = Object.assign({}, this.emptyElement);
    this.data.push(param);
  }

  removeElement(param) {
    const items = this.data;
    this.data = items.filter(item => item !== param);
  }

  moveElementUp(param) {
    const index = this.data.indexOf(param);
    if (index > 0 && index < this.data.length) {
      this.swapElements(index - 1, index);
    }
  }

  moveElementDown(param) {
    const index = this.data.indexOf(param);
    if (index >= 0 && index < this.data.length - 1) {
      this.swapElements(index + 1, index);
    }
  }

  createTemplateContext(element?: any) {
    return {
      element,
      data: this.data,
    };
  }

  private swapElements(a, b) {
    [this.data[a], this.data[b]] = [this.data[b], this.data[a]];
  }

}
