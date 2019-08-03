import { Component } from '@angular/core';
import { NavbarItem } from '../../services/navbar.service';

@Component({
  selector: 'app-task-definition',
  templateUrl: './task-definition.component.html',
  styleUrls: ['./task-definition.component.less'],
  host: { class: 'd-flex flex-column' }
})
@NavbarItem('Task Definition')
export class TaskDefinitionComponent {
  constructor() {

  }
}
