import { ErrorHandler } from '@angular/core';

export class CustomErrorHandler implements ErrorHandler {
  handleError(error: any) {
    console.error(error.fileName, error.lineNumber, ':', error.columnNumber, '\n', error.message, error.rejection);
  }
}

export const CUSTOM_ERROR_HANDLER_PROVIDER = {
  provide: ErrorHandler,
  useClass: CustomErrorHandler,
};
