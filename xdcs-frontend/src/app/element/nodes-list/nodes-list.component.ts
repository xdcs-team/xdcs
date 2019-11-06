import { Component, Input, OnInit } from '@angular/core';
import { NavbarItem } from '../../services/navbar.service';
import { NodesService } from '../../../api/services/nodes.service';
import { NodeDto } from '../../../api/models/node-dto';
import { NodeCardData, NodeStatus } from '../node-card/node-card.component';
import { first } from 'rxjs/operators';
import { NodesDto } from '../../../api/models/nodes-dto';

@Component({
  selector: 'app-nodes-list',
  templateUrl: './nodes-list.component.html',
  styleUrls: ['./nodes-list.component.less'],
})
@NavbarItem('Nodes')
export class NodesListComponent implements OnInit {

  @Input()
  cards: Array<NodeCardData> = [];
  private queried = false;

  constructor(private nodesService: NodesService) {

  }

  private static mapNodeToCard(node: NodeDto): NodeCardData {
    return {
      name: node.name,
      address: node.address,
      status: NodesListComponent.mapNodeStatus(node.status),
      routerLink: '/nodes/' + node.id,
    };
  }

  private static mapNodeStatus(status: 'offline' | 'unavailable' | 'ready' | 'busy'): NodeStatus {
    switch (status) {
      case 'ready':
        return NodeStatus.Ready;
      case 'offline':
        return NodeStatus.Offline;
      case 'unavailable':
        return NodeStatus.Unavailable;
      case 'busy':
        return NodeStatus.Busy;
      default:
        return NodeStatus.Unknown;
    }
  }

  ngOnInit() {
    this.nodesService.getNodes()
      .pipe(first())
      .subscribe((nodes: NodesDto) => {
        this.queried = true;
        this.cards = nodes.items.map(node => NodesListComponent.mapNodeToCard(node));
      });
  }
}
