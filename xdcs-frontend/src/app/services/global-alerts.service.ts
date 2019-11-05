import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
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
  type: 'success' | 'info' | 'warning' | 'danger';
  message: string;
  timeout: number;

  constructor(type: 'success' | 'info' | 'warning' | 'danger', message: string, timeout: number | 'short' | 'long' = 0) {
    this.type = type;
    this.message = message;
    switch (timeout) {
      case 'short':
        this.timeout = 2;
        break;
      case 'long':
        this.timeout = 5;
        break;
      default:
        this.timeout = timeout;
    }
  }
}
