import { HTTP_INTERCEPTORS, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { forwardRef, Injectable, Provider } from '@angular/core';
import { EMPTY, Observable, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { catchError } from 'rxjs/operators';

export const AUTH_ERROR_INTERCEPTOR_PROVIDER: Provider = {
  provide: HTTP_INTERCEPTORS,
  useExisting: forwardRef(() => AuthErrorInterceptor),
  multi: true,
};

@Injectable()
export class AuthErrorInterceptor implements HttpInterceptor {
  constructor(private auth: AuthService) {

  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const isAuth = this.auth.isAuthUrl(req.url);

    return next.handle(req).pipe(
      catchError(err => {
        const isUnauthorized = err.status === 401;
        const isInvalidToken = isAuth && err.status === 400;
        if (isUnauthorized || isInvalidToken) {
          this.auth.logOutAndRedirectToSignIn();
          return EMPTY;
        }

        return throwError(err);
      })
    );
  }
}
