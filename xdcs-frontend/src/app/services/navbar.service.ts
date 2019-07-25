import { Type } from '@angular/core';

export let navbarItemNames: Map<Type<any>, string> =
  new Map<Type<any>, string>();

export function NavbarItem(name: string): ClassDecorator {
  return (component: any) => {
    navbarItemNames.set(component, name);
  };
}
