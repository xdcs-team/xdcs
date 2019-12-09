import { Component, OnInit } from '@angular/core';
import { NavbarItem } from '../../services/navbar.service';
import { NodesService } from '../../../api/services/nodes.service';
import { NodeDto } from '../../../api/models/node-dto';
import { first } from 'rxjs/operators';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { forkJoin } from 'rxjs';

const nodeIdParam = 'nodeId';

@Component({
  selector: 'app-nodes',
  templateUrl: './nodes.component.html',
  styleUrls: ['./nodes.component.less'],
})
@NavbarItem('Nodes')
export class NodesComponent implements OnInit {

  cards: Array<NodeDto> = null;
  selected: NodeDto;

  constructor(private nodesService: NodesService,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit() {
    this.fetchNodes();
    this.route.params.subscribe(params => {
      this.refreshSelection(params);
    });
  }

  onSelectionChange(selected: NodeDto) {
    this.router.navigate(['nodes', selected.id], {
      replaceUrl: true,
    });
  }

  private fetchNodes() {
    const parameters = this.route.params.pipe(first());
    const nodesDto = this.nodesService.getNodes({}).pipe(first());

    forkJoin([parameters, nodesDto])
      .subscribe(([params, nodes]) => {
        this.cards = nodes.items;
        this.refreshSelection(params);
      });
  }

  private refreshSelection(params: Params) {
    if (!this.cards) {
      return;
    }

    const found = this.cards
      .find(def => def.id === params[nodeIdParam]);
    if (found) {
      this.selected = found;
    }
  }
}
