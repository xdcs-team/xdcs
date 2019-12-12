import { Component, Input } from '@angular/core';
import * as Convert from 'ansi-to-html';
import { HttpClient } from '@angular/common/http';
import { NodeDto } from '../../../api/models/node-dto';

@Component({
  selector: 'app-log-preview',
  templateUrl: './log-preview.component.html',
  styleUrls: ['./log-preview.component.less'],
  host: { style: 'display: block' },
})
export class LogPreviewComponent {
  timePopupFormat = 'MMM d, y, h:mm:ss.SSS a';

  @Input()
  lines: LogLine[] = [];

  convert: any;

  constructor(private http: HttpClient) {
    this.http.get('/assets/config/palette256_aci.json')
      .subscribe(data => {
        this.convert = new Convert({
          fg: '#ffffff',
          bg: '#252525',
          newline: false,
          escapeXML: false,
          stream: true,
          colors: data,
        });
      });
  }
}

export interface LogLine {
  lineNumber: number;
  time: Date;
  contents: string;
  agentTag: String;
}
