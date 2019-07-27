import { Component, OnInit } from '@angular/core';
import { NavbarItem } from '../../services/navbar.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.less']
})
@NavbarItem('Home')
export class HomeComponent implements OnInit {

  constructor() { }

  ngOnInit() {
  }

}
