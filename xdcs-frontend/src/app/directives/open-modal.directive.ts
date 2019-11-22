import { Directive, EventEmitter, HostListener, Input, Output, TemplateRef } from '@angular/core';
import { ModalService } from '../services/modal.service';

@Directive({
  selector: '[appOpenModal]',
})
export class OpenModalDirective {
  @Input()
  centered = true;

  @Input()
  modal: string | TemplateRef<any> | any;

  @Output()
  modalHidden = new EventEmitter();

  constructor(private modalService: ModalService) {

  }

  @HostListener('click')
  onClick() {
    this.modalService.show(this.modal, this.centered);
    this.modalService.onHidden.subscribe(() => this.modalHidden.emit());
  }
}
