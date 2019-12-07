import { Component, OnInit } from '@angular/core';
import { NavbarItem } from '../../services/navbar.service';
import { NodesService } from '../../../api/services/nodes.service';
import { NodeDto } from '../../../api/models/node-dto';
import { first } from 'rxjs/operators';
import { NodesDto } from '../../../api/models/nodes-dto';

@Component({
  selector: 'app-nodes',
  templateUrl: './nodes.component.html',
  styleUrls: ['./nodes.component.less'],
})
@NavbarItem('Nodes')
export class NodesComponent implements OnInit {

  cards: Array<NodeDto> = null;

  constructor(private nodesService: NodesService) {
  }

  ngOnInit() {
    this.nodesService.getNodes()
      .pipe(first())
      .subscribe((nodes: NodesDto) => {
        this.cards = nodes.items;
      });
  }
}
