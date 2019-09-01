import { Component, EventEmitter, Input, Output } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap';

@Component({
  selector: 'app-modal',
  templateUrl: './modal.component.html',
  styleUrls: ['./modal.component.less']
})
export class ModalComponent {
  @Input()
  title = '';

  @Input()
  closeable = true;

  @Output()
  close = new EventEmitter();

  constructor(private modalRef: BsModalRef) {

  }

  closeModal() {
    this.close.emit();
    this.modalRef.hide();
  }
}
