import { Component, OnInit } from '@angular/core';
import { NavbarItem } from '../../services/navbar.service';
import { TaskDefinitionDto } from '../../../api/models/task-definition-dto';
import { TaskDefinitionsService } from '../../../api/services/task-definitions.service';
import { first } from 'rxjs/operators';
import { ActivatedRoute } from '@angular/router';
import { TaskDefinitionConfigDto } from '../../../api/models/task-definition-config-dto';
import { Alert, GlobalAlertsService } from '../../services/global-alerts.service';

@Component({
  selector: 'app-task-definition',
  templateUrl: './task-definition.component.html',
  styleUrls: ['./task-definition.component.less'],
  host: { class: 'd-flex flex-column' }
})
@NavbarItem('Task Definition')
export class TaskDefinitionComponent implements OnInit {
  taskDefinition: TaskDefinitionDto = null;

  taskDefinitionId: string;

  constructor(private taskDefinitionsService: TaskDefinitionsService,
              private route: ActivatedRoute,
              private globalAlerts: GlobalAlertsService) {
    this.taskDefinitionId = this.route.snapshot.params.id;
  }

  ngOnInit(): void {
    this.taskDefinitionsService.getTaskDefinition({
      taskDefinitionId: this.taskDefinitionId
    }).pipe(first()).subscribe(def => {
      this.taskDefinition = def;
    });
  }

  saveTaskDefinitionConfig(config: TaskDefinitionConfigDto) {
    this.taskDefinitionsService.setTaskDefinitionConfiguration({
      taskDefinitionId: this.taskDefinitionId,
      body: config,
    }).pipe(first()).subscribe(_ => {
      this.globalAlerts.addAlert(new Alert('success', 'Task definition saved'));
    });
  }
}
