import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../api/services/api.service';
import { AgentEntryDto } from '../../api/models/agent-entry-dto';

@Component({
  selector: 'app-agents',
  templateUrl: './agents.component.html',
  styleUrls: ['./agents.component.less']
})
export class AgentsComponent implements OnInit {
  private apiService: ApiService;

  private agents: Array<AgentEntryDto>;

  constructor(apiService: ApiService) {
    this.apiService = apiService;
  }

  ngOnInit() {
    this.apiService.getAgentList()
      .subscribe(agents => this.agents = agents);
  }
}
