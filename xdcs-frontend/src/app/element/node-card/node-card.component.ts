import { Component, Input, OnInit } from '@angular/core';
import { faCircle } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-node-card',
  templateUrl: './node-card.component.html',
  styleUrls: ['./node-card.component.less']
})
export class NodeCardComponent implements OnInit {
  private hover: boolean;

  private NodeStatus = NodeStatus;
  private faCircle = faCircle;

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
  Unknown,
  Offline,
  Online,
}
