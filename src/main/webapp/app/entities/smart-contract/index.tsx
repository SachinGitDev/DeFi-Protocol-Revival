import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import SmartContract from './smart-contract';
import SmartContractDetail from './smart-contract-detail';
import SmartContractUpdate from './smart-contract-update';
import SmartContractDeleteDialog from './smart-contract-delete-dialog';

const SmartContractRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<SmartContract />} />
    <Route path="new" element={<SmartContractUpdate />} />
    <Route path=":id">
      <Route index element={<SmartContractDetail />} />
      <Route path="edit" element={<SmartContractUpdate />} />
      <Route path="delete" element={<SmartContractDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default SmartContractRoutes;
