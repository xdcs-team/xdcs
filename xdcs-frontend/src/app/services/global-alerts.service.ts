import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class GlobalAlertsService {
  private alerts: Alert[] = [];

  constructor() {

  }

  addAlert(alert: Alert): void {
    this.alerts.push(alert);
  }

  getAlerts(): Alert[] {
    return this.alerts;
  }

  dismiss(dismissed: Alert): void {
    this.alerts = this.alerts.filter(alert => alert !== dismissed);
  }
}

export class Alert {
  type: string;
  message: string;
  timeout: number;

  constructor(type: string, message: string, timeout = 0) {
    this.type = type;
    this.message = message;
    this.timeout = timeout;
  }
}
