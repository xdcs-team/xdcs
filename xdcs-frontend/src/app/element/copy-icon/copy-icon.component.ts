import { Component, Input } from '@angular/core';
import { faClipboard as farClipboard } from '@fortawesome/free-regular-svg-icons';
import { ClipboardService } from 'ngx-clipboard';

@Component({
  selector: 'app-copy-icon',
  templateUrl: './copy-icon.component.html',
  styleUrls: ['./copy-icon.component.less'],
})
export class CopyIconComponent {
  farClipboard = farClipboard;

  @Input()
  content: any;

  constructor(private clipboardService: ClipboardService) {

  }

  onClick() {
    this.clipboardService.copyFromContent(this.content);
  }
}
