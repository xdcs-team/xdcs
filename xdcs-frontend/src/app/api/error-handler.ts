import { HTTP_INTERCEPTORS, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { forwardRef, Injectable, Provider } from '@angular/core';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Alert, GlobalAlertsService } from '../services/global-alerts.service';

export const API_INTERCEPTOR_PROVIDER: Provider = {
  provide: HTTP_INTERCEPTORS,
  useExisting: forwardRef(() => ApiInterceptor),
  multi: true,
};

@Injectable()
export class ApiInterceptor implements HttpInterceptor {
  private alerts: GlobalAlertsService;

  constructor(alerts: GlobalAlertsService) {
    this.alerts = alerts;
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      tap(x => x, err => {
        const alert = new Alert('danger', 'An error occurred', 5);
        this.alerts.addAlert(alert);
      })
    );
  }
}
