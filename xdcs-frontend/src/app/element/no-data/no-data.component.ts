import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-no-data',
  templateUrl: './no-data.component.html',
  styleUrls: ['./no-data.component.less']
})
export class NoDataComponent implements OnInit {
  @Input()
  message = 'No data :(';

  @Input()
  mode = 'light';

  constructor() {

  }

  ngOnInit() {

  }
}
