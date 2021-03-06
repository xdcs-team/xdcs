import { Component, Input } from '@angular/core';
import { NodeDto } from '../../../api/models/node-dto';
import { NodesService } from '../../../api/services/nodes.service';
import { first } from 'rxjs/operators';
import { NodeDetailsDto } from '../../../api/models/node-details-dto';

@Component({
  selector: 'app-node-preview',
  templateUrl: './node-preview.component.html',
  styleUrls: ['./node-preview.component.less'],
})

export class NodePreviewComponent {
  @Input()
  node: NodeDto;

  nodeDetails: NodeDetailsDto = null;

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
