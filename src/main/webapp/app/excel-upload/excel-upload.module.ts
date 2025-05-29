// excel-upload.module.ts
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { ExcelUploadComponent } from './excel-upload.component';
import { ExcelUploadRoutingModule } from './excel-upload-routing.module';

@NgModule({
  declarations: [ExcelUploadComponent],
  imports: [CommonModule, ReactiveFormsModule, FormsModule, HttpClientModule, ExcelUploadRoutingModule],
})
export class ExcelUploadModule {}

import { Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./excel-upload.component'),
    canActivate: [UserRouteAccessService],
    data: {
      pageTitle: 'Excel Upload',
      defaultSort: 'id,asc',
    },
  },
  {
    path: 'batch/:batchNo',
    loadComponent: () => import('./batch-detail/batch-detail.component'),
    canActivate: [UserRouteAccessService],
    data: {
      pageTitle: 'Batch Details',
    },
  },
  {
    path: 'reports',
    loadComponent: () => import('./reports/upload-reports.component'),
    canActivate: [UserRouteAccessService],
    data: {
      pageTitle: 'Upload Reports',
    },
  },
];
