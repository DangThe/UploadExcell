// Location: src/main/webapp/app/app-routing.module.ts
// Updated app routing to include Excel Upload route

import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { errorRoute } from './layouts/error/error.route';
import { navbarRoute } from './layouts/navbar/navbar.route';
import { DEBUG_INFO_ENABLED } from 'app/app.constants';
import { Authority } from 'app/config/authority.constants';

import UserRouteAccessService from 'app/core/auth/user-route-access.service';

@NgModule({
  imports: [
    RouterModule.forRoot(
      [
        {
          path: 'admin',
          data: {
            authorities: [Authority.ADMIN],
          },
          canActivate: [UserRouteAccessService],
          loadChildren: () => import('./admin/admin-routing.module').then(m => m.AdminRoutingModule),
        },
        {
          path: 'account',
          loadChildren: () => import('./account/account.module').then(m => m.AccountModule),
        },
        {
          path: 'login',
          loadChildren: () => import('./login/login.module').then(m => m.LoginModule),
        },
        // Excel Upload Route - Add this section
        {
          path: 'excel-upload',
          data: {
            authorities: [Authority.USER], // Require USER role
            pageTitle: 'Excel Upload System',
          },
          canActivate: [UserRouteAccessService],
          loadChildren: () => import('./excel-upload/excel-upload.module').then(m => m.ExcelUploadModule),
        },
        {
          path: '',
          loadChildren: () => import(`./entities/entity-routing.module`).then(m => m.EntityRoutingModule),
        },
        navbarRoute,
        ...errorRoute,
      ],
      {
        enableTracing: DEBUG_INFO_ENABLED,
        bindToComponentInputs: true,
      },
    ),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}

// Alternative approach for JHipster 8+ with standalone components
// Location: src/main/webapp/app/app.routes.ts

import { Routes } from '@angular/router';
import { Authority } from 'app/config/authority.constants';
import UserRouteAccessService from 'app/core/auth/user-route-access.service';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./home/home.component'),
    title: 'Welcome, Java Hipster!',
  },
  {
    path: '',
    loadComponent: () => import('./layouts/navbar/navbar.component'),
    outlet: 'navbar',
  },
  // Excel Upload Routes
  {
    path: 'excel-upload',
    data: {
      authorities: [Authority.USER],
      pageTitle: 'Excel Upload System',
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./excel-upload/excel-upload.routes').then(r => r.routes),
  },
  {
    path: 'admin',
    data: {
      authorities: [Authority.ADMIN],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./admin/admin.routes'),
  },
  {
    path: 'account',
    loadChildren: () => import('./account/account.routes'),
  },
  {
    path: 'login',
    loadChildren: () => import('./login/login.routes'),
  },
  {
    path: '',
    loadChildren: () => import('./entities/entity.routes'),
  },
  {
    path: '**',
    redirectTo: '',
  },
];

// Excel Upload specific routes
// Location: src/main/webapp/app/excel-upload/excel-upload.routes.ts

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

// i18n translation keys
// Location: src/main/webapp/i18n/en/global.json
// Add these translation keys:

/*
{
  "global": {
    "menu": {
      "excel-upload": "Excel Upload"
    }
  },
  "excelUpload": {
    "home": {
      "title": "Excel Upload System",
      "subtitle": "Upload and process Excel files with validation",
      "createLabel": "Upload New File",
      "createOrEditLabel": "Upload Excel File"
    },
    "created": "Excel file uploaded successfully with identifier {{ param }}",
    "updated": "Batch {{ param }} has been updated",
    "deleted": "Batch {{ param }} has been deleted",
    "delete": {
      "question": "Are you sure you want to delete Batch {{ id }}?"
    },
    "detail": {
      "title": "Batch Details"
    },
    "batchNo": "Batch Number",
    "branchCode": "Branch Code",
    "sourceCode": "Source Code",
    "uploadDate": "Upload Date",
    "totalRows": "Total Rows",
    "successCount": "Success Count",
    "errorCount": "Error Count",
    "status": "Status"
  }
}
*/
