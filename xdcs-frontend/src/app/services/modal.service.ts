import { EventEmitter, Injectable, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap';
import { ConfirmationComponent } from '../modal/confirmation/confirmation.component';

export interface ConfirmationParams {
  title?: string;
  text?: string;
  cancelText?: string;
  confirmText?: string;
  type?: 'danger' | 'warning' | 'success';
}

@Injectable({
  providedIn: 'root',
})
export class ModalService {
  onHide: EventEmitter<any>;
  onHidden: EventEmitter<any>;

  constructor(private modalService: BsModalService) {
    this.onHide = modalService.onHide;
    this.onHidden = modalService.onHidden;
  }

  show(modal: string | TemplateRef<any> | any, centered = true, parameters: object = {}): BsModalRef {
    return this.modalService.show(modal, {
      class: centered ? 'modal-dialog-centered' : 'modal-dialog',
      animated: true,
      initialState: parameters,
    });
  }

  confirmation(params: ConfirmationParams): Promise<() => void> {
    return new Promise((resolve, reject) => {
      const modal = this.show(ConfirmationComponent, true, params);
      modal.content.confirm.subscribe(callback => {
        resolve(callback);
      });
      modal.content.cancel.subscribe(() => {
        reject();
      });
    });
  }
}
