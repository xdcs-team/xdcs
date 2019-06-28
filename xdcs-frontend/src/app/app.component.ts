import { Component } from '@angular/core';
import { ApiService } from '../api/services/api.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.less']
})
export class AppComponent {
  title = 'xdcs-frontend';
  private apiService: ApiService;

  constructor(apiService: ApiService) {
    this.apiService = apiService;
  }

  public test() {
    this.apiService.getAgentList()
      .subscribe(agents => console.log(agents));
  }
}
