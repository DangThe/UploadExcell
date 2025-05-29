// excel-upload-routing.module.ts
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ExcelUploadComponent } from './excel-upload.component';

const routes: Routes = [
  {
    path: '',
    component: ExcelUploadComponent,
    data: {
      pageTitle: 'Excel Upload',
    },
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ExcelUploadRoutingModule {}
