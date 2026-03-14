import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { createEntity, getEntity, reset, updateEntity } from './smart-contract.reducer';

export const SmartContractUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const smartContractEntity = useAppSelector(state => state.smartContract.entity);
  const loading = useAppSelector(state => state.smartContract.loading);
  const updating = useAppSelector(state => state.smartContract.updating);
  const updateSuccess = useAppSelector(state => state.smartContract.updateSuccess);

  const handleClose = () => {
    navigate('/smart-contract');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    if (values.id !== undefined && typeof values.id !== 'number') {
      values.id = Number(values.id);
    }

    const entity = {
      ...smartContractEntity,
      ...values,
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...smartContractEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="deFiProtocolRevivalApp.smartContract.home.createOrEditLabel" data-cy="SmartContractCreateUpdateHeading">
            <Translate contentKey="deFiProtocolRevivalApp.smartContract.home.createOrEditLabel">Create or edit a SmartContract</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="smart-contract-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('deFiProtocolRevivalApp.smartContract.name')}
                id="smart-contract-name"
                name="name"
                data-cy="name"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('deFiProtocolRevivalApp.smartContract.githubUrl')}
                id="smart-contract-githubUrl"
                name="githubUrl"
                data-cy="githubUrl"
                type="text"
              />
              <ValidatedField
                label={translate('deFiProtocolRevivalApp.smartContract.originalCode')}
                id="smart-contract-originalCode"
                name="originalCode"
                data-cy="originalCode"
                type="textarea"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('deFiProtocolRevivalApp.smartContract.resurrectedCode')}
                id="smart-contract-resurrectedCode"
                name="resurrectedCode"
                data-cy="resurrectedCode"
                type="textarea"
              />
              <ValidatedField
                label={translate('deFiProtocolRevivalApp.smartContract.isValidated')}
                id="smart-contract-isValidated"
                name="isValidated"
                data-cy="isValidated"
                check
                type="checkbox"
              />
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/smart-contract" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default SmartContractUpdate;
