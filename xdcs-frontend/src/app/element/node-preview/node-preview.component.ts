import { Component, Input, OnInit } from '@angular/core';
import { NodeDto } from '../../../api/models/node-dto';

@Component({
  selector: 'app-node-preview',
  templateUrl: './node-preview.component.html',
  styleUrls: ['./node-preview.component.less'],
})
export class NodePreviewComponent implements OnInit {
  @Input()
  node: NodeDto;

  constructor() {

  }

  ngOnInit() {

  }
}
