import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { DeploymentsService } from '../../../api/services/deployments.service';
import { first } from 'rxjs/operators';
import { Alert, GlobalAlertsService } from '../../services/global-alerts.service';

@Component({
  selector: 'app-deploy-definition',
  templateUrl: './deploy-definition.component.html',
  styleUrls: ['./deploy-definition.component.less'],
})
export class DeployDefinitionComponent implements OnInit {
  @ViewChild('modal', { static: false })
  private modal;

  private description: string;

  @Input()
  taskDefinitionId: string;

  constructor(private deploymentsService: DeploymentsService,
              private globalAlertsService: GlobalAlertsService) {

  }

  ngOnInit() {

  }

  private doDeploy() {
    this.deploymentsService.deployTaskDefinition({
      body: {
        from: this.taskDefinitionId,
        description: this.description,
      },
    }).pipe(first()).subscribe(() => {
      this.globalAlertsService.addAlert(
        new Alert('success', 'Deployed successfully', 'short'));
      this.modal.closeModal();
    });
  }
}
