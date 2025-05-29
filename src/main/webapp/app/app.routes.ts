import { Routes } from '@angular/router';

import { Authority } from 'app/config/authority.constants';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { errorRoute } from './layouts/error/error.route';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./home/home.component'),
    title: 'home.title',
  },
  {
    path: '',
    loadComponent: () => import('./layouts/navbar/navbar.component'),
    outlet: 'navbar',
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
    loadChildren: () => import('./account/account.route'),
  },
  {
    path: 'login',
    loadComponent: () => import('./login/login.component'),
    title: 'login.title',
  },
  {
    path: '',
    loadChildren: () => import(`./entities/entity.routes`),
  },
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
  ...errorRoute,
];

export default routes;
