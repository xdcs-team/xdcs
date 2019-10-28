import { Component, Input, OnInit } from '@angular/core';
import { TaskDefinitionDto } from '../../../api/models/task-definition-dto';

@Component({
  selector: 'app-task-definition-preview',
  templateUrl: './task-definition-preview.component.html',
  styleUrls: ['./task-definition-preview.component.less'],
})
export class TaskDefinitionPreviewComponent implements OnInit {
  @Input()
  definition: TaskDefinitionDto;

  constructor() {

  }

  ngOnInit() {

  }
}
