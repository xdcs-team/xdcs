import { AfterViewInit, Component, ElementRef, Input, ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-ansi-to-html',
  template: '',
  styleUrls: ['./ansi-to-html.component.less'],
  encapsulation: ViewEncapsulation.None,
})
export class AnsiToHtmlComponent implements AfterViewInit {
  @Input()
  convert: any;

  @Input()
  data: string;

  constructor(private host: ElementRef) {

  }

  ngAfterViewInit(): void {
    this.host.nativeElement.innerHTML = this.convert.toHtml(this.data);
  }
}
