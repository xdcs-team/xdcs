import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-node-card',
  templateUrl: './node-card.component.html',
  styleUrls: ['./node-card.component.less']
})
export class NodeCardComponent implements OnInit {
  private hover: boolean;

  private NodeStatus = NodeStatus;

  @Input()
  data: NodeCardData;

  @Input()
  routerLink: string;

  constructor() {
  }

  ngOnInit() {
  }
}

export class NodeCardData {
  name: string;
  address: string;
  status: NodeStatus;
  routerLink: string;
}

export enum NodeStatus {
  Offline,
  Online,
}
