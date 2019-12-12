import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { LogDto, TaskDto } from '../../../api/models';
import { first } from 'rxjs/operators';
import { WebSocketSubject } from 'rxjs/internal-compatibility';
import { LogLine } from '../log-preview/log-preview.component';
import { LogHandlingService } from '../../../api/services/log-handling.service';

@Component({
  selector: 'app-task-result-preview',
  templateUrl: './task-result-preview.component.html',
  styleUrls: ['./task-result-preview.component.less'],
})
export class TaskResultPreviewComponent implements OnInit, OnChanges {
  currentLineNumber = 0;

  logLines: LogLine[];
  taskId: string;
  logsWebSocket: WebSocketSubject<LogDto>;
  @Input()
  task: TaskDto;
  tagWidth = 0;


  constructor(private loggingService: LogHandlingService) {
  }

  ngOnInit() {
    this.loggingService.getTaskLogs({
      taskId: this.task.id,
    }).pipe(first()).subscribe(logs => {
      this.currentLineNumber = 0;
      this.logLines = logs.items.map(item => {
        this.updateTagWidth(item);
        return this.mapToLogLine(item);
      });

      if (this.logsWebSocket) {
        this.logsWebSocket.unsubscribe();
      }

      this.logsWebSocket = new WebSocketSubject(logs.websocketUrl);
      this.logsWebSocket.subscribe(log => {
        this.updateTagWidth(log);
        this.logLines.push(this.mapToLogLine(log));
      });
    });
  }

  private updateTagWidth(log: LogDto) {
    if (log.nodeId.length > this.tagWidth) {
      this.tagWidth = log.nodeId.length;
    }
  }

  private mapToLogLine(item: LogDto): LogLine {
    return {
      lineNumber: ++this.currentLineNumber,
      time: new Date(item.time),
      contents: atob(item.contents),
      tag: item.nodeId,
    } as LogLine;
  }

  ngOnChanges(changes: SimpleChanges) {
    this.ngOnInit();
  }
}
