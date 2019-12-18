import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-or-id',
  templateUrl: './or-id.component.html',
  styleUrls: ['./or-id.component.less'],
})
export class OrIdComponent {

  @Input()
  id: string;

  constructor() { }

}
