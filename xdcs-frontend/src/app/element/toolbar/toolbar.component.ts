import { AfterViewInit, Component, Input, TemplateRef } from '@angular/core';

@Component({
  selector: 'app-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.less'],
})
export class ToolbarComponent implements AfterViewInit {
  @Input()
  buttons: Array<TemplateRef<any>>;

  constructor() {

  }

  ngAfterViewInit(): void {

  }
}
