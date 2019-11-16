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
        if (err && err.status && err.status / 100 !== 5 &&
          err.error && typeof err.error === 'string') {
          let errorMessage: any;
          try {
            const parsed = JSON.parse(err.error);
            errorMessage = parsed.error ? parsed.error : err.error;
          } catch (e) {
            errorMessage = err.error;
          }
          const alert = new Alert('danger', 'Error: ' + errorMessage, 5);
          this.alerts.addAlert(alert);
        } else {
          const alert = new Alert('danger', 'An error occurred', 5);
          this.alerts.addAlert(alert);
        }
      })
    );
  }
}
