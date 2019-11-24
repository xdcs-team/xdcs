import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgxSpinnerService } from 'ngx-spinner';

@Component({
  selector: 'app-loading',
  templateUrl: './loading.component.html',
  styleUrls: ['./loading.component.less'],
})
export class LoadingComponent implements OnInit, OnDestroy {
  name: string = Math.random().toString(36).substring(2);

  constructor(private spinner: NgxSpinnerService) {

  }

  ngOnInit() {
    this.spinner.show(this.name);
  }

  ngOnDestroy() {
    this.spinner.hide(this.name);
  }
}
