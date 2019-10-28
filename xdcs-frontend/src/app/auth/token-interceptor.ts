import { HTTP_INTERCEPTORS, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { forwardRef, Injectable, Provider } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { mergeMap } from 'rxjs/operators';

export const TOKEN_INTERCEPTOR_PROVIDER: Provider = {
  provide: HTTP_INTERCEPTORS,
  useExisting: forwardRef(() => TokenInterceptor),
  multi: true,
};

@Injectable()
export class TokenInterceptor implements HttpInterceptor {
  constructor(private auth: AuthService) {

  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (this.auth.isAuthUrl(req.url)) {
      return next.handle(req);
    }

    return this.auth.getAccessToken().pipe(
      mergeMap(accessToken => {
        return next.handle(req.clone({
          setHeaders: {
            Authorization: `Bearer ${accessToken}`,
          },
        }));
      }),
    );
  }
}
