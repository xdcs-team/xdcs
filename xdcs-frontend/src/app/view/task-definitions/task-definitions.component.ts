import { Component } from '@angular/core';
import { NavbarItem } from '../../services/navbar.service';

@Component({
  selector: 'app-task-definitions',
  templateUrl: './task-definitions.component.html',
  styleUrls: ['./task-definitions.component.less']
})
@NavbarItem('Task Definitions')
export class TaskDefinitionsComponent {
  constructor() {

  }
}
