import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { IDropdownSettings } from 'ng-multiselect-dropdown/multiselect.model';

@Component({
  selector: 'app-multiselect-menu',
  templateUrl: './multiselect-menu.component.html',
  styleUrls: ['./multiselect-menu.component.less'],
})
export class MultiselectMenuComponent implements OnInit, OnChanges {
  @Input()
  dropdownList: any;
  @Input()
  idField: string;
  @Input()
  textField: string;
  @Output()
  selectionChange = new EventEmitter<any>();
  @Input()
  selectAll = false;

  selectedItems: any;
  dropdownSettings: IDropdownSettings = {};

  constructor() {
  }

  ngOnInit() {
    this.initSelectedItems();

    this.dropdownSettings = {
      singleSelection: false,
      idField: this.idField,
      textField: this.textField,
      selectAllText: 'Select All',
      unSelectAllText: 'Unselect All',
      itemsShowLimit: 10,
      allowSearchFilter: true,
    };
  }

  private initSelectedItems() {
    if (this.selectAll) {
      this.selectedItems = this.dropdownList;
      this.emit();
    }
  }

  emit() {
    this.selectionChange.emit(this.selectedItems);
  }

  emitAll() {
    this.selectionChange.emit(this.dropdownList);
  }

  emitEmpty() {
    this.selectionChange.emit([]);
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.initSelectedItems();
  }
}
