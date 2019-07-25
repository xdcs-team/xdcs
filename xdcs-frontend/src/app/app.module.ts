import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HttpClientModule } from '@angular/common/http';
import { environment } from '../environments/environment';
import { ApiModule } from '../api/api.module';
import { NodeCardComponent } from './element/node-card/node-card.component';
import { NodeCardsComponent } from './element/node-cards/node-cards.component';
import { NavbarComponent } from './element/navbar/navbar.component';
import { SignInComponent } from './view/sign-in/sign-in.component';
import { HomeComponent } from './view/home/home.component';
import { TaskSummaryItemComponent } from './element/task-summary-item/task-summary-item.component';
import { TaskSummaryListComponent } from './element/task-summary-list/task-summary-list.component';
import { AlertModule, CollapseModule, SortableModule } from 'ngx-bootstrap';
import { OcticonDirective } from './directives/octicon.directive';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { GlobalAlertsComponent } from './element/global-alerts/global-alerts.component';
import { API_INTERCEPTOR_PROVIDER, ApiInterceptor } from './api/error-handler';
import { CodeEditorComponent } from './element/code-editor/code-editor.component';
import { AceEditorModule } from 'ng2-ace-editor';
import { TaskDefinitionsComponent } from './view/task-definitions/task-definitions.component';
import { TaskDefinitionComponent } from './view/task-definition/task-definition.component';
import { AngularSplitModule } from 'angular-split';
import { JwtModule } from '@auth0/angular-jwt';
import { TOKEN_INTERCEPTOR_PROVIDER, TokenInterceptor } from './auth/token-interceptor';
import { NoDataComponent } from './element/no-data/no-data.component';
import { AUTH_ERROR_INTERCEPTOR_PROVIDER, AuthErrorInterceptor } from './auth/auth-error-interceptor';
import { SubmitTaskComponent } from './view/submit-task/submit-task.component';
import { TaskDefConfigComponent } from './element/task-def-config/task-def-config.component';

@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    NodeCardComponent,
    NodeCardsComponent,
    SignInComponent,
    HomeComponent,
    TaskSummaryItemComponent,
    TaskSummaryListComponent,
    OcticonDirective,
    GlobalAlertsComponent,
    CodeEditorComponent,
    TaskDefinitionsComponent,
    TaskDefinitionComponent,
    NoDataComponent,
    TaskDefConfigComponent,
    SubmitTaskComponent,
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
    AceEditorModule,
    AngularSplitModule,
    JwtModule.forRoot({
      config: {
        tokenGetter: () => undefined,
      },
    }),
    SortableModule.forRoot(),
  ],
  providers: [
    HttpClientModule,
    ApiInterceptor,
    API_INTERCEPTOR_PROVIDER,
    TokenInterceptor,
    TOKEN_INTERCEPTOR_PROVIDER,
    AuthErrorInterceptor,
    AUTH_ERROR_INTERCEPTOR_PROVIDER,
  ],
  bootstrap: [AppComponent]
})
export class AppModule {

}
