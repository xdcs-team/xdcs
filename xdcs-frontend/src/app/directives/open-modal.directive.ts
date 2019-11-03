import { Directive, HostListener, Input, TemplateRef } from '@angular/core';
import { ModalService } from '../services/modal.service';

@Directive({
  selector: '[appOpenModal]',
})
export class OpenModalDirective {
  @Input()
  centered = true;

  @Input()
  modal: string | TemplateRef<any> | any;

  constructor(private modalService: ModalService) {

  }

  @HostListener('click')
  onClick() {
    this.modalService.show(this.modal, this.centered);
  }
}
