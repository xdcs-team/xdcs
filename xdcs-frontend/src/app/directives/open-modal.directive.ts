import { Directive, HostListener, Input, TemplateRef } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap';

@Directive({
  selector: '[openModal]'
})
export class OpenModalDirective {
  @Input()
  centered = true;

  @Input()
  modal: string | TemplateRef<any> | any;

  constructor(private modalService: BsModalService) {

  }

  @HostListener('click')
  onClick() {
    this.modalService.show(this.modal, {
      class: this.centered ? 'modal-dialog-centered' : 'modal-dialog',
      animated: true,
    });
  }
}
