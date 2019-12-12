import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { LogDto, NodesDto, TaskDto } from '../../../api/models';
import { first } from 'rxjs/operators';
import { WebSocketSubject } from 'rxjs/internal-compatibility';
import { LogLine } from '../log-preview/log-preview.component';
import { LogHandlingService } from '../../../api/services/log-handling.service';
import { NodesService } from '../../../api/services/nodes.service';
import { NodeDto } from '../../../api/models/node-dto';

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


  constructor(private loggingService: LogHandlingService) {
  }

  ngOnInit() {
    this.loggingService.getTaskLogs({
      taskId: this.task.id,
    }).pipe(first()).subscribe(logs => {
      this.currentLineNumber = 0;
      this.logLines = logs.items.map(item => {
        return this.mapToLogLine(item);
      });

      if (this.logsWebSocket) {
        this.logsWebSocket.unsubscribe();
      }

      this.logsWebSocket = new WebSocketSubject(logs.websocketUrl);
      this.logsWebSocket.subscribe(log => {
        this.logLines.push(this.mapToLogLine(log));
      });
    });
  }

  private mapToLogLine(item: LogDto): LogLine {
    return {
      lineNumber: ++this.currentLineNumber,
      time: new Date(item.time),
      contents: atob(item.contents),
      agentTag: item.nodeId,
    } as LogLine;
  }

  ngOnChanges(changes: SimpleChanges) {
    this.ngOnInit();
  }
}
