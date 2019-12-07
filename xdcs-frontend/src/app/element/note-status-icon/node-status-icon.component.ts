import { Component, Input } from '@angular/core';
import { faCircle } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-node-status-icon',
  templateUrl: './node-status-icon.component.html',
  styleUrls: ['./node-status-icon.component.less'],
})
export class NodeStatusIconComponent {
  faCircle = faCircle;

  @Input()
  node: any;
  @Input()
  size = 'md';

  constructor() {
  }
}
