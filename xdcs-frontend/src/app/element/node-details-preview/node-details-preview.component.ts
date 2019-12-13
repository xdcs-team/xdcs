import { Component, Input, OnInit } from '@angular/core';
import { NodeDto } from '../../../api/models/node-dto';
import { NodeDetailsDto } from '../../../api/models/node-details-dto';
import { NodesService } from '../../../api/services/nodes.service';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-node-details-preview',
  templateUrl: './node-details-preview.component.html',
  styleUrls: ['./node-details-preview.component.less'],
})
export class NodeDetailsPreviewComponent implements OnInit {

  @Input()
  node: NodeDto;

  @Input()
  nodeDetails: NodeDetailsDto;

  constructor(private nodesService: NodesService) {
  }

  ngOnInit(): void {
    if (this.node.status !== 'offline' && this.node.status !== 'unavailable') {
      this.nodesService.getNodeDetails({ nodeId: this.node.id })
        .pipe(first())
        .subscribe(details => {
          this.nodeDetails = details;
        });
    }
  }
}
