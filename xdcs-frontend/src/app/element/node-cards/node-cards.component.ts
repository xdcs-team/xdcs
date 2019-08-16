import { Component, Input, OnInit } from '@angular/core';
import { NodeCardData, NodeStatus } from '../node-card/node-card.component';
import { NodesService } from '../../../api/services/nodes.service';
import { NodesDto } from '../../../api/models/nodes-dto';
import { NodeDto } from '../../../api/models/node-dto';

@Component({
  selector: 'app-node-cards',
  templateUrl: './node-cards.component.html',
  styleUrls: ['./node-cards.component.less']
})
export class NodeCardsComponent implements OnInit {
  @Input()
  cards: Array<NodeCardData> = [];

  private queried = false;

  constructor(private nodesService: NodesService) {

  }

  ngOnInit() {
    this.nodesService.getNodes()
      .subscribe((nodes: NodesDto) => {
        this.queried = true;
        this.cards = nodes.items.map(node => this.mapNodeToCard(node));
      });
  }

  private mapNodeToCard(node: NodeDto): NodeCardData {
    return {
      name: node.name,
      address: node.address,
      status: this.mapStatus(node.status),
      routerLink: '/nodes/' + node.id,
    };
  }

  private mapStatus(status: 'online' | 'offline'): NodeStatus {
    switch (status) {
      case 'online':
        return NodeStatus.Online;
      case 'offline':
        return NodeStatus.Offline;
      default:
        return NodeStatus.Unknown;
    }
  }
}
