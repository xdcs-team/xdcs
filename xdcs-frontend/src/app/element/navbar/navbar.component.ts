import { Component, OnInit } from '@angular/core';
import { Route, Router, Routes } from '@angular/router';
import { routes } from '../../app-routing.module';
import { navbarItemNames } from '../../services/navbar.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.less'],
})
export class NavbarComponent implements OnInit {
  private routes: Routes = routes;

  constructor(private router: Router) {

  }

  ngOnInit() {

  }

  private getItemName(route: Route): string {
    return navbarItemNames.get(route.component);
  }
}
