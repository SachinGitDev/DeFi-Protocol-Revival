import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './smart-contract.reducer';

export const SmartContractDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const smartContractEntity = useAppSelector(state => state.smartContract.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="smartContractDetailsHeading">
          <Translate contentKey="deFiProtocolRevivalApp.smartContract.detail.title">SmartContract</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{smartContractEntity.id}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="deFiProtocolRevivalApp.smartContract.name">Name</Translate>
            </span>
          </dt>
          <dd>{smartContractEntity.name}</dd>
          <dt>
            <span id="githubUrl">
              <Translate contentKey="deFiProtocolRevivalApp.smartContract.githubUrl">Github Url</Translate>
            </span>
          </dt>
          <dd>{smartContractEntity.githubUrl}</dd>
          <dt>
            <span id="originalCode">
              <Translate contentKey="deFiProtocolRevivalApp.smartContract.originalCode">Original Code</Translate>
            </span>
          </dt>
          <dd>{smartContractEntity.originalCode}</dd>
          <dt>
            <span id="resurrectedCode">
              <Translate contentKey="deFiProtocolRevivalApp.smartContract.resurrectedCode">Resurrected Code</Translate>
            </span>
          </dt>
          <dd>{smartContractEntity.resurrectedCode}</dd>
          <dt>
            <span id="isValidated">
              <Translate contentKey="deFiProtocolRevivalApp.smartContract.isValidated">Is Validated</Translate>
            </span>
          </dt>
          <dd>{smartContractEntity.isValidated ? 'true' : 'false'}</dd>
        </dl>
        <Button tag={Link} to="/smart-contract" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/smart-contract/${smartContractEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default SmartContractDetail;
