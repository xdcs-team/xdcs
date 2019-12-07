import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-node-basic-info',
  templateUrl: './node-basic-info.component.html',
  styleUrls: ['./node-basic-info.component.less'],
})
export class NodeBasicInfoComponent {
  @Input()
  data: any;

  constructor() {
  }
}
