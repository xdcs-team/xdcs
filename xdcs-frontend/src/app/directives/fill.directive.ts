import { Directive, ElementRef, HostBinding, HostListener, Input, OnInit } from '@angular/core';

@Directive({
  selector: '[appSizeFill]',
})
export class FillDirective implements OnInit {
  @Input()
  fillHeight = false;

  @Input()
  fillWidth = false;

  @HostBinding('style.height.px')
  hostHeight;

  @HostBinding('style.width.px')
  hostWidth;

  constructor(private element: ElementRef) {

  }

  @HostListener('window:resize')
  onResize() {
    if (this.fillHeight) {
      this.recalculateHeight();
    }

    if (this.fillWidth) {
      this.recalculateWidth();
    }
  }

  ngOnInit(): void {
    setTimeout(() => {
      this.onResize();
    });
  }

  private recalculateHeight() {
    const thisNode = this.element.nativeElement;
    const parentNode = thisNode.parentNode;

    let newHeight = parentNode.offsetHeight;
    for (const sibling of parentNode.children) {
      if (sibling !== thisNode) {
        newHeight -= sibling.offsetHeight;
      }
    }

    this.hostHeight = newHeight;
  }

  private recalculateWidth() {
    const thisNode = this.element.nativeElement;
    const parentNode = thisNode.parentNode;

    let newWidth = parentNode.offsetWidth;
    for (const sibling of parentNode.children) {
      if (sibling !== thisNode) {
        newWidth -= sibling.offsetWidth;
      }
    }

    this.hostWidth = newWidth;
  }
}
