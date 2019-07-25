import { Component, OnInit } from '@angular/core';
import {printLine} from "tslint/lib/verify/lines";

@Component({
  selector: 'app-submit-task',
  templateUrl: './submit-task.component.html',
  styleUrls: ['./submit-task.component.less']
})
export class SubmitTaskComponent implements OnInit {

  constructor() { }

  ngOnInit() {
  }

  spierdalaj() {
    alert("wypierdalaj")
  }
}




