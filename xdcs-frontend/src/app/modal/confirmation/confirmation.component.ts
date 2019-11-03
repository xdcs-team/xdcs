import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';

@Component({
  selector: 'app-confirmation',
  templateUrl: './confirmation.component.html',
  styleUrls: ['./confirmation.component.less'],
})
export class ConfirmationComponent implements OnInit {
  @ViewChild('modal', { static: false })
  private modal;

  @Input()
  title = '';

  @Input()
  text = 'Are you sure?';

  @Input()
  cancelText = 'Cancel';

  @Input()
  confirmText = 'Confirm';

  @Input()
  type: 'danger' | 'warning' | 'success' = 'success';

  @Output()
  confirm = new EventEmitter();

  @Output()
  cancel = new EventEmitter();

  constructor() {

  }

  ngOnInit() {

  }

  doCancel() {
    this.cancel.emit();
    this.modal.closeModal();
  }

  doConfirm() {
    this.confirm.emit(() => {
      this.modal.closeModal();
    });
  }
}
