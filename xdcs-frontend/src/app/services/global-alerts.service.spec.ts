import { TestBed } from '@angular/core/testing';

import { GlobalAlertsService } from './global-alerts.service';

describe('GlobalAlertsService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: GlobalAlertsService = TestBed.get(GlobalAlertsService);
    expect(service).toBeTruthy();
  });
});
