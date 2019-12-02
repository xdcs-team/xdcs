import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SignInComponent } from './view/sign-in/sign-in.component';
import { HomeComponent } from './view/home/home.component';
import { TaskDefinitionsComponent } from './view/task-definitions/task-definitions.component';
import { TaskDefinitionComponent } from './view/task-definition/task-definition.component';
import { AuthGuard } from './auth/auth.guard';
import { NodeComponent } from './view/node/node.component';
import { NodesComponent } from './view/nodes/nodes.component';
import { TaskCreationComponent } from './view/task-creation/task-creation.component';
import { TaskLogComponent } from './view/task-log/task-log.component';

export const routes: Routes = [
  { path: 'sign-in', component: SignInComponent },
  {
    path: '',
    canActivate: [AuthGuard],
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'home', component: HomeComponent },
      { path: 'task-definitions', component: TaskDefinitionsComponent },
      { path: 'task-definitions/:definitionId', component: TaskDefinitionComponent },
      { path: 'nodes', component: NodesComponent },
      { path: 'nodes/:nodeId', component: NodeComponent },
      { path: 'new-task/:deploymentId', component: TaskCreationComponent },
      { path: 'task-log/:taskId', component: TaskLogComponent },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule],
})
export class AppRoutingModule {

}
