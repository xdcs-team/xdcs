import { Component, OnInit } from '@angular/core';
import { LogLine } from '../../element/log-preview/log-preview.component';
import { first } from 'rxjs/operators';
import { ActivatedRoute } from '@angular/router';
import { LogHandlingService } from '../../../api/services/log-handling.service';
import { LogDto } from '../../../api/models/log-dto';
import { WebSocketSubject } from 'rxjs/internal-compatibility';

@Component({
  selector: 'app-task-log',
  templateUrl: './task-log.component.html',
  styleUrls: ['./task-log.component.less'],
})
export class TaskLogComponent implements OnInit {
  currentLineNumber = 0;

  logLines: LogLine[];
  taskId: string;
  logsWebSocket: WebSocketSubject<LogDto>;

  constructor(private loggingService: LogHandlingService,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.route.params.subscribe(routeParams => {
      this.loggingService.getTaskLogs({
        taskId: routeParams.taskId,
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
    });
  }

  private mapToLogLine(item: LogDto): LogLine {
    return {
      lineNumber: ++this.currentLineNumber,
      time: new Date(item.time),
      contents: atob(item.contents),
    } as LogLine;
  }
}
