import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { Translate, getSortState } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { ASC, DESC } from 'app/shared/util/pagination.constants';
import { overrideSortStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities } from './smart-contract.reducer';

export const SmartContract = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [sortState, setSortState] = useState(overrideSortStateWithQueryParams(getSortState(pageLocation, 'id'), pageLocation.search));

  const smartContractList = useAppSelector(state => state.smartContract.entities);
  const loading = useAppSelector(state => state.smartContract.loading);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        sort: `${sortState.sort},${sortState.order}`,
      }),
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?sort=${sortState.sort},${sortState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [sortState.order, sortState.sort]);

  const sort = p => () => {
    setSortState({
      ...sortState,
      order: sortState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handleSyncList = () => {
    sortEntities();
  };

  const getSortIconByFieldName = (fieldName: string) => {
    const sortFieldName = sortState.sort;
    const order = sortState.order;
    if (sortFieldName !== fieldName) {
      return faSort;
    }
    return order === ASC ? faSortUp : faSortDown;
  };

  return (
    <div>
      <h2 id="smart-contract-heading" data-cy="SmartContractHeading">
        <Translate contentKey="deFiProtocolRevivalApp.smartContract.home.title">Smart Contracts</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="deFiProtocolRevivalApp.smartContract.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/smart-contract/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="deFiProtocolRevivalApp.smartContract.home.createLabel">Create new Smart Contract</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {smartContractList && smartContractList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="deFiProtocolRevivalApp.smartContract.id">ID</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('name')}>
                  <Translate contentKey="deFiProtocolRevivalApp.smartContract.name">Name</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('name')} />
                </th>
                <th className="hand" onClick={sort('githubUrl')}>
                  <Translate contentKey="deFiProtocolRevivalApp.smartContract.githubUrl">Github Url</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('githubUrl')} />
                </th>
                <th className="hand" onClick={sort('originalCode')}>
                  <Translate contentKey="deFiProtocolRevivalApp.smartContract.originalCode">Original Code</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('originalCode')} />
                </th>
                <th className="hand" onClick={sort('resurrectedCode')}>
                  <Translate contentKey="deFiProtocolRevivalApp.smartContract.resurrectedCode">Resurrected Code</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('resurrectedCode')} />
                </th>
                <th className="hand" onClick={sort('isValidated')}>
                  <Translate contentKey="deFiProtocolRevivalApp.smartContract.isValidated">Is Validated</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('isValidated')} />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {smartContractList.map((smartContract, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/smart-contract/${smartContract.id}`} color="link" size="sm">
                      {smartContract.id}
                    </Button>
                  </td>
                  <td>{smartContract.name}</td>
                  <td>{smartContract.githubUrl}</td>
                  <td>{smartContract.originalCode}</td>
                  <td>{smartContract.resurrectedCode}</td>
                  <td>{smartContract.isValidated ? 'true' : 'false'}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/smart-contract/${smartContract.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/smart-contract/${smartContract.id}/edit`}
                        color="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button
                        onClick={() => (window.location.href = `/smart-contract/${smartContract.id}/delete`)}
                        color="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.delete">Delete</Translate>
                        </span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && (
            <div className="alert alert-warning">
              <Translate contentKey="deFiProtocolRevivalApp.smartContract.home.notFound">No Smart Contracts found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default SmartContract;
