import { Component, Input, OnInit } from '@angular/core';
import { NodeCardData, NodeStatus } from '../node-card/node-card.component';

@Component({
  selector: 'app-node-cards',
  templateUrl: './node-cards.component.html',
  styleUrls: ['./node-cards.component.less']
})
export class NodeCardsComponent implements OnInit {
  @Input()
  cards: Array<NodeCardData> = [];

  constructor() {
  }

  ngOnInit() {
    for (let i = 0; i < 17; ++i) {
      this.cards.push({
        name: 'Node ' + i,
        address: '127.0.1.' + i,
        status: Math.floor(Math.random() * 4) === 0 ? NodeStatus.Offline : NodeStatus.Online,
        routerLink: '/agents',
      });
    }
  }
}
