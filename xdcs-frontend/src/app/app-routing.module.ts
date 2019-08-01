import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AgentsComponent } from './agents/agents.component';
import { SignInComponent } from './view/sign-in/sign-in.component';
import { HomeComponent } from './view/home/home.component';
import { TaskDefinitionsComponent } from './view/task-definitions/task-definitions.component';
import { TaskDefinitionComponent } from './view/task-definition/task-definition.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'agents', component: AgentsComponent },
  { path: 'sign-in', component: SignInComponent },
  { path: 'task/definitions', component: TaskDefinitionsComponent },
  { path: 'task/definition', component: TaskDefinitionComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
