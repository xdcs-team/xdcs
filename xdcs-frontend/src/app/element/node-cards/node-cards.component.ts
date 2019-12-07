import { Component, Input, OnInit } from '@angular/core';
import { NodeCardData } from '../node-card/node-card.component';
import { NodesService } from '../../../api/services/nodes.service';
import { NodesDto } from '../../../api/models/nodes-dto';
import { NodeDto } from '../../../api/models/node-dto';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-node-cards',
  templateUrl: './node-cards.component.html',
  styleUrls: ['./node-cards.component.less'],
})
export class NodeCardsComponent implements OnInit {
  @Input()
  cards: Array<NodeCardData> = [];

  queried = false;

  constructor(private nodesService: NodesService) {
  }

  ngOnInit() {
    this.nodesService.getNodes()
      .pipe(first())
      .subscribe((nodes: NodesDto) => {
        this.queried = true;
        this.cards = nodes.items.map(node => this.mapNodeToCard(node));
      });
  }

  private mapNodeToCard(node: NodeDto): NodeCardData {
    return {
      name: node.name,
      address: node.address,
      status: node.status,
      routerLink: '/nodes/' + node.id,
    };
  }
}
