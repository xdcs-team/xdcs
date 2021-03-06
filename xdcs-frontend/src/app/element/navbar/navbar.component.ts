import { Component, Input, OnInit } from '@angular/core';
import { Route, Router } from '@angular/router';
import { navbarItemNames } from '../../services/navbar.service';
import { TaskDefinitionsComponent } from '../../view/task-definitions/task-definitions.component';
import { AuthService } from '../../auth/auth.service';
import { NodesComponent } from '../../view/nodes/nodes.component';
import { TasksComponent } from '../../view/tasks/tasks.component';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.less'],
})
export class NavbarComponent implements OnInit {
  private routes = [
    {
      path: '/task-definitions',
      component: TaskDefinitionsComponent,
    },
    {
      path: '/nodes',
      component: NodesComponent,
    },
    {
      path: '/tasks',
      component: TasksComponent,
    },
  ];

  @Input()
  showLinks = true;

  @Input()
  fullscreenMode = false;

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
