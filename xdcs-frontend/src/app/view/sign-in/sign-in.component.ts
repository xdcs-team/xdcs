import { AfterContentChecked, Component, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-sign-in',
  templateUrl: './sign-in.component.html',
  styleUrls: ['./sign-in.component.less'],
})
export class SignInComponent implements AfterContentChecked {
  disabled = false;

  @ViewChild('username', { static: false })
  username;

  @ViewChild('password', { static: false })
  password;

  constructor(public route: ActivatedRoute,
              private authService: AuthService) {

  }

  ngAfterContentChecked() {
    if (this.authService.isAuthenticated()) {
      this.redirect();
    }
  }

  signIn() {
    this.disabled = true;
    this.authService.authenticate(
      this.username.nativeElement.value,
      this.password.nativeElement.value
    ).pipe(first()).subscribe(
      data => this.redirect(),
      error => {
        alert('Error: ' + JSON.stringify(error));
        this.disabled = false;
      }
    );
  }

  private redirect() {
    const redirectUri = this.route.snapshot.queryParamMap.get('redirect');
    if (redirectUri) {
      window.location.href = '/#' + redirectUri;
    } else {
      window.location.href = '/#/';
    }
  }
}
