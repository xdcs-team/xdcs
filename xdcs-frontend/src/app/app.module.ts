import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HttpClientModule } from '@angular/common/http';
import { AgentsComponent } from './agents/agents.component';
import { environment } from '../environments/environment';
import { ApiModule } from '../api/api.module';
import { NodeCardComponent } from './element/node-card/node-card.component';
import { NodeCardsComponent } from './element/node-cards/node-cards.component';
import { NavbarComponent } from './element/navbar/navbar.component';
import { SignInComponent } from './view/sign-in/sign-in.component';
import { HomeComponent } from './view/home/home.component';
import { TaskSummaryItemComponent } from './element/task-summary-item/task-summary-item.component';
import { TaskSummaryListComponent } from './element/task-summary-list/task-summary-list.component';
import { AlertModule, CollapseModule } from 'ngx-bootstrap';
import { OcticonDirective } from './directives/octicon.directive';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { GlobalAlertsComponent } from './element/global-alerts/global-alerts.component';
import { API_INTERCEPTOR_PROVIDER, ApiInterceptor } from './api/error-handler';

@NgModule({
  declarations: [
    AppComponent,
    AgentsComponent,
    NavbarComponent,
    NodeCardComponent,
    NodeCardsComponent,
    SignInComponent,
    HomeComponent,
    TaskSummaryItemComponent,
    TaskSummaryListComponent,
    OcticonDirective,
    GlobalAlertsComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    ApiModule.forRoot({ rootUrl: environment.serverUrl }),
    BrowserAnimationsModule,
    CollapseModule.forRoot(),
    AlertModule.forRoot(),
    FontAwesomeModule,
  ],
  providers: [
    HttpClientModule,
    ApiInterceptor,
    API_INTERCEPTOR_PROVIDER,
  ],
  bootstrap: [AppComponent]
})
export class AppModule {

}
