import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-node-card',
  templateUrl: './node-card.component.html',
  styleUrls: ['./node-card.component.less'],
})
export class NodeCardComponent {
  hover: boolean;

  @Input()
  data: NodeCardData;

  @Input()
  routerLink: string;

  constructor() {
  }
}

export class NodeCardData {
  name: string;
  address: string;
  status: string;
  routerLink: string;
}
