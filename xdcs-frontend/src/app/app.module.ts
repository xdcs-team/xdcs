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
import { NodesComponent } from './view/nodes/nodes.component';
import { NodesListComponent } from './element/nodes-list/nodes-list.component';
import { NodeComponent } from './view/node/node.component';
import { NodePreviewComponent } from './element/node-preview/node-preview.component';
import { TaskSummaryItemComponent } from './element/task-summary-item/task-summary-item.component';
import { TaskSummaryListComponent } from './element/task-summary-list/task-summary-list.component';
import { AlertModule, CollapseModule, ModalModule, SortableModule, TooltipModule } from 'ngx-bootstrap';
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
import { TaskDefConfigComponent } from './element/task-def-config/task-def-config.component';
import { FullscreenListComponent } from './element/fullscreen-list/fullscreen-list.component';
import { TaskDefinitionPreviewComponent } from './element/task-definition-preview/task-definition-preview.component';
import { ModalComponent } from './element/modal/modal.component';
import { OpenModalDirective } from './directives/open-modal.directive';
import { NewTaskDefinitionComponent } from './modal/new-task-definition/new-task-definition.component';
import { NewTaskComponent } from './modal/new-task/new-task.component';
import { FileTreeComponent } from './element/file-tree/file-tree.component';
import { TreeDraggedElement, TreeModule } from 'angular-tree-component';
import { ContextMenuModule, ContextMenuService } from 'ngx-contextmenu';
import { ClipboardModule } from 'ngx-clipboard';
import { ConfirmationComponent } from './modal/confirmation/confirmation.component';
import { LoadingComponent } from './element/loading/loading.component';
import { NgxSpinnerModule } from 'ngx-spinner';
import { DeployDefinitionComponent } from './modal/deploy-definition/deploy-definition.component';
import { FillDirective } from './directives/fill.directive';
import { EditorToolbarComponent } from './element/editor-toolbar/editor-toolbar.component';
import { TaskCreationComponent } from './view/task-creation/task-creation.component';
import { ToolbarComponent } from './element/toolbar/toolbar.component';
import { FileTreeToolbarComponent } from './element/file-tree-toolbar/file-tree-toolbar.component';
import { CreateFileComponent } from './modal/create-file/create-file.component';
import { TimeAgoPipe } from 'time-ago-pipe';
import { CopyIconComponent } from './element/copy-icon/copy-icon.component';
import { OrderableInputListComponent } from './element/orderable-input-list/orderable-input-list.component';
import { UploadFileComponent } from './element/upload-file/upload-file.component';
import { ngfModule } from 'angular-file';
import { AutoFocusDirective } from './directives/auto-focus.directive';

@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    NodeCardComponent,
    NodeCardsComponent,
    SignInComponent,
    HomeComponent,
    NodesComponent,
    NodeComponent,
    NodePreviewComponent,
    NodesListComponent,
    TaskSummaryItemComponent,
    TaskSummaryListComponent,
    OcticonDirective,
    GlobalAlertsComponent,
    CodeEditorComponent,
    TaskDefinitionsComponent,
    TaskDefinitionComponent,
    TaskCreationComponent,
    NoDataComponent,
    TaskDefConfigComponent,
    FullscreenListComponent,
    TaskDefinitionPreviewComponent,
    ModalComponent,
    OpenModalDirective,
    NewTaskDefinitionComponent,
    NewTaskComponent,
    FileTreeComponent,
    ConfirmationComponent,
    LoadingComponent,
    DeployDefinitionComponent,
    FillDirective,
    EditorToolbarComponent,
    ToolbarComponent,
    FileTreeToolbarComponent,
    CreateFileComponent,
    OrderableInputListComponent,
    TimeAgoPipe,
    CopyIconComponent,
    UploadFileComponent,
    AutoFocusDirective,
  ],
  entryComponents: [
    NewTaskDefinitionComponent,
    NewTaskComponent,
    ConfirmationComponent,
    DeployDefinitionComponent,
    CreateFileComponent,
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
        tokenGetter: jwtTokenGetter,
      },
    }),
    SortableModule.forRoot(),
    ModalModule.forRoot(),
    TreeModule.forRoot(),
    ContextMenuModule.forRoot({
      useBootstrap4: true,
    }),
    ClipboardModule,
    NgxSpinnerModule,
    TooltipModule.forRoot(),
    ngfModule,
  ],
  providers: [
    HttpClientModule,
    ApiInterceptor,
    API_INTERCEPTOR_PROVIDER,
    TokenInterceptor,
    TOKEN_INTERCEPTOR_PROVIDER,
    AuthErrorInterceptor,
    AUTH_ERROR_INTERCEPTOR_PROVIDER,
    TreeDraggedElement,
    ContextMenuService,
  ],
  bootstrap: [AppComponent],
})
export class AppModule {

}

export function jwtTokenGetter() {
  return undefined;
}
