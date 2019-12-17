import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'noValue',
})
export class NoValuePipe implements PipeTransform {
  transform(value: string, noValue = 'No value'): string {
    if (value === null) {
      return `<i>${noValue}</i>`;
    }
    return value;
  }
}
