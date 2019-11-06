import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { first } from 'rxjs/operators';
import { NodeDto } from '../../../api/models/node-dto';
import { NodesService } from '../../../api/services/nodes.service';
import { NodeCardData } from '../../element/node-card/node-card.component';
import { faCircle } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-node',
  templateUrl: './node.component.html',
  styleUrls: ['./node.component.less'],
})
export class NodeComponent implements OnInit {
  node: NodeDto = null;
  nodeId: string;
  @Input()
  data: NodeCardData;
  private faCircle = faCircle;

  constructor(private nodesService: NodesService,
              private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.route.params.subscribe(routeParams => {
      this.loadNode(routeParams.id);
    });
  }

  loadNode(nodeId): void {
    this.nodeId = nodeId;
    this.nodesService.getNode({
      nodeId: this.nodeId,
    }).pipe(first()).subscribe(def => {
      this.node = def;
    });
  }
}

