import { Component, ContentChild, Input, TemplateRef } from '@angular/core';
import { faChevronDown, faChevronUp, faPlus, faTrash } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-orderable-input-list',
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

  @Input()
  orderable = true;

  faChevronUp = faChevronUp;
  faChevronDown = faChevronDown;
  faTrash = faTrash;
  faPlus = faPlus;

  addElement() {
    const param = typeof this.emptyElement === 'object' ?
      Object.assign({}, this.emptyElement) :
      this.emptyElement;
    this.data.push(param);
  }

  removeElement(param) {
    const index = this.data.indexOf(param);
    if (index > -1) {
      this.data.splice(index, 1);
    }
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

  createTemplateContext(element: any, index: number) {
    return {
      element,
      data: this.data,
      index,
    };
  }

  private swapElements(a, b) {
    [this.data[a], this.data[b]] = [this.data[b], this.data[a]];
  }

  indexTracker(index: number, value: any) {
    return index;
  }
}
