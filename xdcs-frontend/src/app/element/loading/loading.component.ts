import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgxSpinnerService } from 'ngx-spinner';

@Component({
  selector: 'app-loading',
  templateUrl: './loading.component.html',
  styleUrls: ['./loading.component.less'],
})
export class LoadingComponent implements OnInit, OnDestroy {
  private name: string;

  constructor(private spinner: NgxSpinnerService) {

  }

  ngOnInit() {
    this.spinner.show(this.getName());
  }

  ngOnDestroy() {
    this.spinner.hide(this.getName());
  }

  private getName() {
    return this.name ?
      this.name :
      this.name = Math.random().toString(36).substring(2);
  }
}
