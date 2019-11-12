import { Component, OnInit } from '@angular/core';
import { NewTaskComponent } from 'src/app/modal/new-task/new-task.component';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.less'],
})
export class HomeComponent {
  NewTaskComponent = NewTaskComponent;

}
