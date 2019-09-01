import { Component, Input, OnInit } from '@angular/core';
import { Route, Router } from '@angular/router';
import { navbarItemNames } from '../../services/navbar.service';
import { TaskDefinitionsComponent } from '../../view/task-definitions/task-definitions.component';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.less'],
})
export class NavbarComponent implements OnInit {
  private routes = [
    {
      path: '/task-definitions',
      component: TaskDefinitionsComponent
    },
  ];

  @Input()
  showLinks = true;

  constructor(private router: Router,
              private authService: AuthService) {

  }

  ngOnInit() {

  }

  private getItemName(route: Route): string {
    return navbarItemNames.get(route.component);
  }

  logOut() {
    this.authService.logOutAndRedirectToSignIn();
  }

  isAuthenticated() {
    return this.authService.isAuthenticated();
  }
}
