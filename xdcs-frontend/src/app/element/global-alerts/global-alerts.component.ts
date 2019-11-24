import { Component, OnInit } from '@angular/core';
import { GlobalAlertsService } from '../../services/global-alerts.service';

@Component({
  selector: 'app-global-alerts',
  templateUrl: './global-alerts.component.html',
  styleUrls: ['./global-alerts.component.less'],
})
export class GlobalAlertsComponent implements OnInit {
  alerts: GlobalAlertsService;

  constructor(alerts: GlobalAlertsService) {
    this.alerts = alerts;
  }

  ngOnInit(): void {

  }

  onClosed(dismissedAlert: any): void {
    this.alerts.dismiss(dismissedAlert);
  }
}
