import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-sign-in',
  templateUrl: './sign-in.component.html',
  styleUrls: ['./sign-in.component.less'],
})
export class SignInComponent {
  constructor(private route: ActivatedRoute) {

  }
}
