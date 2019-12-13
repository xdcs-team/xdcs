import { Component, Input } from '@angular/core';
import { NodeDto } from '../../../api/models/node-dto';
import { NodeDetailsDto } from '../../../api/models/node-details-dto';

@Component({
  selector: 'app-node-details',
  templateUrl: './node-details.component.html',
  styleUrls: ['./node-details.component.less'],
})
export class NodeDetailsComponent {
  @Input()
  node: NodeDto;

  @Input()
  nodeDetails: NodeDetailsDto;

  constructor() {
  }
}
