import { Pipe, PipeTransform } from '@angular/core';
import { SlicePipe } from '@angular/common';

@Pipe({
  name: 'sliceOrId',
})
export class SliceOrIdPipe implements PipeTransform {
  transform(value: any, ...args: any[]): any {
    return new SlicePipe().transform(value, 0, 8);
  }
}
