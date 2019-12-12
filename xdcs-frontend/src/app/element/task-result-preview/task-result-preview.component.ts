import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { ArtifactDto, LogDto, NodeDto, NodesDto, TaskDto } from '../../../api/models';
import { first } from 'rxjs/operators';
import { WebSocketSubject } from 'rxjs/internal-compatibility';
import { LogLine } from '../log-preview/log-preview.component';
import { LogHandlingService } from '../../../api/services/log-handling.service';
import { TasksService } from '../../../api/services/tasks.service';
import { NodesService } from '../../../api/services/nodes.service';

@Component({
  selector: 'app-task-result-preview',
  templateUrl: './task-result-preview.component.html',
  styleUrls: ['./task-result-preview.component.less'],
})
export class TaskResultPreviewComponent implements OnInit, OnChanges {
  currentLineNumber = 0;

  artifacts: ArtifactDto[] = null;
  logLines: LogLine[];
  logsWebSocket: WebSocketSubject<LogDto>;
  @Input()
  task: TaskDto;
  tagWidth = 0;
  nodes: Array<NodeDto> = null;
  selectedNodes: Array<NodeDto> = null;

  constructor(private loggingService: LogHandlingService,
              private nodesService: NodesService,
              private tasksService: TasksService) {
  }

  ngOnInit() {
    this.initNodes();
    this.changeLogs();
  }

  private changeLogs() {
    this.loggingService.getTaskLogs({
      taskId: this.task.id,
      agents: this.getNodeNames(),
      queryAgents: true,
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
        if (this.selectedNodes.some(e => e.id === log.nodeId)) {
          this.updateTagWidth(log);
          this.logLines.push(this.mapToLogLine(log));
        }
      });
    });

    this.tasksService.getTaskArtifacts({
      taskId: this.task.id,
    }).pipe(first()).subscribe(artifacts => this.artifacts = artifacts);
  }

  private initNodes() {
    this.nodesService.getNodes({})
      .pipe(first())
      .subscribe((nodes: NodesDto) => {
        this.nodes = nodes.items;
        this.selectedNodes = nodes.items;
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

  private getNodeNames(): Array<string> {
    if (this.selectedNodes) {
      return this.selectedNodes.map(o => o.id);
    }
  }

  public changeAgents(agents: Array<NodeDto>): void {
    this.selectedNodes = agents;
    this.changeLogs();
  }

  ngOnChanges(changes: SimpleChanges) {
    this.initNodes();
  }
}
