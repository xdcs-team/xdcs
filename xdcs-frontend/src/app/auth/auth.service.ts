import { Injectable } from '@angular/core';
import { JwtHelperService } from '@auth0/angular-jwt';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { flatMap, map, tap } from 'rxjs/operators';
import { ActivatedRoute, Router } from '@angular/router';

const CLIENT_ID = 'web';
const AUTH_URL = '/xdcs/auth/auth';
const TOKEN_URL = '/xdcs/auth/token';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private router: Router,
              private route: ActivatedRoute,
              private jwtHelper: JwtHelperService,
              private http: HttpClient) {

  }

  private isExpired(token) {
    return this.jwtHelper.isTokenExpired(token);
  }

  getAccessToken(): Observable<string> {
    const token = localStorage.getItem('access_token');
    if (!token || this.isExpired(token)) {
      return this.refreshToken().pipe(
        tap(t => localStorage.setItem('access_token', t))
      );
    }
    return of(token);
  }

  private refreshToken(): Observable<string> {
    const refreshToken = localStorage.getItem('refresh_token');
    if (refreshToken == null || this.isExpired(refreshToken)) {
      this.redirectToSignIn();
      return null;
    }

    const tokenRequestBody = new HttpParams()
      .set('grant_type', 'refresh_token')
      .set('refresh_token', refreshToken);
    return this.http.post(TOKEN_URL, tokenRequestBody).pipe(
      map((data: any) => data.access_token),
    );
  }

  public isAuthenticated(): boolean {
    const token = localStorage.getItem('refresh_token');
    return token && !this.isExpired(token);
  }

  public authenticate(username: string, password: string): Observable<any> {
    const authParams = new HttpParams()
      .set('response_type', 'code')
      .set('client_id', CLIENT_ID);
    const authBody = new HttpParams()
      .set('username', username)
      .set('password', password);
    const formHeaders = new HttpHeaders()
      .set('Content-Type', 'application/x-www-form-urlencoded');
    return this.http.post(AUTH_URL, authBody, { params: authParams, headers: formHeaders }).pipe(
      map((data: any) => data.code),
      flatMap(code => {
        const tokenRequestBody = new HttpParams()
          .set('grant_type', 'authorization_code')
          .set('code', code.toString());
        return this.http.post(TOKEN_URL, tokenRequestBody);
      }),
      map((data: any) => {
        localStorage.setItem('refresh_token', data.refresh_token);
        localStorage.setItem('access_token', data.access_token);
      }),
    );
  }

  logOut() {
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('access_token');
    this.redirectToSignIn();
  }

  redirectToSignIn() {
    const redirectParam = this.route.snapshot.queryParamMap.get('redirect');
    const redirect = redirectParam ?
      redirectParam :
      window.location.hash.substr(1);

    this.router.navigate(['sign-in'], {
      queryParams: { redirect }
    });
  }
}

class TokenGrant {

}
